/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 SpongePowered <http://spongepowered.org/>
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
package org.spongepowered.mod.world;

import org.spongepowered.api.block.Block;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.mod.block.SpongeBlock;

public class SpongeChunk implements Chunk {

    private net.minecraft.world.chunk.Chunk nmschunk;

    public SpongeChunk(net.minecraft.world.chunk.Chunk chunk){
        this.nmschunk = chunk;
    }

    @Override
    public int getX() {
        return nmschunk.getChunkCoordIntPair().getCenterXPos();
    }

    @Override
    public int getZ() {
        return nmschunk.getChunkCoordIntPair().getCenterZPosition();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return new SpongeBlock(nmschunk.getBlock(x, y, z));
    }
}
