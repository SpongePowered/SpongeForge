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
import org.spongepowered.api.util.gen.MutableBiomeArea;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import java.util.Arrays;

/**
 * Mutable biome area backed by a byte array. Reusable.
 *
 * <p>Using {@link #detach()} the underlying byte array can be accessed. Both
 * the sizeX and sizeZ will be set to 0 by that method, preventing further
 * access to the byte array. The byte array can then be reused by calling
 * {@link #reuse(Vector2i)}.</p>
 */
public final class ByteArrayMutableBiomeArea extends AbstractBiomeArea implements MutableBiomeArea {

    private boolean detached;
    private final byte[] biomes;

    private final BiomeGenBase[] biomeById = BiomeGenBase.getBiomeGenArray();

    private void checkOpen() {
        Preconditions.checkState(!this.detached, "trying to use buffer after it's closed");
    }

    public ByteArrayMutableBiomeArea(Vector2i start, Vector2i size) {
        super(start, size);
        this.biomes = new byte[size.getX() * size.getY()];
    }

    @Override
    public void setBiome(Vector2i position, BiomeType biome) {
        setBiome(position.getX(), position.getY(), biome);
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkRange(x, z);
        checkOpen();

        this.biomes[(x - this.start.getX()) | (z - this.start.getY()) << 4] = (byte) ((BiomeGenBase) biome).biomeID;
    }

    @Override
    public void fill(BiomeType biome) {
        Arrays.fill(this.biomes, (byte) ((BiomeGenBase) biome).biomeID);
    }

    @Override
    public ImmutableBiomeArea getImmutableClone() {
        checkOpen();
        return new ByteArrayImmutableBiomeArea(this.biomes, this.start, this.size);
    }

    /**
     * Gets the internal byte array, and prevents further of it through this
     * object uses until {@link #reuse(Vector2i)} is called.
     *
     * @return The internal byte array.
     */
    public byte[] detach() {
        checkOpen();

        this.detached = true;
        return this.biomes;
    }

    /**
     * Gets whether this biome area is currently detached. When detached,
     * this object is available for reuse using {@link #reuse(Vector2i)}.
     * @return Whether this biome area is detached
     */
    public boolean isDetached() {
        return this.detached;
    }

    /**
     * Changes the bounds of this biome area, so that it can be reused for
     * another chunk.
     *
     * @param start New start position.
     */
    public void reuse(Vector2i start) {
        Preconditions.checkState(this.detached, "Cannot reuse while still in use");

        this.start = Preconditions.checkNotNull(start, "start");
        this.end = this.start.add(this.size).sub(Vector2i.ONE);
        Arrays.fill(this.biomes, (byte) 0);

        this.detached = false;
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return getBiome(position.getX(), position.getY());
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkOpen();

        byte biomeId = this.biomes[(x - this.start.getX()) | (z - this.start.getY()) << 4];
        BiomeType biomeType = (BiomeType) this.biomeById[biomeId & 0xff];
        return biomeType == null ? BiomeTypes.OCEAN : biomeType;
    }

}
