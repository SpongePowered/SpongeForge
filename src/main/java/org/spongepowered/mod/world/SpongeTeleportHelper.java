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
package org.spongepowered.mod.world;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

public class SpongeTeleportHelper implements TeleportHelper {

    /** The default height radius to scan for safe locations */
    static int DEFAULT_HEIGHT = 3;
    /** The default width radius to scan for safe locations */
    static int DEFAULT_WIDTH = 9;

    @Override
    public Optional<Location> getSafeLocation(Location location) {
        return getSafeLocation(location, DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    @Override
    public Optional<Location> getSafeLocation(Location location, final int height, final int width) {
        // Check around the player first in a configurable radius:
        Optional<Location> safe = checkAboveAndBelowLocation(location, height, width);
        if (safe.isPresent()) {
            return safe;
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
        // Let's check the center of the 'circle' first...
        Vector3i blockPos = new Vector3i(location.getBlockPosition());
        if (isSafeLocation((World) location.getExtent(), blockPos)) {
            return Optional.of(new Location(location.getExtent(), blockPos));
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
        Vector3i up = new Vector3i(blockPos.add(Vector3i.UP));
        Vector3i down = new Vector3i(blockPos.sub(0, 1, 0));

        if (!isBlockSafe(world, blockPos) || !isBlockSafe(world, up) || !isBlockSafe(world, down)) {
            return false;
        }

        if (world.getBlock(down).getType() == BlockTypes.AIR) {
            final boolean blocksBelowSafe = areTwoBlocksBelowSafe(world, down);
            return blocksBelowSafe;
        }
        return true;
    }

    protected boolean isBlockSafe(World world, Vector3i blockPos) {
        BlockType block = world.getBlockType(blockPos);
        if (block.isSolidCube()) {
            return false;
        }
        if (blockPos.getY() < 0) {
            return false;
        }

        if (blockPos.getY() >= world.getDimension().getHeight()) {
            return false;
        }

        BlockType type = world.getBlockType(blockPos);
        if (type == BlockTypes.LAVA) {
            return false;
        }
        if (type == BlockTypes.FIRE) {
            return false;
        }
        return true;
    }

    protected boolean areTwoBlocksBelowSafe(World world, Vector3i blockPos) {

        Vector3i blockBelowPos = blockPos.sub(0, 1, 0);
        Vector3i blockBelowPos2 = blockPos.sub(0, 2, 0);
        BlockType blockBelow = world.getBlockType(blockBelowPos);
        BlockType blockBelow2 = world.getBlockType(blockBelowPos2);
        if (blockBelow == BlockTypes.AIR && blockBelow2 == BlockTypes.AIR) {
            return false; // prevent fall damage
        }

        if ((blockBelow == BlockTypes.AIR && (blockBelow2 == BlockTypes.LAVA || blockBelow2 == BlockTypes.FLOWING_LAVA))) {
            return false; // prevent damage;
        }

        return true;
    }
}
