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

package org.spongepowered.mod.util.gen;

import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.util.gen.ImmutableBiomeArea;

import java.util.Arrays;

/**
 * Immutable biome area, backed by a byte array. The array passed to the
 * constructor is copied to ensure that the instance is immutable.
 */
public class SpongeImmutableBiomeArea extends AbstractBiomeArea implements ImmutableBiomeArea {

    private static byte[] getIds(BiomeGenBase[] biomes) {
        byte[] ids = new byte[biomes.length];
        for (int i = 0; i < biomes.length; i++) {
            BiomeGenBase biome = biomes[i];
            if (biome != null) {
                ids[i] = (byte) biomes[i].biomeID;
            }
        }
        return ids;
    }

    public SpongeImmutableBiomeArea(BiomeGenBase[] biomes, int startX, int startZ, int sizeX, int sizeZ) {
        super(getIds(biomes), startX, startZ, sizeX, sizeZ);
    }

    public SpongeImmutableBiomeArea(byte[] biomes, int startX, int startZ, int sizeX, int sizeZ) {
        super(Arrays.copyOf(biomes, biomes.length), startX, startZ, sizeX, sizeZ);
    }

}
