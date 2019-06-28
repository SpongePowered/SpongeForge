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
package org.spongepowered.mod.mixin.api.forge.fluids;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = Fluid.class, remap = false)
@Implements(@Interface(iface = FluidType.class, prefix = "fluid$"))
public abstract class FluidMixin_ForgeAPI implements FluidType {

    @Shadow @Final protected String fluidName;
    @Shadow protected String unlocalizedName;
    @Shadow @Nullable protected Block block;


    @Shadow public abstract String shadow$getName();

    @Override
    public Optional<BlockType> getBlockTypeBase() {
        return Optional.ofNullable((BlockType) this.block);
    }

    @Override
    public String getId() {
        return this.fluidName;
    }

    @Intrinsic
    public String fluid$getName() {
        return this.unlocalizedName;
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = SpongeImpl.getPropertyRegistry().getStore(propertyClass);
        if (optional.isPresent()) {
            return optional.get().getFor(this);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(this);
    }
}
