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

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.world.IMixinWorld;

@Mixin(value = ChunkProviderServer.class, priority = 1001)
public abstract class MixinChunkProviderServer {

    private IMixinWorld spongeWorld;

    @Shadow public WorldServer worldObj;
    @Shadow public IChunkProvider serverChunkGenerator;;
    @Shadow public abstract Chunk provideChunk(int x, int z);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(WorldServer world, IChunkLoader chunkLoader, IChunkProvider serverChunkGenerator, CallbackInfo ci) {
        this.spongeWorld = (IMixinWorld) world;
    }

    @Inject(method = "originalLoadChunk", at = @At(value = "INVOKE", args = "log=true", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    public void onOriginalLoadChunkStart(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir) {
        this.spongeWorld.getTimingsHandler().syncChunkLoadDataTimer.startTiming();
    }

    @Inject(method = "originalLoadChunk", at = @At(value = "INVOKE", args = "log=true", target = "Lnet/minecraft/world/chunk/Chunk;populateChunk(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/chunk/IChunkProvider;II)V"))
    public void onOriginalLoadChunkEnd(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir) {
        this.spongeWorld.getTimingsHandler().syncChunkLoadDataTimer.stopTiming();
    }

    /**
     * @author blood - April 26th, 2016
     * @reason Control terrain gen flag here to avoid leaking block captures during FML's
     * 'GameRegistry.generateWorld' due to recursive calls.
     *     
     * @param chunkProvider The chunk provider
     * @param x The x coordinate of chunk
     * @param z The z coordinate of chunk
     */
    @Overwrite
    public void populate(IChunkProvider chunkProvider, int x, int z) {
        Chunk chunk = this.provideChunk(x, z);

        if (!chunk.isTerrainPopulated()) {
            chunk.func_150809_p();

            if (this.serverChunkGenerator != null) {
                boolean capturingTerrain = this.spongeWorld.getCauseTracker().isCapturingTerrainGen();
                this.spongeWorld.getCauseTracker().setCapturingTerrainGen(true);
                this.serverChunkGenerator.populate(chunkProvider, x, z);
                net.minecraftforge.fml.common.registry.GameRegistry.generateWorld(x, z, worldObj, serverChunkGenerator, chunkProvider);
                chunk.setChunkModified();
                this.spongeWorld.getCauseTracker().setCapturingTerrainGen(capturingTerrain);
            }
        }
    }
}
