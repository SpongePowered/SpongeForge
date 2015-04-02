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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.api.block.BlockProperty;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;

@NonnullByDefault
@Mixin(PropertyHelper.class)
@SuppressWarnings("rawtypes")
@Implements(@Interface(iface = BlockProperty.class, prefix = "blockProperty$"))
public abstract class MixinPropertyHelper implements IProperty {

    @Intrinsic
    public String blockProperty$getName() {
        return this.getName();
    }

    public Collection blockProperty$getValidValues() {
        return getAllowedValues();
    }

    public String blockProperty$getNameForValue(Comparable value) {
        return getName(value);
    }

    @SuppressWarnings("unchecked")
    public Optional blockProperty$getValueForName(String name) {
        for (Comparable o : (Collection<Comparable>) getAllowedValues()) {
            if (getName(o).equals(name)) {
                return Optional.fromNullable(o);
            }
        }

        return Optional.absent();
    }

    // These don't need any special methods, as those are done in the PropertyHelper mixin.
    @Mixin(PropertyBool.class)
    public abstract static class MixinPropertyBool extends PropertyHelper implements BlockProperty.BooleanProperty {

        private MixinPropertyBool() {
            super("dummyPropertyBool", Object.class);
        }
    }

    @Mixin(PropertyEnum.class)
    public abstract static class MixinPropertyEnum extends PropertyHelper implements BlockProperty.EnumProperty {

        private MixinPropertyEnum() {
            super("dummyPropertyEnum", Object.class);
        }
    }

    @Mixin(PropertyInteger.class)
    public abstract static class MixinPropertyInteger extends PropertyHelper implements BlockProperty.IntegerProperty {

        private MixinPropertyInteger() {
            super("dummyPropertyInteger", Object.class);
        }
    }
}
