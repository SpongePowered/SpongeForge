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

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.util.gen.BiomeBuffer;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

/**
 * Base class for biome areas backed by byte arrays.
 *
 */
class AbstractBiomeArea implements BiomeBuffer {

    protected final BiomeGenBase[] biomeById = BiomeGenBase.getBiomeGenArray();

    protected final byte[] biomes;
    protected int startX;
    protected int startZ;
    protected int sizeX;
    protected int sizeZ;

    AbstractBiomeArea(byte[] biomes, int startX, int startZ, int sizeX, int sizeZ) {
        Preconditions.checkArgument(biomes.length >= sizeX * sizeZ);
        Preconditions.checkArgument(sizeX > 0);
        Preconditions.checkArgument(sizeZ > 0);

        this.biomes = biomes;

        this.startX = startX;
        this.startZ = startZ;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    protected final void checkRange(int x, int z) {
        if (x < this.startX || x >= (this.startX + this.sizeX)
                || z < this.startZ || z >= (this.startZ + this.sizeZ)) {
            throw new IndexOutOfBoundsException("Position " + new Vector2i(x, z) + " out of bounds for " + this);
        }
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return getBiome(position.getX(), position.getY());
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkRange(x, z);
        BiomeType biomeType = (BiomeType) this.biomeById[(x - this.startX) | (z - this.startZ) << 4];
        return biomeType == null ? BiomeTypes.OCEAN : biomeType;
    }

    @Override
    public Vector2i getMinBound() {
        return new Vector2i(this.startX, this.startZ);
    }

    @Override
    public Vector2i getMaxBound() {
        return getMinBound().add(getSize());
    }

    @Override
    public Vector2i getSize() {
        return new Vector2i(this.sizeX, this.sizeZ);
    }

}
