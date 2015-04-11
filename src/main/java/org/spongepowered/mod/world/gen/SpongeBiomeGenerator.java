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

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import org.spongepowered.api.util.gen.MutableBiomeArea;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.BiomeGenerator;

/**
 * Simple wrapper for {@link WorldChunkManager} that implements
 * {@link BiomeGenerator}.
 *
 * <p>This class does the opposite of {@link CustomWorldChunkManager}, that
 * wraps a {@link BiomeGenerator} into a {@link WorldChunkManager}.</p>
 */
public final class SpongeBiomeGenerator implements BiomeGenerator {

    final WorldChunkManager worldChunkManager;

    public static BiomeGenerator of(WorldChunkManager worldChunkManager) {
        if (worldChunkManager instanceof CustomWorldChunkManager) {
            return ((CustomWorldChunkManager) worldChunkManager).biomeGenerator;
        }
        return new SpongeBiomeGenerator(worldChunkManager);
    }

    private SpongeBiomeGenerator(WorldChunkManager worldChunkManager) {
        this.worldChunkManager = Preconditions.checkNotNull(worldChunkManager);
    }

    @Override
    public void generateBiomes(MutableBiomeArea buffer) {
        Vector2i min = buffer.getBiomeMin();
        Vector2i size = buffer.getBiomeSize();
        int xStart = min.getX();
        int zStart = min.getY();
        int xSize = size.getX();
        int zSize = size.getY();

        BiomeGenBase[] biomes = this.worldChunkManager.getBiomeGenAt(null, xStart, zStart, xSize, zSize, true);

        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < zSize; j++) {
                buffer.setBiome(xStart + i, zStart + j, (BiomeType) biomes[i | j << 4]);
            }
        }
    }

}
