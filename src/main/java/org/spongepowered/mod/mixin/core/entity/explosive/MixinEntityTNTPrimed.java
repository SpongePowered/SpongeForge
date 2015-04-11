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
package org.spongepowered.mod.mixin.core.entity.explosive;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulators.entities.FuseData;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.data.manipulators.SpongeFuseData;
import org.spongepowered.mod.entity.SpongeEntityConstants;
import org.spongepowered.mod.mixin.core.entity.MixinEntity;

import java.util.Collection;

@NonnullByDefault
@Mixin(net.minecraft.entity.item.EntityTNTPrimed.class)
public abstract class MixinEntityTNTPrimed extends MixinEntity implements PrimedTNT {

    @Shadow private int fuse;
    @Shadow private EntityLivingBase tntPlacedBy;
    @Shadow public abstract void explode();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> Optional<T> getData(Class<T> dataClass) {
        if (dataClass.isAssignableFrom(FuseData.class)) {
            Object manipulator = new SpongeFuseData(); // TODO because Java 6's compiler
            ((FuseData) manipulator).setFuseDuration(this.fuse);
            return Optional.<T>of((T) manipulator);
        }
        return super.getData(dataClass);
    }

    private void setFuse(int fuse) {
        checkArgument(fuse >= 0);
        this.fuse = fuse;
    }

    @Override
    public <T extends DataManipulator<T>> Optional<T> getOrCreate(Class<T> manipulatorClass) {
        return null;
    }

    @Override
    public <T extends DataManipulator<T>> boolean remove(Class<T> manipulatorClass) {
        return super.remove(manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<T>> boolean isCompatible(Class<T> manipulatorClass) {
        if (manipulatorClass.isAssignableFrom(FuseData.class)) {
            return true;
        }
        return super.isCompatible(manipulatorClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData) {
        return offer(manipulatorData, DataPriority.DATA_MANIPULATOR);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData, DataPriority priority) {
        if (((Object) manipulatorData) instanceof FuseData) { // TODO because Java 6's compiler
            if (priority == DataPriority.DATA_MANIPULATOR || priority == DataPriority.POST_MERGE) {
                Object oldData = new SpongeFuseData();
                ((SpongeFuseData) oldData).setFuseDuration(this.fuse); // TODO because Java 6's compiler
                DataTransactionResult result = preTransaction(manipulatorData, (T) oldData, this);
                // TODO Throw event here with the result for plugin manipulation
                if (result.getType() == DataTransactionResult.Type.SUCCESS) {
                    setFuse(((FuseData) (Object) manipulatorData).getFuseDuration()); // TODO because Java 6's compiler
                }
                // TODO throw post event
                return result;
            } else {
                return SpongeEntityConstants.NO_CHANGE;
            }
        }
        return super.offer(manipulatorData, priority);
    }

    @Override
    public Collection<? extends DataManipulator<?>> getManipulators() {
        ImmutableList.Builder<DataManipulator<?>> superBuilder = super.getUnsafeManipulators();
        superBuilder.add(new SpongeFuseData().fill(this).get());
        return superBuilder.build();
    }

    @Override
    public boolean validateRawData(DataContainer container) {
        boolean doesSuper = super.validateRawData(container);
        return doesSuper && container.contains(DataQuery.of("Fuse"));
    }

    @Override
    public void setRawData(DataContainer container) throws InvalidDataException {
        super.setRawData(container);
        try {
            setFuse(container.getInt(DataQuery.of("Fuse")).get());
        } catch (Exception e) {
            throw new InvalidDataException("Couldn't parse raw data", e);
        }
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(DataQuery.of("Fuse"), this.fuse);
        return container;
    }

    @Override
    public void detonate() {
        this.setDead();
        this.explode();
    }

    @Override
    public Optional<Living> getDetonator() {
        return Optional.fromNullable((Living) this.tntPlacedBy);
    }
}
