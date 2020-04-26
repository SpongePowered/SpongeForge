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
package org.spongepowered.mod.tracker;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.spongepowered.common.launch.transformer.tracker.TrackerMethod;

import javax.annotation.Nullable;

public final class FluidTracker {

    // DO NOT MODIFY THE SIGNATURES OF THE FOLLOWING METHODS!
    ///////////////////////// START /////////////////////////

    // IFluidHandler methods

    @TrackerMethod
    public static IFluidTankProperties[] getTankProperties(IFluidHandler fluidHandler) {
        return fluidHandler.getTankProperties();
    }

    @TrackerMethod
    public static int fill(IFluidHandler fluidHandler, FluidStack resource, boolean doFill) {
        return fluidHandler.fill(resource, doFill);
    }

    @TrackerMethod
    @Nullable
    public static FluidStack drain(IFluidHandler fluidHandler, FluidStack resource, boolean doDrain) {
        return fluidHandler.drain(resource, doDrain);
    }

    @TrackerMethod
    @Nullable
    public static FluidStack drain(IFluidHandler fluidHandler, int maxDrain, boolean doDrain) {
        return fluidHandler.drain(maxDrain, doDrain);
    }

    // IFluidBlock methods

    @TrackerMethod
    public static Fluid getFluid(IFluidBlock fluidBlock) {
        return fluidBlock.getFluid();
    }

    @TrackerMethod
    public static int place(IFluidBlock fluidBlock, World world, BlockPos pos, FluidStack fluidStack, boolean doPlace) {
        return fluidBlock.place(world, pos, fluidStack, doPlace);
    }

    @TrackerMethod
    @Nullable
    public static FluidStack drain(IFluidBlock fluidBlock, World world, BlockPos pos, boolean doDrain) {
        return fluidBlock.drain(world, pos, doDrain);
    }

    @TrackerMethod
    public static boolean canDrain(IFluidBlock fluidBlock, World world, BlockPos pos) {
        return fluidBlock.canDrain(world, pos);
    }

    @TrackerMethod
    public static float getFilledPercentage(IFluidBlock fluidBlock, World world, BlockPos pos) {
        return fluidBlock.getFilledPercentage(world, pos);
    }

    // IFluidTank methods

    @TrackerMethod
    @Nullable
    public static FluidStack getFluid(IFluidTank fluidTank) {
        return fluidTank.getFluid();
    }

    @TrackerMethod
    public static int getFluidAmount(IFluidTank fluidTank) {
        return fluidTank.getFluidAmount();
    }

    @TrackerMethod
    public static int getCapacity(IFluidTank fluidTank) {
        return fluidTank.getCapacity();
    }

    @TrackerMethod
    public static FluidTankInfo getInfo(IFluidTank fluidTank) {
        return fluidTank.getInfo();
    }

    @TrackerMethod
    public static int fill(IFluidTank fluidTank, FluidStack resource, boolean doFill) {
        return fluidTank.fill(resource, doFill);
    }

    @TrackerMethod
    @Nullable
    public static FluidStack drain(IFluidTank fluidTank, int maxDrain, boolean doDrain) {
        return fluidTank.drain(maxDrain, doDrain);
    }

    ////////////////////////// END //////////////////////////
    // Put whatever you like under here.

    private FluidTracker() {
    }
}
