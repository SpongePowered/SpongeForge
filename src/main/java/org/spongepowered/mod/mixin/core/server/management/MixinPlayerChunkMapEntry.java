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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.mod.entity.PlayerChunkRunnable;
import org.spongepowered.mod.interfaces.IMixinPlayerChunkMapEntry;

import java.util.List;

@Mixin(value = PlayerChunkMapEntry.class, priority = 1001)
public class MixinPlayerChunkMapEntry implements IMixinPlayerChunkMapEntry {

    private Chunk chunk;
    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow @Final public List<EntityPlayerMP> players;
    @Shadow @Final public ChunkPos pos;
    @Shadow public boolean sentToPlayers;
    @Shadow(remap = false) private Runnable loadedRunnable;
    @Shadow(remap = false) private boolean loading;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;", remap = false))
    public Chunk onLoadChunk(ChunkProviderServer chunkProviderServer, int chunkX, int chunkZ, Runnable runnable) {
        this.loading = true;
        this.loadedRunnable = new PlayerChunkRunnable(this.playerChunkMap, (PlayerChunkMapEntry) (Object) this);
        this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(chunkX, chunkZ, this.loadedRunnable);
        return this.chunk;
    }

    // delay chunk unloads
    public final void markChunkUsed() {
        if (this.chunk == null) {
            return;
        }

        IMixinChunk spongeChunk = (IMixinChunk) this.chunk;
        spongeChunk.setScheduledForUnload(null);
        this.loading = false;
    }

    /**
     * @author blood - July 24th, 2016
     * @reason Add chunk async load support
     */
    @Overwrite
    public boolean providePlayerChunk(boolean canGenerate) {
        if (this.loading) {
            return false;
        }
        if (this.chunk != null) {
            return true;
        } else {
            // Sponge start
            if (!this.playerChunkMap.getWorldServer().getChunkProvider().chunkExists(this.pos.chunkXPos, this.pos.chunkZPos) && canGenerate) {
                this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().provideChunk(this.pos.chunkXPos, this.pos.chunkZPos);
            } else if (!this.loading) {
                // try to load chunk async
                this.loading = true;
                this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable);
                this.markChunkUsed();
            }
            // Sponge end
            return this.chunk != null;
        }
    }

    // Called by PlayerChunkRunnable after a chunk is loaded
    @Override
    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        this.markChunkUsed();
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
