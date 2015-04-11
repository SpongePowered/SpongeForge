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
package org.spongepowered.mod.util.gen;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.gen.MutableBlockBuffer;

/**
 * Base class for block buffers that are exactly one chunk in size.
 *
 */
abstract class AbstractChunkBuffer implements MutableBlockBuffer {

    private static final Vector3i CHUNK_SIZE = new Vector3i(16, 256, 16);

    private final int chunkX;
    private final int chunkZ;

    private final Vector3i maxBlock;
    private final Vector3i minBlock;

    public AbstractChunkBuffer(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        this.minBlock = new Vector3i(chunkX * CHUNK_SIZE.getX(), 0, chunkZ * CHUNK_SIZE.getZ());
        this.maxBlock = this.minBlock.add(CHUNK_SIZE).sub(Vector3i.ONE);
    }

    protected void checkRange(int x, int y, int z) {
        if ((x >> 4) != this.chunkX || (z >> 4) != this.chunkZ || (y >> 8) != 0) {
            throw new IndexOutOfBoundsException("Outside chunk: " + new Vector3i(x, y, z)
                    + " is outside chunk (" + this.chunkX + "," + this.chunkZ
                    + "), containing blocks " + this.minBlock + " to " + this.maxBlock);
        }
    }

    @Override
    public Vector3i getBlockMax() {
        return this.maxBlock;
    }

    @Override
    public Vector3i getBlockMin() {
        return this.minBlock;
    }

    @Override
    public Vector3i getBlockSize() {
        return CHUNK_SIZE;
    }

    @Override
    public void setHorizontalLayer(int startY, int height, BlockState block) {
        int startX = this.chunkX << 4;
        int startZ = this.chunkZ << 4;

        int endY = startY + height;

        for (int xInChunk = 0; xInChunk < 16; xInChunk++) {
            for (int zInChunk = 0; zInChunk < 16; zInChunk++) {
                for (int yInChunk = startY; yInChunk < endY; yInChunk++) {
                    this.setBlock(xInChunk | startX, yInChunk, zInChunk | startZ, block);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(min = " + this.getBlockMin() + ", max = " + this.getBlockMax() + ")";
    }

}
