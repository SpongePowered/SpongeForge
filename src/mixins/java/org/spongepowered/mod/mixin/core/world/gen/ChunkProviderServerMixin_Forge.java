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
package org.spongepowered.mod.mixin.core.world.gen;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.mixin.core.world.gen.ChunkProviderServerMixin;

@Mixin(value = ChunkProviderServer.class, priority = 1001)
public abstract class ChunkProviderServerMixin_Forge implements ChunkProviderServerBridge {

    @Shadow @Final public WorldServer world;
    @Shadow @Final public Long2ObjectMap<Chunk> loadedChunks;
    @Shadow public abstract Chunk loadChunk(int x, int z);
    @Shadow protected abstract void saveChunkExtraData(Chunk chunkIn);

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", shift = Shift.AFTER, remap = false))
    private void forge$RemoveForgePersistingChunkCheck(CallbackInfoReturnable<Boolean> cir) {
        // Remove forge's persistent chunk check since we cache it in the chunk. Only unload the world if we're not the overworld and we're told that
        // we are not to keep spawn loaded (which is our flag to keep the world loaded)
        // TODO Consider splitting this into two flags: keep-spawn-loaded and keep-world-loaded
        if (this.loadedChunks.size() == 0 && ((WorldServerBridge) this.world).bridge$getDimensionId() != 0 && !SpongeImplHooks.shouldKeepSpawnLoaded(this
                .world.provider.getDimensionType(), ((WorldServerBridge) this.world).bridge$getDimensionId())) {
            net.minecraftforge.common.DimensionManager.unloadWorld(this.world.provider.getDimension());
        }
    }
    @Redirect(method = "provideChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/IChunkGenerator;generateChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk impl$generateChunkWithSponge(final IChunkGenerator generator, final int x, final int z) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return generator.generateChunk(x, z);
        }
        return ((WorldServerBridge) this.world).bridge$getSpongeGenerator().generateChunk(x, z);
    }

    @Redirect(method = "provideChunk",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/Chunk;populate(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/gen/IChunkGenerator;)V"))
    private void impl$populateChunkThroughSponge(final Chunk chunk, final IChunkProvider chunkProvider, final IChunkGenerator chunkGenrator) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            chunk.populate(chunkProvider, chunkGenrator);
            return;
        }
        chunk.populate(chunkProvider, ((WorldServerBridge) this.world).bridge$getSpongeGenerator());
    }

    /**
     * @author Aaron1011 - January 28, 2017
     * @reason In SpongeVanilla, it's safe to run this method instead of loadChunk,
     * since the only modification made is the removal of a check we've already done.
     *
     * However, loadChunk is completely different in Forge. therefore, we need to delegate to
     * the original method to ensure that async loading gets handled properly (Forge's code properly
     * handles a concurrent asychronous load of the same chunk).
     *
     * @see ChunkProviderServerMixin#bridge$loadChunkForce(int, int)
     */
    @Override
    public Chunk bridge$loadChunkForce(int x, int z) {
        return this.loadChunk(x, z);
    }
}
