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
import org.spongepowered.api.util.gen.ImmutableBiomeArea;
import org.spongepowered.api.util.gen.MutableBiomeArea;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.Arrays;

/**
 * Mutable biome area backed by a byte array. Reusable.
 *
 * <p>Using {@link #detach()} the underlying byte array can be accessed. Both
 * the sizeX and sizeZ will be set to 0 by that method, preventing further
 * access to the byte array. The byte array can then be reused by calling
 * {@link #reuse(int, int)}.</p>
 */
public final class SpongeMutableBiomeArea extends AbstractBiomeArea implements MutableBiomeArea {

    private boolean detached;

    private void checkOpen() {
        Preconditions.checkState(!this.detached, "trying to use buffer after it's closed");
    }

    public SpongeMutableBiomeArea(int startX, int startZ, int sizeX, int sizeZ) {
        super(new byte[sizeX * sizeZ], startX, startZ, sizeX, sizeZ);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(min: " + getMinBound() + ", size: " + getSize() + ")";
    }

    @Override
    public void setBiome(Vector2i position, BiomeType biome) {
        setBiome(position.getX(), position.getY(), biome);
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkRange(x, z);
        checkOpen();

        this.biomes[(x - this.startX) | (z - this.startZ) << 4] = (byte) ((BiomeGenBase) biome).biomeID;
    }

    @Override
    public void fill(BiomeType biome) {
        Arrays.fill(this.biomes, (byte) ((BiomeGenBase) biome).biomeID);
    }

    @Override
    public ImmutableBiomeArea getImmutableClone() {
        checkOpen();
        return new SpongeImmutableBiomeArea(biomes, startX, startZ, sizeX, sizeZ);
    }

    /**
     * Gets the internal byte array, and prevents further of it through this
     * object uses until {@link #reuse(int, int)} is called.
     *
     * @return The internal byte array.
     */
    public byte[] detach() {
        checkOpen();

        this.detached = true;
        return this.biomes;
    }

    /**
     * Changes the bounds of this biome area, so that it can be reused for
     * another chunk.
     *
     * @param startX New start x.
     * @param startZ New start z.
     */
    public void reuse(int startX, int startZ) {
        Preconditions.checkState(this.detached, "Cannot reuse while still in use");

        this.startX = startX;
        this.startZ = startZ;
        Arrays.fill(this.biomes, (byte) 0);

        this.detached = false;
    }

}
