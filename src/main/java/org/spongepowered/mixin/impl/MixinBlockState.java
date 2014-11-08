package org.spongepowered.mixin.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockProperty;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.mod.mixin.Mixin;

@Mixin(BlockState.StateImplementation.class)
public abstract class MixinBlockState extends BlockStateBase implements org.spongepowered.api.block.BlockState {

    @Override
    public BlockType getType() {
        return (BlockType) getBlock();
    }

    // TODO: This method requires mixin support for methods with the same name and signature.
    //@Override
    //public abstract ImmutableMap<BlockProperty<?>, ? extends Comparable<?>> getProperties();

    // TODO: This method requires mixin support for methods with the same name and signature.
    //@Override
    //public abstract Collection<String> getPropertyNames();

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
    public byte getDataValue() {
        return (byte) getBlock().getMetaFromState(this);
    }

    @Override
    public BlockSnapshot getSnapshot() {
        throw new UnsupportedOperationException();
    }
}
