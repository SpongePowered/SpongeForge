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
package org.spongepowered.mod.registry;

import org.spongepowered.api.data.property.Properties;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidTankData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidTankData;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.immutable.extra.ImmutableSpongeFluidTankData;
import org.spongepowered.common.data.manipulator.mutable.extra.SpongeFluidTankData;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.registry.type.world.gen.PopulatorTypeRegistryModule;
import org.spongepowered.common.world.gen.SpongePopulatorType;
import org.spongepowered.mod.data.ForgeFluidTankDataProcessor;
import org.spongepowered.mod.data.ForgeLightEmissionPropertyStore;
import org.spongepowered.mod.data.ForgeMatterPropertyStore;
import org.spongepowered.mod.data.ForgeSolidCubePropertyStore;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

public class SpongeForgeModuleRegistry {

    public static void registerForgeData() {
        SpongePropertyRegistry propertyRegistry = SpongeImpl.getPropertyRegistry();

        // Property registration
        propertyRegistry.register(Properties.LIGHT_EMISSION, new ForgeLightEmissionPropertyStore());
        propertyRegistry.register(Properties.MATTER, new ForgeMatterPropertyStore());
        propertyRegistry.register(Properties.IS_SOLID_CUBE, new ForgeSolidCubePropertyStore());

        // Data registration

        DataUtil.registerDualProcessor(FluidTankData.class, SpongeFluidTankData.class, ImmutableFluidTankData.class,
                ImmutableSpongeFluidTankData.class, new ForgeFluidTankDataProcessor());

        // Value registration

        //Populator types
        PopulatorTypeRegistryModule populatorTypeModule = PopulatorTypeRegistryModule.getInstance();
        populatorTypeModule.customTypeFunction = (type) -> new SpongePopulatorType(type.getSimpleName(), StaticMixinForgeHelper.getModIdFromClass(type));
    }

}
