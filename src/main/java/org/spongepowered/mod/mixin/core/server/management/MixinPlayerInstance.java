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
import net.minecraft.server.management.PlayerManager;
import net.minecraft.server.management.PlayerManager.PlayerInstance;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.mod.entity.PlayerChunkRunnable;
import org.spongepowered.mod.interfaces.IMixinPlayerInstance;

import java.util.List;

@Mixin(value = PlayerInstance.class, priority = 1001)
public class MixinPlayerInstance implements IMixinPlayerInstance {

    private PlayerInstance playerInstance = (PlayerInstance)(Object) this;
    private Chunk chunk;
    @Shadow(aliases = "this$0") @Final private PlayerManager playerManager;
    @Shadow @Final public List<EntityPlayerMP> playersWatchingChunk;
    @Shadow @Final public ChunkCoordIntPair chunkCoords;
    @Shadow @Final private java.util.HashMap<EntityPlayerMP, Runnable> players;
    @Shadow public long previousWorldTime;
    @Shadow private Runnable loadedRunnable;
    @Shadow private boolean loaded;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;", remap = false))
    public Chunk onLoadChunk(ChunkProviderServer chunkProviderServer, int chunkX, int chunkZ, Runnable runnable) {
        this.loadedRunnable = new PlayerChunkRunnable(this.playerManager, this.playerInstance);
        this.chunk = playerManager.getWorldServer().theChunkProviderServer.loadChunk(chunkX, chunkZ, this.loadedRunnable);
        return this.chunk;
    }

    // delay chunk unloads
    public final void markChunkUsed() {
        if (this.chunk == null) {
            return;
        }

        IMixinChunk spongeChunk = (IMixinChunk) this.chunk;
        spongeChunk.setScheduledForUnload(null);
        this.loaded = true;
    }

    @Inject(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerManager$PlayerInstance$2;<init>(Lnet/minecraft/server/management/PlayerManager$PlayerInstance;Lnet/minecraft/entity/player/EntityPlayerMP;)V", remap = false), cancellable = true)
    public void onAddPlayer(EntityPlayerMP player, CallbackInfo ci) {
        Runnable playerRunnable = new PlayerChunkRunnable(player, this.playerManager, this.playerInstance);
        this.chunk = this.playerManager.getWorldServer().theChunkProviderServer.loadChunk(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos, playerRunnable);
        this.players.put(player, playerRunnable);
        ci.cancel();
    }

    @Redirect(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;dropChunk(II)V"))
    public void onPlayerDropChunk(ChunkProviderServer chunkProviderServer, int chunkX, int chunkZ) {
        // We remove the ability for a PlayerInstance to queue chunks for unload to prevent chunk thrashing
        // where the same chunks repeatedly unload and load. This is caused by a player moving in and out of the same chunks.
        // Instead, the Chunk GC will now be responsible for going through loaded chunks and queuing any chunk where no player
        // is within view distance or a spawn chunk is force loaded. However, if the Chunk GC is disabled then we will fall back to vanilla
        // and queue the chunk to be unloaded.
        // -- blood

        if (((IMixinWorldServer) chunkProviderServer.worldObj).getChunkGCTickInterval() <= 0 || ((IMixinWorldServer) chunkProviderServer.worldObj).getChunkUnloadDelay() <= 0) {
            chunkProviderServer.dropChunk(chunkX, chunkZ);
        } else {
            if (this.chunk != null) {
                ((IMixinChunk) this.chunk).setScheduledForUnload(System.currentTimeMillis());
            }
        }
    }

    // Called by PlayerChunkRunnable after a chunk is loaded
    @Override
    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        this.markChunkUsed();
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
