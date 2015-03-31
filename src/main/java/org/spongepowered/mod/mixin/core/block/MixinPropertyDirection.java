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
package org.spongepowered.mod.mixin.core.block;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.block.BlockProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.util.ArrayList;
import java.util.Collection;

@NonnullByDefault
@Mixin(PropertyDirection.class)
public abstract class MixinPropertyDirection extends PropertyEnum implements BlockProperty.DirectionProperty {

    @SuppressWarnings("rawtypes")
    private MixinPropertyDirection() {
        super("dummyPropertyDirection", Object.class, new ArrayList());
    }

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

    private Direction getDirection(EnumFacing facing) {
        Direction direction = SpongeGameRegistry.directionMap.inverse().get(facing);
        if (direction == null) {
            throw new IllegalArgumentException(String.format("Invalid EnumFacing '%s'", facing.toString()));
        }
        return direction;
    }

    private EnumFacing getFacing(Direction direction) {
        EnumFacing facing = SpongeGameRegistry.directionMap.get(direction);
        if (facing == null) {
            throw new IllegalArgumentException(String.format("Invalid Direction '%s', only cardinal and upright directions are supported",
                    direction.toString()));
        }
        return facing;
    }
}
