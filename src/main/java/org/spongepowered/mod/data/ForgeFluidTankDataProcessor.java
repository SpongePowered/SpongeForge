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
package org.spongepowered.mod.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidTankData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidTankData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.mutable.extra.SpongeFluidTankData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForgeFluidTankDataProcessor extends AbstractSingleDataSingleTargetProcessor<IFluidHandler, Map<Direction, List<FluidStackSnapshot>>,
        MapValue<Direction, List<FluidStackSnapshot>>, FluidTankData, ImmutableFluidTankData> {

    public ForgeFluidTankDataProcessor() {
        super(Keys.FLUID_TANK_CONTENTS, IFluidHandler.class);
    }

    @Override
    protected boolean set(IFluidHandler dataHolder, Map<Direction, List<FluidStackSnapshot>> value) {
        for (Map.Entry<Direction, List<FluidStackSnapshot>> entry : value.entrySet()) {
            final EnumFacing direction = DirectionFacingProvider.getInstance().get(entry.getKey()).get();
            final FluidTankInfo[] oldInfo = dataHolder.getTankInfo(direction);
            if (oldInfo != null) {
                for (FluidTankInfo old : oldInfo) {
                    if (old != null && old.fluid != null) {
                        dataHolder.drain(direction, old.fluid, true);
                    }
                }
            }
            for (FluidStackSnapshot snapshot : entry.getValue()) {
                dataHolder.fill(direction, ((net.minecraftforge.fluids.FluidStack) snapshot.createStack()), true);
            }
        }
        return true;
    }

    @Override
    protected Optional<Map<Direction, List<FluidStackSnapshot>>> getVal(IFluidHandler dataHolder) {
        Map<Direction, List<FluidStackSnapshot>> map = new HashMap<>();
        for (Direction api : Direction.values()) {
            final EnumFacing direction = DirectionFacingProvider.getInstance().get(api).get();
            final FluidTankInfo[] data = dataHolder.getTankInfo(direction);
            if (data != null) {
                ImmutableList.Builder<FluidStackSnapshot> snapshotBuilder = ImmutableList.builder();
                for (FluidTankInfo info : data) {
                    if (info != null && info.fluid != null) {
                        snapshotBuilder.add(((FluidStack) info.fluid).createSnapshot());
                        dataHolder.drain(direction, info.fluid, true);
                    }
                }
                map.put(api, snapshotBuilder.build());
            }
        }
        return Optional.of(map);
    }

    @Override
    protected ImmutableValue<Map<Direction, List<FluidStackSnapshot>>> constructImmutableValue(Map<Direction, List<FluidStackSnapshot>> value) {
        return new ImmutableSpongeMapValue<>(Keys.FLUID_TANK_CONTENTS, value);
    }

    @Override
    protected MapValue<Direction, List<FluidStackSnapshot>> constructValue(Map<Direction, List<FluidStackSnapshot>> actualValue) {
        return new SpongeMapValue<>(Keys.FLUID_TANK_CONTENTS, actualValue);
    }

    @Override
    protected FluidTankData createManipulator() {
        return new SpongeFluidTankData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData(); // you can't actually remove tanks, you can set them to empty lists though ;)
    }
}
