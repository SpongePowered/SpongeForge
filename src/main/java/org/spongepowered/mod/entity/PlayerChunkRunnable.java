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
package org.spongepowered.mod.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.server.management.PlayerManager.PlayerInstance;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.mod.interfaces.IMixinPlayerInstance;

public class PlayerChunkRunnable implements Runnable {

    private PlayerManager playerManager;
    private PlayerInstance playerInstance;
    private EntityPlayerMP player;

    public PlayerChunkRunnable(EntityPlayerMP player, PlayerManager playerManager, PlayerInstance playerInstance) {
        this(playerManager, playerInstance);
        this.player = player;
    }

    public PlayerChunkRunnable(PlayerManager playerManager, PlayerInstance playerInstance) {
        this.playerManager = playerManager;
        this.playerInstance = playerInstance;
    }

    // Callback logic which is called after a chunk loads async or sync
    @Override
    public void run() {
        IMixinPlayerInstance spongePlayerInstance = (IMixinPlayerInstance) this.playerInstance;
        IMixinChunkProviderServer spongeChunkProviderServer = (IMixinChunkProviderServer) this.playerManager.getWorldServer().theChunkProviderServer;
        Chunk chunk = spongeChunkProviderServer.getChunkIfLoaded(this.playerInstance.chunkCoords.chunkXPos, this.playerInstance.chunkCoords.chunkZPos);
        if (chunk != null) {
            // mark chunk used
            spongePlayerInstance.setChunk(chunk);
            if (this.player != null) {
                this.player.loadedChunks.add(this.playerInstance.chunkCoords);
            }
            return;
        }
        spongePlayerInstance.setLoaded(false);
    }

}
