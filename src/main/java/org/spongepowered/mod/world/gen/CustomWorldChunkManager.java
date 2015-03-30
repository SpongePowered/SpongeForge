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

import com.google.common.base.Preconditions;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.IntCache;
import org.spongepowered.api.util.gen.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.mod.util.gen.SpongeMutableBiomeArea;

import java.util.List;
import java.util.Random;

/**
 * Implementation of {@link WorldChunkManager} (bad name for the class that is
 * responsible for where the biomes appear) based on a {@link BiomeGenerator}.
 *
 * <p>This class does the opposite of {@link SpongeBiomeGenerator}, that class
 * wraps a world chunk manager so that it is usable as a {@link BiomeGenerator}
 * .</p>
 */
public final class CustomWorldChunkManager extends WorldChunkManager {

    private static final int CACHED_AREA_SIZE = 40;

    private final SpongeMutableBiomeArea areaForGeneration = new SpongeMutableBiomeArea(0, 0, CACHED_AREA_SIZE, CACHED_AREA_SIZE);
    final BiomeGenerator biomeGenerator;

    /**
     * Gets a world chunk manager based on the given biome generator.
     *
     * @param biomeGenerator The biome generator.
     * @return The world chunk manager.
     */
    public static WorldChunkManager of(BiomeGenerator biomeGenerator) {
        if (biomeGenerator instanceof SpongeBiomeGenerator) {
            // Biome generator set to a wrapper
            return ((SpongeBiomeGenerator) biomeGenerator).worldChunkManager;
        }
        // Biome generator set to some custom implementation
        return new CustomWorldChunkManager(biomeGenerator);
    }

    private CustomWorldChunkManager(BiomeGenerator biomeGenerator) {
        this.biomeGenerator = Preconditions.checkNotNull(biomeGenerator);

        if (this.biomeGenerator instanceof SpongeBiomeGenerator) {
            throw new AssertionError(getClass() + " can only wrap custom biome generators, "
                    + SpongeBiomeGenerator.class + " is not a custom biome generator");
        }
    }

