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
package org.spongepowered.mod.mixin.core.common.fluid;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.extra.fluid.SpongeFluidStackBuilder;

import javax.annotation.Nullable;

@Mixin(value = SpongeFluidStackBuilder.class, remap = false)
public abstract class MixinSpongeFluidStackBuilder extends AbstractDataBuilder<FluidStack> {

    @Shadow FluidType fluidType;
    @Shadow int volume;
    @Shadow DataContainer extra;

    public MixinSpongeFluidStackBuilder(Class<FluidStack> requiredClass, int supportedVersion) {
        super(requiredClass, supportedVersion);
    }

    /**
     * @author gabizou
     * @reason Use forge's fluid stack instaed of sponge's fluid stack.
     */
    @Overwrite
    public FluidStack build() {
        checkNotNull(this.fluidType, "Fluidtype cannot be null!");
        checkState(this.volume >= 0, "Volume must be at least zero!");
        @Nullable NBTTagCompound compound = this.extra == null ? null : NbtTranslator.getInstance().translateData(this.extra);
        net.minecraftforge.fluids.FluidStack fluidStack = new net.minecraftforge.fluids.FluidStack((Fluid) this.fluidType, this.volume, compound);
        return (FluidStack) fluidStack;
    }

}
