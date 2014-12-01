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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.math.Vector3i;
import org.spongepowered.api.math.Vectors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;

public class BlockWrapper implements BlockLoc {

    private net.minecraft.world.World handle;
    private World extent;

    private BlockPos pos;

    public BlockWrapper(World world, int x, int y, int z) {
        // This is a NOT check, be careful of that
        if (!(world instanceof net.minecraft.world.World)) {
            System.err.println("World passed to BlockWrapper wasn't a mixin for net.minecraft.world.World! Serious issue!");
            throw new RuntimeException("An unrecoverable error occured!");
        }
        handle = (net.minecraft.world.World) world;
        extent = world;
        pos = new BlockPos(x, y, z);
    }

    //TODO: Move this to Direction
    private static EnumFacing getNotchDirection(Direction dir) {
        switch (dir) {
            case DOWN:
                return EnumFacing.DOWN;
            case UP:
                return EnumFacing.UP;
            case NORTH:
                return EnumFacing.NORTH;
            case SOUTH:
                return EnumFacing.SOUTH;
            case WEST:
                return EnumFacing.WEST;
            case EAST:
                return EnumFacing.EAST;
            default:
                // TODO: EnumFacing doesn't have an 'invalid/default' value.
                return EnumFacing.DOWN;
        }
    }

    @Override
    public Extent getExtent() {
        return extent;
    }

    // TODO: Can we mixin Vector3i with BlockPos?
    @Override
    public Vector3i getPosition() {
        return Vectors.create3i(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Location getLocation() {
        return new Location(extent, Vectors.create3d(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public int getX() {
        return pos.getX();
    }

    @Override
    public int getY() {
        return pos.getY();
    }

    @Override
    public int getZ() {
        return pos.getZ();
    }

    @Override
    public void replaceWith(BlockState state) {
        // 0 is no notify flag. For now not going to notify nearby blocks of update.
        if (state instanceof IBlockState) {
            handle.setBlockState(pos, (IBlockState) state, 0);
        } else {
            // TODO: Need to figure out what is sensible for other BlockState implementing classes.
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void replaceWith(BlockType type) {
        handle.setBlockState(pos, ((net.minecraft.block.Block) type).getDefaultState(), 3);
    }

    @Override
    public void replaceWith(BlockSnapshot snapshot) {
        replaceWith(snapshot.getState());
        // TODO: However we end up storing NBT/TE data in BlockSnapshot, restore that too.
    }

    @Override
    public void interact() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interactWith(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockType getType() {
        return (BlockType) handle.getBlockState(pos).getBlock();
    }

    @Override
    public BlockState getState() {
        return (BlockState) handle.getBlockState(pos);
    }

    @Override
    public BlockSnapshot getSnapshot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDigTime() {
        return 0;
    }

    @Override
    public int getDigTimeWith(ItemStack itemStack) {
        return 0;
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
        return (byte) handle.getLight(pos);
    }

    @Override
    public byte getLuminanceFromSky() {
        return (byte) handle.getLightFor(EnumSkyBlock.SKY, pos);
    }

    @Override
    public byte getLuminanceFromGround() {
        return (byte) handle.getLightFor(EnumSkyBlock.BLOCK, pos);
    }

    @Override
    public boolean isPowered() {
        return handle.getStrongPower(pos) > 0;
    }

    @Override
    public boolean isIndirectlyPowered() {
        return handle.isBlockPowered(pos);
    }

    @Override
    public boolean isFacePowered(Direction direction) {
        return handle.getStrongPower(pos, getNotchDirection(direction)) > 0;
    }

    @Override
    public boolean isFaceIndirectlyPowered(Direction direction) {
        return handle.getRedstonePower(pos, getNotchDirection(direction)) > 0;
    }

    @Override
    public Collection<Direction> getPoweredFaces() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Direction> getIndirectlyPoweredFaces() {
        throw new UnsupportedOperationException();
    }

    public net.minecraft.world.World getHandle() {
        return handle;
    }

    @Override
    public <T> Optional<T> getData(Class<T> dataClass) {
        return Optional.absent();
    }
}
