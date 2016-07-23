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
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.mod.interfaces.IMixinPlayerChunkMapEntry;

public class PlayerChunkRunnable implements Runnable {

    private PlayerChunkMap playerChunkMap;
    private PlayerChunkMapEntry playerChunkMapEntry;
    private EntityPlayerMP player;

    public PlayerChunkRunnable(EntityPlayerMP player, PlayerChunkMap playerChunkMap, PlayerChunkMapEntry playerChunkMapEntry) {
        this(playerChunkMap, playerChunkMapEntry);
        this.player = player;
    }

    public PlayerChunkRunnable(PlayerChunkMap playerChunkMap, PlayerChunkMapEntry playerChunkMapEntry) {
        this.playerChunkMap = playerChunkMap;
        this.playerChunkMapEntry = playerChunkMapEntry;
    }

    // Callback logic which is called after a chunk loads async or sync
    @Override
    public void run() {
        IMixinPlayerChunkMapEntry spongePlayerChunkMapEntry = (IMixinPlayerChunkMapEntry) this.playerChunkMapEntry;
        IMixinChunkProviderServer spongeChunkProviderServer = (IMixinChunkProviderServer) this.playerChunkMap.getWorldServer().getChunkProvider();
        Chunk chunk = spongeChunkProviderServer.getChunkIfLoaded(this.playerChunkMapEntry.pos.chunkXPos, this.playerChunkMapEntry.pos.chunkZPos);
        if (chunk != null) {
            // mark chunk used
            spongePlayerChunkMapEntry.setChunk(chunk);
            if (this.player != null) {
//                this.player.loadedChunks.add(this.playerChunkMapEntry.pos);
            }
            return;
        }
        spongePlayerChunkMapEntry.setLoaded(false);
    }

}