    /**
     * Return a list of biomes for the specified blocks. Args: listToReuse, x,
     * y, width, length, cacheFlag (if false, don't check biomeCache to avoid
     * infinite loop in BiomeCacheBlock)
     *
     * @param cacheFlag If false, don't check biomeCache to avoid infinite loop
     *        in BiomeCacheBlock
     */
    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.loadBlockGeneratorData(listToReuse, x, z, width, length);
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomeArray, int xStart, int zStart, int xSize, int zSize) {
        // "Biomes for generation" are a 4x downscaled (on both the x and z
        // axis) version of the normal biomes
        // The easiest way to obtain downscaled biomes is to obtain the normal
        // scale biomes and then downscale them

        if (biomeArray == null || biomeArray.length < xSize * zSize) {
            biomeArray = new BiomeGenBase[xSize * zSize];
        }

        // Transform to normal scale
        int xStartBlock = xStart * 4;
        int zStartBlock = zStart * 4;
        int xSizeBlock = xSize * 4;
        int zSizeBlock = zSize * 4;

        // Get biomes
        SpongeMutableBiomeArea buffer = getBiomeBuffer(xStartBlock, zStartBlock, xSizeBlock, zSizeBlock);
        this.biomeGenerator.getBiomesForArea(buffer);

        // Downscale
        byte[] biomesForBlocks = buffer.detach();
        BiomeGenBase[] biomeById = BiomeGenBase.getBiomeGenArray();
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; i < zSize; j++) {
                BiomeGenBase biome = biomeById[biomesForBlocks[i >> 2 | j << 2] & 0xff];
                biomeArray[i | j << 4] = (biome == null ? BiomeGenBase.ocean : biome);
                // [i >> 2 | j << 2] is equal to [(i * 4) | (j * 4) << 4]
            }
        }

        return biomeArray;
    }

    private SpongeMutableBiomeArea getBiomeBuffer(int xStart, int zStart, int xSize, int zSize) {
        if (xSize == CACHED_AREA_SIZE && zSize == CACHED_AREA_SIZE) {
            this.areaForGeneration.reuse(xStart, zStart);
            return this.areaForGeneration;
        } else {
            return new SpongeMutableBiomeArea(xStart, zStart, xSize, zSize);
        }
    }

    @Override
    public float[] getRainfall(float[] rainfallArray, int x, int z, int xSize, int zSize) {
        if (rainfallArray == null || rainfallArray.length < xSize * zSize) {
            rainfallArray = new float[xSize * zSize];
        }

        SpongeMutableBiomeArea buffer = getBiomeBuffer(x, z, xSize, zSize);
        this.biomeGenerator.getBiomesForArea(buffer);
        byte[] biomes = buffer.detach();
        BiomeGenBase[] biomeById = BiomeGenBase.getBiomeGenArray();

        for (int i = 0; i < xSize * zSize; i++) {
            BiomeGenBase biome = biomeById[biomes[i] & 0xff];
            float rainfall = (biome == null) ? 1.0F : (biome.getIntRainfall() / 65536.0F);

            if (rainfall > 1.0F) {
                rainfall = 1.0F;
            }

            rainfallArray[i] = rainfall;
        }

        return rainfallArray;
    }

    @Override
    public BlockPos findBiomePosition(int xCenter, int zCenter, int range, @SuppressWarnings("rawtypes") List searchingFor, Random random) {
        IntCache.resetIntCache();
        int xStartSegment = xCenter - range >> 2;
        int zStartSegment = zCenter - range >> 2;
        int xMaxSegment = xCenter + range >> 2;
        int zMaxSegment = zCenter + range >> 2;
        int xSizeSegments = xMaxSegment - xStartSegment + 1;
        int zSizeSegments = zMaxSegment - zStartSegment + 1;

        SpongeMutableBiomeArea buffer = getBiomeBuffer(xStartSegment << 2, zStartSegment << 2, xSizeSegments << 2, zSizeSegments << 2);
        this.biomeGenerator.getBiomesForArea(buffer);
        byte[] biomes = buffer.detach();

        BlockPos blockpos = null;
        int foundPositions = 0;

        for (int i = 0; i < xSizeSegments * zSizeSegments; ++i) {
            BiomeGenBase foundBiome = BiomeGenBase.getBiome(biomes[i << 2] & 0xff);

            if (searchingFor.contains(foundBiome) && (blockpos == null || random.nextInt(foundPositions + 1) == 0)) {
                int x = xStartSegment + i % xSizeSegments << 2;
                int z = zStartSegment + i / xSizeSegments << 2;
                blockpos = new BlockPos(x, 0, z);
                foundPositions++;
            }
        }

        return blockpos;
    }

    @Override
    public boolean areBiomesViable(int xCenter, int zCenter, int range, @SuppressWarnings("rawtypes") List searchingForBiomes) {
        IntCache.resetIntCache();
        int xStartSegment = xCenter - range >> 2;
        int zStartSegment = zCenter - range >> 2;
        int xMaxSegment = xCenter + range >> 2;
        int zMaxSegment = zCenter + range >> 2;
        int xSizeSegments = xMaxSegment - xStartSegment + 1;
        int zSizeSegments = zMaxSegment - zStartSegment + 1;

        SpongeMutableBiomeArea buffer = getBiomeBuffer(xStartSegment << 2, zStartSegment << 2, xSizeSegments << 2, zSizeSegments << 2);
        this.biomeGenerator.getBiomesForArea(buffer);
        byte[] biomes = buffer.detach();

        for (int i = 0; i < xSizeSegments * zSizeSegments; ++i) {
            BiomeGenBase biomegenbase = BiomeGenBase.getBiome(biomes[i << 2] & 0xff);

            if (!searchingForBiomes.contains(biomegenbase)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomeArray, int startX, int startZ, int sizeX, int sizeZ) {
        if (biomeArray == null || biomeArray.length < sizeX * sizeZ) {
            biomeArray = new BiomeGenBase[sizeX * sizeZ];
        }

        MutableBiomeArea biomeArea = new BiomePrimerBuffer(biomeArray, startX, startZ, sizeX, sizeZ);
        this.biomeGenerator.getBiomesForArea(biomeArea);

        return biomeArray;
    }

}
