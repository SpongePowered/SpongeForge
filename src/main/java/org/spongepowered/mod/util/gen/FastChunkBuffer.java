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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.gen.ImmutableBlockBuffer;

import java.util.Arrays;

/**
 * Buffer backed by a single chunk.
 *
 * <p>Light values are not updated, nor is tile entity data. (Re-)initialize the
 * chunk after you are done.</p>
 *
 */
public final class FastChunkBuffer extends AbstractChunkBuffer {

    private final Chunk chunk;
    private final ExtendedBlockStorage[] sectionArray;

    public FastChunkBuffer(Chunk chunk) {
        super(chunk.xPosition, chunk.zPosition);
        this.chunk = chunk;
        this.sectionArray = chunk.getBlockStorageArray();
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        this.checkRange(x, y, z);

        int ySectionId = y >> 4;
        ExtendedBlockStorage section = this.sectionArray[ySectionId];
        if (section == null) {
            return BlockTypes.AIR.getDefaultState();
        }
        return (BlockState) section.get(x & 0xf, y & 0xf, z & 0xf);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return this.getBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void setBlock(Vector3i position, BlockState block) {
        this.setBlock(position.getX(), position.getY(), position.getZ(), block);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        this.checkRange(x, y, z);

        int ySectionId = y >> 4;
        ExtendedBlockStorage section = this.sectionArray[ySectionId];
        if (section == null && block.getType() != BlockTypes.AIR) {
            // Create new section first, so that we can store the block
            this.sectionArray[ySectionId] = section = createChunkSection(ySectionId);
        }
        section.set(x & 0xf, y & 0xf, z & 0xf, (IBlockState) block);
    }

    private ExtendedBlockStorage createChunkSection(int ySectionId) {
        boolean storeSkyLight = !this.chunk.getWorld().provider.getHasNoSky();
        return new ExtendedBlockStorage(ySectionId << 4, storeSkyLight);
    }

    @Override
    public void fill(BlockState block) {
        if (block.getType() == BlockTypes.AIR) {
            // Clear chunk by clearing chunk sections
            Arrays.fill(this.sectionArray, null);
            return;
        }

        // Fill chunk
        @SuppressWarnings("deprecation")
        char blockStateId = (char) Block.BLOCK_STATE_IDS.get(block);
        for (int ySectionId = 0; ySectionId < this.sectionArray.length; ySectionId++) {
            ExtendedBlockStorage section = this.sectionArray[ySectionId];
            if (section == null) {
                this.sectionArray[ySectionId] = section = createChunkSection(ySectionId);
            }
            char[] data = section.getData();
            Arrays.fill(data, blockStateId);
        }
    }

    @Override
    public ImmutableBlockBuffer getImmutableClone() {
        // TODO implement
        throw new UnsupportedOperationException("Not yet supported");
    }

}
