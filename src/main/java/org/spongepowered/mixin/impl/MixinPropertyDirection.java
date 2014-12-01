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
package org.spongepowered.mixin.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.block.BlockProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Mixin;

import java.util.ArrayList;
import java.util.Collection;

@NonnullByDefault
@Mixin(PropertyDirection.class)
public abstract class MixinPropertyDirection extends PropertyEnum implements BlockProperty.DirectionProperty {

    @SuppressWarnings("rawtypes")
    private MixinPropertyDirection() {
        super("dummyPropertyDirection", Object.class, new ArrayList());
    }

    // TODO: mixin, methods with same name and return type in mcp names
    //@Override
    //public String getName() {
    //    return null;
    //}

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Direction> getValidValues() {
        return convertValues(getAllowedValues());
    }

    @Override
    public String getNameForValue(Direction value) {
        return getName(getFacing(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Direction> getValueForName(String name) {
        for (EnumFacing o : (Collection<EnumFacing>) getAllowedValues()) {
            if (getName(o).equals(name)) {
                return Optional.of(getDirection(o));
            }
        }

        return Optional.absent();
    }

    private Collection<Direction> convertValues(Collection<EnumFacing> values) {
        ArrayList<Direction> converted = new ArrayList<Direction>();

        for (EnumFacing facing : values) {
            converted.add(getDirection(facing));
        }

        return ImmutableSet.copyOf(converted);
    }

    // TODO: These are probably generally useful and should be in a helper class somewhere.
    private Direction getDirection(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return Direction.DOWN;
            case UP:
                return Direction.UP;
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case EAST:
                return Direction.EAST;
            default:
                throw new IllegalArgumentException(String.format("Invalid EnumFacing '%s'", facing.toString()));
        }
    }

    private EnumFacing getFacing(Direction direction) {
        switch (direction) {
            case NORTH:
                return EnumFacing.NORTH;
            case EAST:
                return EnumFacing.EAST;
            case SOUTH:
                return EnumFacing.SOUTH;
            case WEST:
                return EnumFacing.WEST;
            case UP:
                return EnumFacing.UP;
            case DOWN:
                return EnumFacing.DOWN;
            default:
                throw new IllegalArgumentException(
                        String.format("Invalid Direction '%s', only cardinal and upright directions are supported", direction.toString()));
        }
    }
}