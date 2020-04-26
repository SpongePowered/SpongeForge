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
package org.spongepowered.mod.mixin.core.server.management;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.mod.bridge.server.management.PlayerChunkMapEntryBridge_Forge;
import org.spongepowered.mod.entity.PlayerChunkRunnable;

@Mixin(value = PlayerChunkMapEntry.class, priority = 1001)
public class PlayerChunkMapEntryMixin_Forge implements PlayerChunkMapEntryBridge_Forge {

    @Shadow private Chunk chunk;
    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow(remap = false) private Runnable loadedRunnable;
    @Shadow(remap = false) private boolean loading;

    @Redirect(method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;",
            remap = false))
    private Chunk forgeImpl$OnConstruct(final ChunkProviderServer chunkProviderServer, final int chunkX, final int chunkZ, final Runnable runnable) {
        this.loading = true;
        this.loadedRunnable = new PlayerChunkRunnable(this.playerChunkMap, (PlayerChunkMapEntry) (Object) this);
        this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(chunkX, chunkZ, this.loadedRunnable);
        this.forgeImpl$markChunkUsed();
        return this.chunk;
    }

    // delay chunk unloads
    private void forgeImpl$markChunkUsed() {
        if (this.chunk == null) {
            return;
        }

        final ChunkBridge spongeChunk = (ChunkBridge) this.chunk;
        spongeChunk.bridge$setScheduledForUnload(-1);
        this.loading = false;
    }

    @Redirect(method = "providePlayerChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;provideChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk forgeImpl$UpdateLoadedChunk(final ChunkProviderServer chunkProviderServer, final int chunkX, final int chunkZ) {
        this.loading = true;
        if (!chunkProviderServer.chunkExists(chunkX, chunkZ)) {
            this.chunk = chunkProviderServer.provideChunk(chunkX, chunkZ);
        } else {
            // try to load chunk async
            this.chunk = chunkProviderServer.loadChunk(chunkX, chunkZ, this.loadedRunnable);
        }
        forgeImpl$markChunkUsed();
        return this.chunk;
    }

    /**
     * @author blood - July 24th, 2016
     * @reason Add chunk async load support
     */
    @Redirect(method = "providePlayerChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk forgeImpl$loadChunkAsync(final ChunkProviderServer chunkProviderServer, final int chunkX, final int chunkZ) {
        // try to load chunk async
        this.loading = true;
        this.chunk = chunkProviderServer.loadChunk(chunkX, chunkZ, this.loadedRunnable);
        this.forgeImpl$markChunkUsed();
        return this.chunk;
    }

    // Called by PlayerChunkRunnable after a chunk is loaded
    @Override
    public void forgeBridge$setChunk(final Chunk chunk) {
        this.chunk = chunk;
        this.forgeImpl$markChunkUsed();
    }

    @Override
    public void forgeBridge$setLoading(final boolean loading) {
        this.loading = loading;
    }
}
