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
package org.spongepowered.mod.world.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.storage.ChunkLayout;

public final class SpongeChunkLayout implements ChunkLayout {

    private static final Vector3i CHUNK_SIZE = new Vector3i(16, 256, 16);
    private static final Vector3i SPACE_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1).div(CHUNK_SIZE);
    private static final Vector3i SPACE_MIN = new Vector3i(-30000000, 0, -30000000).div(CHUNK_SIZE);
    private static final Vector3i SPACE_SIZE = SPACE_MAX.sub(SPACE_MIN).add(1, 1, 1);
    public static final SpongeChunkLayout instance = new SpongeChunkLayout();

    private SpongeChunkLayout() {
    }

    @Override
    public Vector3i getChunkSize() {
        return CHUNK_SIZE;
    }

    @Override
    public Vector3i getSpaceMax() {
        return SPACE_MAX;
    }

    @Override
    public Vector3i getSpaceMin() {
        return SPACE_MIN;
    }

    @Override
    public Vector3i getSpaceSize() {
        return SPACE_SIZE;
    }

    @Override
    public Vector3i getSpaceOrigin() {
        return Vector3i.ZERO;
    }

    @Override
    public boolean isValidChunk(Vector3i coords) {
        Preconditions.checkNotNull(coords);
        return isValidChunk(coords.getX(), coords.getY(), coords.getZ());
    }

    @Override
    public boolean isValidChunk(int x, int y, int z) {
        return x >= SPACE_MIN.getX() && x <= SPACE_MAX.getX()
                && y >= SPACE_MIN.getY() && y <= SPACE_MAX.getY()
                && z >= SPACE_MIN.getZ() && z <= SPACE_MAX.getZ();
    }

    @Override
    public Optional<Vector3i> toChunk(Vector3i worldCoords) {
        Preconditions.checkNotNull(worldCoords);
        return toChunk(worldCoords.getX(), worldCoords.getY(), worldCoords.getZ());
    }

    @Override
    public Optional<Vector3i> toChunk(int x, int y, int z) {
        final Vector3i chunkCoords = new Vector3i(x >> 4, y >> 8, z >> 4);
        return isValidChunk(chunkCoords) ? Optional.of(chunkCoords) : Optional.<Vector3i>absent();
    }

    @Override
    public Optional<Vector3i> toWorld(Vector3i chunkCoords) {
        Preconditions.checkNotNull(chunkCoords);
        return toWorld(chunkCoords.getX(), chunkCoords.getY(), chunkCoords.getZ());
    }

    @Override
    public Optional<Vector3i> toWorld(int x, int y, int z) {
        return isValidChunk(x, y, z) ? Optional.of(new Vector3i(x << 4, 0, z << 4)) : Optional.<Vector3i>absent();
    }

    @Override
    public Optional<Vector3i> addToChunk(Vector3i chunkCoords, Vector3i chunkOffset) {
        Preconditions.checkNotNull(chunkCoords);
        Preconditions.checkNotNull(chunkOffset);
        return addToChunk(chunkCoords.getX(), chunkCoords.getY(), chunkCoords.getZ(), chunkOffset.getX(), chunkOffset.getY(), chunkOffset.getZ());
    }

    @Override
    public Optional<Vector3i> addToChunk(int cx, int cy, int cz, int ox, int oy, int oz) {
        final Vector3i newChunkCoords = new Vector3i(cx + ox, cy + oy, cz + oz);
        return isValidChunk(newChunkCoords) ? Optional.of(newChunkCoords) : Optional.<Vector3i>absent();
    }

    @Override
    public Optional<Vector3i> moveToChunk(int x, int y, int z, Direction direction) {
        return moveToChunk(new Vector3i(x, y, z), direction);
    }

    @Override
    public Optional<Vector3i> moveToChunk(Vector3i chunkCoords, Direction direction) {
        return moveToChunk(chunkCoords, direction, 1);
    }

    @Override
    public Optional<Vector3i> moveToChunk(int x, int y, int z, Direction direction, int steps) {
        return moveToChunk(new Vector3i(x, y, z), direction, steps);
    }

    @Override
    public Optional<Vector3i> moveToChunk(Vector3i chunkCoords, Direction direction, int steps) {
        Preconditions.checkArgument(!direction.isSecondaryOrdinal(), "Secondary cardinal directions can't be used here");
        return addToChunk(chunkCoords, direction.toVector3d().ceil().toInt().mul(steps));
    }

}
