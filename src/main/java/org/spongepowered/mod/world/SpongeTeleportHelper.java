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
package org.spongepowered.mod.world;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

public class SpongeTeleportHelper implements TeleportHelper {

    @Override
    public Optional<Location> getSafeLocation(Location location) {
        return getSafeLocation(location, DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    @Override
    public Optional<Location> getSafeLocation(Location location, final int height, final int width) {
        // Check around the player first in a configurable radius:
        final Optional<Location> safe = checkAboveAndBelowLocation(location, height, width);
        if (safe.isPresent()) {
            return Optional.of(new Location(safe.get().getExtent(), safe.get().getPosition().add(0.5, 0, 0.5)));
        } else {
            return Optional.absent();
        }
    }

    private Optional<Location> checkAboveAndBelowLocation(Location location, final int height, final int width) {
        // For now this will just do a straight up block.
        // Check the main level
        Optional<Location> safe = checkAroundLocation(location, width);

        if (safe.isPresent()) {
            return safe;
        }

        // We've already checked zero right above this.
        for (int currentLevel = 1; currentLevel <= height; currentLevel++) {
            // Check above
            safe = checkAroundLocation(new Location(location.getExtent(), location.getPosition().add(0, currentLevel, 0)), width);
            if (safe.isPresent()) {
                return safe;
            }

            // Check below
            safe = checkAroundLocation(new Location(location.getExtent(), location.getPosition().sub(0, currentLevel, 0)), width);
            if (safe.isPresent()) {
                return safe;
            }
        }

        return Optional.absent();
    }

    private Optional<Location> checkAroundLocation(Location location, final int radius) {
        if (isSafeLocation((World) location.getExtent(), location.getBlockPosition())) {
            return Optional.of(location);
        }

        // Now we're going to search in expanding concentric circles...
        for (int currentRadius = 0; currentRadius <= radius; currentRadius++) {
            Optional<Vector3i> safePosition = checkAroundSpecificDiameter(location, currentRadius);
            if (safePosition.isPresent()) {
                // If a safe area was found: Return the checkLoc, it is the safe location.
                return Optional.of(new Location(location.getExtent(), safePosition.get()));
            }
        }

        return Optional.absent();
    }

    private Optional<Vector3i> checkAroundSpecificDiameter(Location checkLoc, final int radius) {
        World world = (World) checkLoc.getExtent();
        Vector3i blockPos = checkLoc.getBlockPosition();
        // Check out at the radius provided.
        blockPos = blockPos.add(radius, 0, 0);
        if (isSafeLocation(world, blockPos)) {
            return Optional.of(blockPos);
        }

        // Move up to the first corner..
        for (int i = 0; i < radius; i++) {
            blockPos = blockPos.add(radius, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }

        // Move to the second corner..
        for (int i = 0; i < radius * 2; i++) {
            blockPos = blockPos.add(radius, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }

        // Move to the third corner..
        for (int i = 0; i < radius * 2; i++) {
            blockPos = blockPos.add(radius, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }

        // Move to the last corner..
        for (int i = 0; i < radius * 2; i++) {
            blockPos = blockPos.add(radius, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }

        // Move back to just before the starting point.
        for (int i = 0; i < radius - 1; i++) {
            blockPos = blockPos.add(radius, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }
        return Optional.absent();
    }

    public boolean isSafeLocation(World world, Vector3i blockPos) {
        final Vector3i up = new Vector3i(blockPos.add(Vector3i.UP));
        final Vector3i down = new Vector3i(blockPos.sub(Vector3i.UP));

        return !(!isBlockSafe(world, blockPos, false) || !isBlockSafe(world, up, false) || !isBlockSafe(world, down, true));

    }

    private boolean isBlockSafe(World world, Vector3i blockPos, boolean floorBlock) {
        final BlockType block = world.getBlockType(blockPos);

        if (blockPos.getY() <= 0) {
            return false;
        }

        if (blockPos.getY() > world.getHeight()) {
            return false;
        }


        if (floorBlock) {
            //Floor is air so we'll fall, need to make sure we fall safely.
            if (block == BlockTypes.AIR) {
                final BlockType typeBelowPos = world.getBlockType(blockPos.sub(0, 1, 0));
                final BlockType typeBelowPos2 = world.getBlockType(blockPos.sub(0, 2, 0));

                // We'll fall too far, not safe
                if (typeBelowPos == BlockTypes.AIR && typeBelowPos2 == BlockTypes.AIR) {
                    return false;
                }

                // We'll fall onto a block, need to make sure its safe
                if (typeBelowPos != BlockTypes.WATER || typeBelowPos != BlockTypes.FLOWING_WATER || !typeBelowPos.isSolidCube()) {
                    return false;
                }

                // We'll fall through an air block to another, need to make sure its safe
                return !(typeBelowPos2 != BlockTypes.WATER || typeBelowPos2 != BlockTypes.FLOWING_WATER || !typeBelowPos2.isSolidCube());
            }

            //We have a non-air floor, need to ensure its safe
            return block == BlockTypes.WATER || block == BlockTypes.FLOWING_WATER || block.isSolidCube();
        }

        //We need to make sure the block at our torso or head is safe
        return block == BlockTypes.AIR || block == BlockTypes.WATER || block == BlockTypes.FLOWING_WATER;
    }
}
