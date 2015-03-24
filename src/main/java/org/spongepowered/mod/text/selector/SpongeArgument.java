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
package org.spongepowered.mod.text.selector;

import org.spongepowered.api.CatalogType;

import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class SpongeArgument<T> implements Argument<T> {

    public static class Invertible<T> extends SpongeArgument<T> implements Argument.Invertible<T> {

        private final boolean inverted;

        public Invertible(ArgumentType.Invertible<T> type, T value, boolean inverted) {
            super(type, value);
            this.inverted = inverted;
        }

        @Override
        String getEqualitySymbols() {
            return isInverted() ? "!=" : "=";
        }

        @Override
        public boolean isInverted() {
            return this.inverted;
        }

        @Override
        public Argument.Invertible<T> invert() {
            return new SpongeArgument.Invertible<T>((ArgumentType.Invertible<T>) this.getType(), this.getValue(), !this.isInverted());
        }

    }

    private static String toSelectorArgument(Object val) {
        if (val instanceof CatalogType) {
            return ((CatalogType) val).getId();
        }
        return String.valueOf(val);
    }

    private final ArgumentType<T> type;
    private final T value;

    public SpongeArgument(ArgumentType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    String getEqualitySymbols() {
        return "=";
    }

    @Override
    public ArgumentType<T> getType() {
        return this.type;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public String toPlain() {
        return this.type.getKey() + getEqualitySymbols() + toSelectorArgument(getValue());
    }

}
