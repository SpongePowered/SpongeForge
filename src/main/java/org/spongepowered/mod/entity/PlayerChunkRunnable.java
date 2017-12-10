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

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.mod.interfaces.IMixinPlayerChunkMapEntry;

public class PlayerChunkRunnable implements Runnable {

    private PlayerChunkMap playerChunkMap;
    private PlayerChunkMapEntry playerChunkMapEntry;

    public PlayerChunkRunnable(PlayerChunkMap playerChunkMap, PlayerChunkMapEntry playerChunkMapEntry) {
        this.playerChunkMap = playerChunkMap;
        this.playerChunkMapEntry = playerChunkMapEntry;
    }

    // Callback logic which is called after a chunk loads async or sync
    @Override
    public void run() {
        IMixinPlayerChunkMapEntry spongePlayerChunkMapEntry = (IMixinPlayerChunkMapEntry) this.playerChunkMapEntry;
        Chunk chunk = this.playerChunkMap.getWorldServer().getChunkProvider().getLoadedChunk(this.playerChunkMapEntry.pos.x,
                this.playerChunkMapEntry.pos.z);
        if (chunk != null) {
            spongePlayerChunkMapEntry.setChunk(chunk);
            return;
        }
        // Since we weren't able to load the chunk async, set loading to false to allow the PlayerChunkMap tick to load
        spongePlayerChunkMapEntry.setLoading(false);
    }

}
