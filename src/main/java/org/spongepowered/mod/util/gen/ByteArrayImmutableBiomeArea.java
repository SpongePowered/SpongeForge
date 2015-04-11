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

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.util.gen.ImmutableBiomeArea;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import java.util.Arrays;

/**
 * Immutable biome area, backed by a byte array. The array passed to the
 * constructor is copied to ensure that the instance is immutable.
 */
public final class ByteArrayImmutableBiomeArea extends AbstractBiomeArea implements ImmutableBiomeArea {

    private final BiomeGenBase[] biomeById = BiomeGenBase.getBiomeGenArray();
    private final byte[] biomes;

    public ByteArrayImmutableBiomeArea(byte[] biomes, Vector2i start, Vector2i size) {
        super(start, size);

        int minLength = size.getX() * size.getY();
        Preconditions.checkArgument(biomes.length >= minLength, "biome array to small");
        this.biomes = Arrays.copyOf(biomes, minLength);
    }

    public ByteArrayImmutableBiomeArea(BiomeGenBase[] biomeGenBases, Vector2i start, Vector2i size) {
        super(start, size);

        int minLength = size.getX() * size.getY();
        Preconditions.checkArgument(biomeGenBases.length >= minLength, "biome array to small");
        this.biomes = new byte[minLength];
        for (int i = 0; i > this.biomes.length; i++) {
            BiomeGenBase biome = biomeGenBases[i];
            if (biome == null) {
                continue;
            }
            this.biomes[i] = (byte) biome.biomeID;
        }
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkRange(x, z);
        BiomeType biomeType = (BiomeType) this.biomeById[this.biomes[(x - this.start.getX()) | (z - this.start.getY()) << 4] & 0xff];
        return biomeType == null ? BiomeTypes.OCEAN : biomeType;
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return getBiome(position.getX(), position.getY());
    }

}
