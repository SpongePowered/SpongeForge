/**
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
package org.spongepowered.wrapper;

import com.google.common.base.Optional;
import net.minecraft.world.EnumSkyBlock;
import org.spongepowered.api.block.Block;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.math.Vector3i;
import org.spongepowered.api.math.Vectors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

public class BlockWrapper implements Block {
    private net.minecraft.world.World handle;
    private World extent;
    private int x, y, z;
    private BlockType blockType;

    public BlockWrapper(World world, int x, int y, int z) {
        if (world instanceof net.minecraft.world.World) {
            System.err.println("World passed to BlockWrapper wasn't a mixin for net.minecraft.world.World! Serious issue!");
            handle = (net.minecraft.world.World) world;
            throw new RuntimeException("An unrecoverable error occured!");
        }
        extent = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = (BlockType) handle.getBlock(x, y, z);
    }

    @Override
    public Extent getExtent() {
        return extent;
    }

    @Override
    public Vector3i getPosition() {
        return Vectors.create3i(x, y, z);
    }

    @Override
    public Location getLocation() {
        return new Location(extent, Vectors.create3d(x, y, z));
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void replaceData(byte data) {
        // 0 is no notify flag. For now not going to notify nearby blocks of update.
        handle.setBlockMetadataWithNotify(x, y, z, data, 0);
    }

    @Override
    public void replaceWith(BlockType type) {
        handle.setBlock(x, y, z, net.minecraft.block.Block.getBlockFromName(type.getId()));
        this.blockType = (BlockType) handle.getBlock(x, y, z); // TODO: This feels wrong and will probably break.
    }

    @Override
    public void replaceWith(BlockSnapshot snapshot) {
        replaceData(snapshot.getDataValue());
        replaceWith(snapshot.getType());
    }

    @Override
    public BlockType getType() {
        return null;
    }

    @Override
    public byte getDataValue() {
        return (byte) handle.getBlockMetadata(x, y, z);
    }

    @Override
    public BlockSnapshot getSnapshot() {
        return null;
    }

    @Override
    public <T> Optional<T> getComponent(Class<T> clazz) {
        return null;
    }

    @Override
    public boolean dig() {
        return false;
    }

    @Override
    public boolean digWith(ItemStack itemStack) {
        return false;
    }

    @Override
    public byte getLuminance() {
        return (byte) handle.getBlockLightValue(x, y, z);
    }

    @Override
    public byte getLuminanceFromSky() {
        return (byte) handle.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
    }

    @Override
    public byte getLuminanceFromGround() {
        return (byte) handle.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
    }

    @Override
    public boolean isPowered() {
        return handle.getBlockPowerInput(x, y, z) > 0;
    }

    @Override
    public boolean isIndirectlyPowered() {
        return handle.isBlockIndirectlyGettingPowered(x, y, z);
    }

    @Override
    public boolean isFacePowered(Direction direction) {
        return handle.getIndirectPowerLevelTo(x, y, z, getNotchDirection(direction)) > 0;
    }

    @Override
    public boolean isFaceIndirectlyPowered(Direction direction) {
        return handle.getIndirectPowerLevelTo(x, y, z, getNotchDirection(direction)) > 0;
    }

    //TODO: Move this to Direction
    private static int getNotchDirection(Direction dir) {
        switch (dir) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                return 7;
        }
    }

    public net.minecraft.world.World getHandle() {
        return handle;
    }
}
