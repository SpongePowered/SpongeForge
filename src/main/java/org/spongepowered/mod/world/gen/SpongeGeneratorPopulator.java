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
package org.spongepowered.mod.world.gen;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.gen.BiomeBuffer;
import org.spongepowered.api.util.gen.MutableBlockBuffer;
import org.spongepowered.api.world.gen.GeneratorPopulator;

/**
 * Generator populator that wraps a Minecraft {@link IChunkProvider}.
 *
 */
public final class SpongeGeneratorPopulator implements GeneratorPopulator {

    private final IChunkProvider chunkGenerator;
    private final World world;

    /**
     * Gets the {@link GeneratorPopulator} from the given {@link IChunkProvider}
     * . If the chunk provider wraps a {@link GeneratorPopulator}, that
     * populator is returned, otherwise the chunk provider is wrapped.
     *
     * @param world The world the chunk generator is bound to.
     * @param chunkGenerator The chunk generator.
     * @return The generator populator.
     */
    public static GeneratorPopulator of(World world, IChunkProvider chunkGenerator) {
        if (chunkGenerator instanceof CustomChunkProviderGenerate) {
            return ((CustomChunkProviderGenerate) chunkGenerator).generatorPopulator;
        }
        return new SpongeGeneratorPopulator(world, chunkGenerator);
    }

    private SpongeGeneratorPopulator(World world, IChunkProvider chunkGenerator) {
        this.world = Preconditions.checkNotNull(world);
        this.chunkGenerator = Preconditions.checkNotNull(chunkGenerator);
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, MutableBlockBuffer buffer, BiomeBuffer biomes) {

        // Empty the buffer
        buffer.fill(BlockTypes.AIR.getDefaultState());

        // The block buffer can be of any size. We generate all chunks that
        // have at least part of the chunk in the given area, and copy the
        // needed blocks into the buffer
        Vector3i min = buffer.getBlockMin();
        Vector3i max = buffer.getBlockMax();
        int minChunkX = (int) Math.floor(min.getX() / 16.0);
        int minChunkZ = (int) Math.floor(min.getZ() / 16.0);
        int maxChunkX = (int) Math.floor(max.getX() / 16.0);
        int maxChunkZ = (int) Math.floor(max.getZ() / 16.0);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk generated = this.chunkGenerator.provideChunk(chunkX, chunkZ);
                placeChunkInBuffer(generated, buffer, chunkX, chunkZ);
            }
        }
    }

    private void placeChunkInBuffer(Chunk chunk, MutableBlockBuffer buffer, int chunkX, int chunkZ) {

        // Calculate bounds
        int xOffset = chunkX * 16;
        int zOffset = chunkZ * 16;
        Vector3i minBound = buffer.getBlockMin();
        Vector3i maxBound = buffer.getBlockMax();
        int xInChunkStart = Math.max(0, minBound.getX() - xOffset);
        int yStart = Math.max(0, minBound.getY());
        int zInChunkStart = Math.max(0, minBound.getZ() - zOffset);
        int xInChunkEnd = Math.min(15, maxBound.getX() - xOffset);
        int yEnd = Math.min(255, maxBound.getY());
        int zInChunkEnd = Math.min(15, maxBound.getZ() - zOffset);

        // Copy the right blocks in
        ExtendedBlockStorage[] blockStorage = chunk.getBlockStorageArray();
        for (int i = 0; i < blockStorage.length; i++) {
            ExtendedBlockStorage miniChunk = blockStorage[i];
            if (miniChunk == null) {
                continue;
            }

            int yOffset = miniChunk.getYLocation();
            int yInChunkStart = Math.max(yOffset, yStart);
            int yInChunkEnd = Math.min(yOffset + 15, yEnd);
            for (int xInChunk = xInChunkStart; xInChunk <= xInChunkEnd; xInChunk++) {
                for (int yInChunk = yInChunkStart; yInChunk <= yInChunkEnd; yInChunk++) {
                    for (int zInChunk = zInChunkStart; zInChunk <= zInChunkEnd; zInChunk++) {
                        buffer.setBlock(xOffset + xInChunk, yOffset + yInChunk, zOffset + zInChunk,
                                (BlockState) miniChunk.get(xInChunk, yInChunk, zInChunk));
                    }
                }
            }
        }
    }

    /**
     * Gets the chunk provider, if the target world matches the world this chunk
     * provider was bound to.
     *
     * @param targetWorld The target world.
     * @return The chunk provider.
     * @throws IllegalArgumentException If the target world is not the world
     *         this chunk provider is bound to.
     */
    IChunkProvider getHandle(World targetWorld) {
        if (!this.world.equals(targetWorld)) {
            throw new IllegalArgumentException("Cannot reassign internal generator from world "
                    + getWorldName(this.world) + " to world " + getWorldName(targetWorld));
        }
        return this.chunkGenerator;
    }

    private static String getWorldName(World world) {
        return ((org.spongepowered.api.world.World) world).getName();
    }

}
