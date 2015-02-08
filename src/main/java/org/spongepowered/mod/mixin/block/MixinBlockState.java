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
package org.spongepowered.mod.mixin.block;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockProperty;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@NonnullByDefault
@Mixin(BlockState.StateImplementation.class)
public abstract class MixinBlockState extends BlockStateBase implements org.spongepowered.api.block.BlockState {

    @Override
    public BlockType getType() {
        return (BlockType) getBlock();
    }

    @Shadow
    @SuppressWarnings("rawtypes")
    private final ImmutableMap properties = null;

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableMap<BlockProperty<?>, ? extends Comparable<?>> getProperties() {
        return this.properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getPropertyNames() {
        ImmutableMap<IProperty, Comparable<?>> properties = ((IBlockState) this).getProperties();
        List<String> names = Lists.newArrayListWithCapacity(properties.size());
        for (IProperty property : properties.keySet()) {
            names.add(property.getName());
        }
        return Collections.unmodifiableCollection(names);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<BlockProperty<?>> getPropertyByName(String name) {
        ImmutableMap<IProperty, Comparable<?>> properties = ((IBlockState) this).getProperties();
        for (IProperty property : properties.keySet()) {
            if (property.getName().equals(name)) {
                // The extra specification here is because Java auto-detects <? extends BlockProperty<?>>
                return Optional.<BlockProperty<?>>fromNullable((BlockProperty<?>) property);
            }
        }

        return Optional.absent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<? extends Comparable<?>> getPropertyValue(String name) {
        ImmutableMap<IProperty, Comparable<?>> properties = ((IBlockState) this).getProperties();
        for (IProperty property : properties.keySet()) {
            if (property.getName().equals(name)) {
                return Optional.fromNullable(properties.get(property));
            }
        }

        return Optional.absent();
    }

    @Override
    public org.spongepowered.api.block.BlockState withProperty(BlockProperty<?> property, Comparable<?> value) {
        return (org.spongepowered.api.block.BlockState) withProperty((IProperty) property, value);
    }

    @Override
    public org.spongepowered.api.block.BlockState cycleProperty(BlockProperty<?> property) {
        return (org.spongepowered.api.block.BlockState) cycleProperty((IProperty) property);
    }

    @Override
    @Deprecated
    public byte getDataValue() {
        return (byte) getBlock().getMetaFromState(this);
    }
}
