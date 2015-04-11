/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.core.world;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.gen.BiomeBuffer;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.util.SpongeHooks;
import org.spongepowered.mod.util.gen.FastChunkBuffer;
import org.spongepowered.mod.util.gen.ObjectArrayMutableBiomeArea;

import java.util.List;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements Chunk {

    private Vector3i chunkPos;
    private ChunkCoordIntPair chunkCoordIntPair;

    @Shadow
    private net.minecraft.world.World worldObj;

    @Shadow
    public int xPosition;

    @Shadow
    public int zPosition;

    @Shadow
    private boolean isChunkLoaded;

    @Shadow
    private boolean isTerrainPopulated;

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"), remap = false)
    public void onConstructed(World world, int x, int z, CallbackInfo ci) {
        this.chunkPos = new Vector3i(x, 0, z);
        this.chunkCoordIntPair = new ChunkCoordIntPair(x, z);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ChunkPrimer;II)V", at = @At("RETURN"), remap = false)
    public void onNewlyGenerated(World world, ChunkPrimer primer, int chunkX, int chunkZ, CallbackInfo ci) {
        // The constructor with the ChunkPrimer in it is only used for newly
        // generated chunks, so we can call the generator populators here

        // Calling the generator populators here has the benefit that the chunk
        // can be modified before light is calculated and that implementations
        // of IChunkProvider provided by mods will very likely still work well

        List<GeneratorPopulator> populators = ((IMixinWorld) world).getGeneratorPopulators();
        if (!populators.isEmpty()) {
            FastChunkBuffer buffer = new FastChunkBuffer((net.minecraft.world.chunk.Chunk) (Object) this);
            BiomeGenBase[] biomeArray = world.getWorldChunkManager().getBiomeGenAt(null, chunkX * 16, chunkZ * 16, 16, 16, true);
            BiomeBuffer biomes = new ObjectArrayMutableBiomeArea(biomeArray, new Vector2i(chunkX * 16, chunkZ * 16), new Vector2i(16, 16));

            for (GeneratorPopulator populator : populators) {
                populator.populate((org.spongepowered.api.world.World) world, buffer, biomes);
            }
        }
    }

    @SideOnly(Side.SERVER)
    @Inject(method = "onChunkLoad()V", at = @At("RETURN"))
    public void onChunkLoadInject(CallbackInfo ci) {
        SpongeHooks.logChunkLoad(this.worldObj, this.chunkPos);
    }

    @SideOnly(Side.SERVER)
    @Inject(method = "onChunkUnload()V", at = @At("RETURN"))
    public void onChunkUnloadInject(CallbackInfo ci) {
        SpongeHooks.logChunkUnload(this.worldObj, this.chunkPos);
    }

    @Override
    public Vector3i getPosition() {
        return this.chunkPos;
    }

    @Override
    public boolean isLoaded() {
        return this.isChunkLoaded;
    }

    @Override
    public boolean isPopulated() {
        return this.isTerrainPopulated;
    }

    @Override
    public boolean loadChunk(boolean generate) {
        WorldServer worldserver = (WorldServer) this.worldObj;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(this.xPosition, this.zPosition) || generate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(this.xPosition, this.zPosition);
        }

        return chunk != null;
    }

    @Override
    public boolean unloadChunk() {
        if (ForgeChunkManager.getPersistentChunksFor(this.worldObj).containsKey(this.chunkCoordIntPair)) {
            return false;
        }

        if (this.worldObj.provider.canRespawnHere() && DimensionManager.shouldLoadSpawn(this.worldObj.provider.getDimensionId())) {
            if (this.worldObj.isSpawnChunk(this.xPosition, this.zPosition)) {
                return false;
            }
        }
        ((WorldServer) this.worldObj).theChunkProviderServer.dropChunk(this.xPosition, this.zPosition);
        return true;
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return (org.spongepowered.api.world.World) this.worldObj;
    }
}
