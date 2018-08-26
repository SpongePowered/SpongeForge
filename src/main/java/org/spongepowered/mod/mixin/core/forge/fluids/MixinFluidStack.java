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
package org.spongepowered.mod.mixin.core.forge.fluids;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(value = FluidStack.class, remap = false)
public class MixinFluidStack implements org.spongepowered.api.extra.fluid.FluidStack {

    @Shadow public int amount;
    @Shadow @Nullable public NBTTagCompound tag;
    @Shadow private IRegistryDelegate<Fluid> fluidDelegate;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(containerClass);
        if (optional.isPresent()) {
            return (Optional<T>) optional.get().from(this);
        }
        return Optional.empty();

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(containerClass);
        if (optional.isPresent()) {
            return (Optional<T>) optional.get().createFrom(this);
        } else if (this instanceof IMixinCustomDataHolder) {
            return ((IMixinCustomDataHolder) this).getCustom(containerClass);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(holderClass);
        return optional.isPresent() && optional.get().supports(this);
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = DataUtil.getBaseValueProcessor(key);
        if (optional.isPresent()) {
            return optional.get().offerToStore(this, value);
        } else if (this instanceof IMixinCustomDataHolder) {
            return ((IMixinCustomDataHolder) this).offerCustom(key, value);
        }
        return DataTransactionResult.failNoData();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        final Optional<DataProcessor> optional = DataUtil.getWildDataProcessor(valueContainer.getClass());
        if (optional.isPresent()) {
            return optional.get().set(this, valueContainer, checkNotNull(function));
        } else if (this instanceof IMixinCustomDataHolder) {
            return ((IMixinCustomDataHolder) this).offerCustom(valueContainer, function);
        }
        return DataTransactionResult.failResult(valueContainer.getValues());
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers) {
        DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (DataManipulator<?, ?> manipulator : valueContainers) {
            final DataTransactionResult result = offer(manipulator);
            if (!result.getRejectedData().isEmpty()) {
                builder.reject(result.getRejectedData());
            }
            if (!result.getReplacedData().isEmpty()) {
                builder.replace(result.getReplacedData());
            }
            if (!result.getSuccessfulData().isEmpty()) {
                builder.success(result.getSuccessfulData());
            }
            final DataTransactionResult.Type type = result.getType();
            builder.result(type);
            switch (type) {
                case UNDEFINED:
                case ERROR:
                case CANCELLED:
                    return builder.build();
                default:
                    break;
            }
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(containerClass);
        if (optional.isPresent()) {
            return optional.get().remove(this);
        } else if (this instanceof IMixinCustomDataHolder) {
            return ((IMixinCustomDataHolder) this).removeCustom(containerClass);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        final Optional<ValueProcessor<?, ?>> optional = DataUtil.getWildValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            return optional.get().removeFrom(this);
        } else if (this instanceof IMixinCustomDataHolder) {
            return ((IMixinCustomDataHolder) this).removeCustom(key);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
            return DataTransactionResult.successNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (ImmutableValue<?> replaced : result.getReplacedData()) {
            builder.absorbResult(offer(replaced));
        }
        for (ImmutableValue<?> successful : result.getSuccessfulData()) {
            builder.absorbResult(remove(successful));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return offer(that.getContainers(), function);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = DataUtil.getBaseValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            return optional.get().getValueFromContainer(this);
        }
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        final Optional<ValueProcessor<E, V>> optional = DataUtil.getValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            return optional.get().getApiValueFromContainer(this);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        final Optional<ValueProcessor<?, ?>> optional = DataUtil.getWildValueProcessor(checkNotNull(key));
        return optional.isPresent() && optional.get().supports(this);
    }

    @Override
    public DataHolder copy() {
        FluidStack fluidStack = new FluidStack(this.fluidDelegate.get(), this.amount, this.tag);
        return (DataHolder) fluidStack;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return ImmutableSet.of();
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return ImmutableSet.of();
    }

    @Override
    public FluidType getFluid() {
        return (FluidType) this.fluidDelegate.get();
    }

    @Override
    public int getVolume() {
        return this.amount;
    }

    @Override
    public org.spongepowered.api.extra.fluid.FluidStack setVolume(int volume) {
        checkArgument(volume > 0);
        this.amount = volume;
        return this;
    }

    @Override
    public FluidStackSnapshot createSnapshot() {
        return new SpongeFluidStackSnapshotBuilder().from(this).build();
    }

    @Override
    public boolean validateRawData(DataView container) {
        // TODO
        return false;
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        // TODO
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.FLUID_TYPE, this.fluidDelegate.get().getName())
                .set(DataQueries.FLUID_VOLUME, this.getVolume());
        if (this.tag != null) {
            container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.tag));
        }
        return container;
    }

    @Override
    public OptionalDouble getDoubleProperty(Property<Double> property) {
        return getFluid().getDoubleProperty(property);
    }

    @Override
    public OptionalInt getIntProperty(Property<Integer> property) {
        return getFluid().getIntProperty(property);
    }

    @Override
    public <V> Optional<V> getProperty(Property<V> property) {
        return getFluid().getProperty(property);
    }

    @Override
    public Map<Property<?>, ?> getProperties() {
        return getFluid().getProperties();
    }
}
