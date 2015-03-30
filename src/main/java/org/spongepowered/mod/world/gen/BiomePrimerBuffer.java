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

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.util.gen.ImmutableBiomeArea;
import org.spongepowered.api.util.gen.MutableBiomeArea;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.mod.util.gen.SpongeImmutableBiomeArea;
import org.spongepowered.mod.util.gen.SpongeMutableBiomeArea;

import java.util.Arrays;

/**
 * Mutable biome area, similar to {@link SpongeMutableBiomeArea}. This biome
 * area is backed by an array of {@link BiomeGenBase}. This is normally less
 * efficient, but when an array of {@link BiomeGenBase} is required by a
 * contract of Minecraft (for example for the
 * {@link CustomWorldChunkManager#getBiomeGenAt(BiomeGenBase[], int, int, int, int, boolean)
 * getBiomeGenAt} method), it becomes more efficient.
 *
 */
final class BiomePrimerBuffer implements MutableBiomeArea {

    private final BiomeGenBase[] biomes;
    private final int startX;
    private final int startZ;
    private final int sizeX;
    private final int sizeZ;

    public BiomePrimerBuffer(BiomeGenBase[] oldArrayToReuse, int startX, int startZ, int sizeX, int sizeZ) {
        // Check, clear and use old array
        Preconditions.checkNotNull(oldArrayToReuse);
        Preconditions.checkArgument(oldArrayToReuse.length >= sizeX * sizeZ);
        Arrays.fill(oldArrayToReuse, BiomeGenBase.ocean);
        this.biomes = oldArrayToReuse;

        this.startX = startX;
        this.startZ = startZ;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    private void checkRange(int x, int z) {
        if (x < this.startX || x >= (this.startX + this.sizeX)
                || z < this.startZ || z >= (this.startZ + this.sizeZ)) {
            throw new IndexOutOfBoundsException("Position " + new Vector2i(x, z) + " out of bounds for " + this);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(min: " + getMinBound() + ", size: " + getSize() + ")";
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return getBiome(position.getX(), position.getY());
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkRange(x, z);
        return (BiomeType) this.biomes[(x - this.startX) | (z - this.startZ) << 4];
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

    @Override
    public void setBiome(Vector2i position, BiomeType biome) {
        setBiome(position.getX(), position.getY(), biome);
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        Preconditions.checkNotNull(biome, "biome");
        checkRange(x, z);
        this.biomes[(x - this.startX) | (z - this.startZ) << 4] = (BiomeGenBase) biome;
    }

    @Override
    public void fill(BiomeType biome) {
        Arrays.fill(this.biomes, biome);
    }

    @Override
    public ImmutableBiomeArea getImmutableClone() {
        return new SpongeImmutableBiomeArea(this.biomes, this.startX, this.startZ, this.sizeX, this.sizeZ);
    }

}
