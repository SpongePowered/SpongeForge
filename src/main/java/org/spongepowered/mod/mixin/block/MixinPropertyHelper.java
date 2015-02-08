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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.api.block.BlockProperty;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;

@NonnullByDefault
@Mixin(PropertyHelper.class)
@SuppressWarnings("rawtypes")
public abstract class MixinPropertyHelper implements BlockProperty, IProperty {
    // TODO: mixin, methods with same name and return type in mcp names
    //@Override
    //public String getName() {
    //    return null;
    //}

    @Override
    public Collection getValidValues() {
        return getAllowedValues();
    }

    @Override
    public String getNameForValue(Comparable value) {
        return getName(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional getValueForName(String name) {
        for (Comparable o : (Collection<Comparable>) getAllowedValues()) {
            if (getName(o).equals(name)) {
                return Optional.fromNullable(o);
            }
        }

        return Optional.absent();
    }

    // These don't need any special methods, as those are done in the PropertyHelper mixin.
    @Mixin(PropertyBool.class)
    public abstract static class MixinPropertyBool extends PropertyHelper implements BooleanProperty {

        private MixinPropertyBool() {
            super("dummyPropertyBool", Object.class);
        }
    }

    @Mixin(PropertyEnum.class)
    public abstract static class MixinPropertyEnum extends PropertyHelper implements EnumProperty {

        private MixinPropertyEnum() {
            super("dummyPropertyEnum", Object.class);
        }
    }

    @Mixin(PropertyInteger.class)
    public abstract static class MixinPropertyInteger extends PropertyHelper implements IntegerProperty {

        private MixinPropertyInteger() {
            super("dummyPropertyInteger", Object.class);
        }
    }
}
