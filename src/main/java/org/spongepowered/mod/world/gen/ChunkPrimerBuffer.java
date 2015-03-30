/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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

package org.spongepowered.mod.world.gen;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.gen.ImmutableBlockBuffer;
import org.spongepowered.api.util.gen.MutableBlockBuffer;

import java.util.Arrays;

/**
 * Makes a {@link ChunkPrimer} useable as a {@link MutableBlockBuffer}.
 *
 */
final class ChunkPrimerBuffer implements MutableBlockBuffer {

    private final ChunkPrimer chunkPrimer;
    private final int chunkX;
    private final int chunkZ;

    public ChunkPrimerBuffer(ChunkPrimer chunkPrimer, int chunkX, int chunkZ) {
        this.chunkPrimer = chunkPrimer;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    private void checkRange(int x, int y, int z) {
        if ((x >> 4) != this.chunkX || (z >> 4) != this.chunkZ || (y >> 8) != 0) {
            throw new IndexOutOfBoundsException("Outside chunk: " + new Vector3i(x, y, z));
        }
    }

    @Override
    public void fill(BlockState block) {
        @SuppressWarnings("deprecation")
        short stateId = (short) Block.BLOCK_STATE_IDS.get(block);

        Arrays.fill(this.chunkPrimer.data, stateId);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return (BlockState) this.chunkPrimer.getBlockState(x, y, z);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return getBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public ImmutableBlockBuffer getImmutableClone() {
        // TODO implement
        throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    public Vector3i getMaxBound() {
        return getMinBound().add(getSize());
    }

    @Override
    public Vector3i getMinBound() {
        return new Vector3i(this.chunkX * 16, 0, this.chunkZ * 16);
    }

    @Override
    public Vector3i getSize() {
        return new Vector3i(16, 256, 16);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkRange(x, y, z);
        this.chunkPrimer.setBlockState(x & 0xf, y, z & 0xF, (IBlockState) block);
    }

    @Override
    public void setBlock(Vector3i position, BlockState block) {
        setBlock(position.getX(), position.getY(), position.getZ(), block);
    }

    @Override
    public void setHorizontalLayer(int startY, int height, BlockState block) {
        IBlockState blockState = (IBlockState) block;
        int endY = startY + height;

        for (int xInChunk = 0; xInChunk < 16; xInChunk++) {
            for (int zInChunk = 0; zInChunk < 16; zInChunk++) {
                for (int yInChunk = startY; yInChunk < endY; yInChunk++) {
                    this.chunkPrimer.setBlockState(xInChunk, yInChunk, zInChunk, blockState);
                }
            }
        }
    }

}
