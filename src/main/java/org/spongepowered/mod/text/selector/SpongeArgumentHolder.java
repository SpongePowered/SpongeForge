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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.math.vector.Vector3l;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@NonnullByDefault
public class SpongeArgumentHolder<T extends ArgumentHolder<?>> implements ArgumentHolder<T> {

    private final Set<T> subtypes;

    @SuppressWarnings("unchecked")
    protected SpongeArgumentHolder() {
        if (this instanceof ArgumentType) {
            this.subtypes = ImmutableSet.of((T) this);
        } else {
            throw new IllegalStateException("Must provide subtypes if not ArgumentType");
        }
    }

    public SpongeArgumentHolder(Set<T> subtypes) {
        this.subtypes = ImmutableSet.copyOf(subtypes);
    }

    @Override
    public int getCount() {
        return this.subtypes.size();
    }

    @Override
    public Set<T> getTypes() {
        return this.subtypes;
    }

    public static class SpongeVector3<V, T> extends SpongeArgumentHolder<ArgumentType<T>> implements ArgumentHolder.Vector3<V, T> {

        private static final SetMultimap<Class<?>, Function<?, ?>> extractFunctionSets = HashMultimap.create();
        static {
            List<Class<?>> vectors = Arrays.<Class<?>>asList(Vector3i.class, Vector3l.class, Vector3f.class, Vector3d.class);;
            for (Class<?> vec : vectors) {
                Set<Function<?, ?>> set = Sets.newLinkedHashSet();
                try {
                    set.add(SpongeSelectorFactory.methodAsFunction(vec.getDeclaredMethod("getX"), false));
                    set.add(SpongeSelectorFactory.methodAsFunction(vec.getDeclaredMethod("getY"), false));
                    set.add(SpongeSelectorFactory.methodAsFunction(vec.getDeclaredMethod("getZ"), false));
                } catch (Exception e) {
                    // should support getX/Y/Z
                    throw new AssertionError("bad vector3 type");
                }
                extractFunctionSets.asMap().put(vec, set);
            }
        }

        private final ArgumentType<T> x;
        private final ArgumentType<T> y;
        private final ArgumentType<T> z;
        private final Set<Function<V, T>> extractFunctionSet;

        @SuppressWarnings("unchecked")
        public SpongeVector3(ArgumentType<T> x, ArgumentType<T> y, ArgumentType<T> z, Class<V> vectorType) {
            super(ImmutableSet.of(x, y, z));
            this.x = x;
            this.y = y;
            this.z = z;
            this.extractFunctionSet = (Set<Function<V, T>>) (Object) extractFunctionSets.get(vectorType);
            if (this.extractFunctionSet.isEmpty()) {
                throw new IllegalStateException("Unknown vector type " + vectorType);
            }
        }

        @Override
        public ArgumentType<T> x() {
            return this.x;
        }

        @Override
        public ArgumentType<T> y() {
            return this.y;
        }

        @Override
        public ArgumentType<T> z() {
            return this.z;
        }

        protected Set<Function<V, T>> extractFunctions() {
            return this.extractFunctionSet;
        }

    }

    public static class SpongeLimit<T extends ArgumentHolder<?>> extends SpongeArgumentHolder<T> implements ArgumentHolder.Limit<T> {

        private final T min;
        private final T max;

        public SpongeLimit(T min, T max) {
            super(ImmutableSet.of(min, max));
            this.min = min;
            this.max = max;
        }

        @Override
        public T minimum() {
            return this.min;
        }

        @Override
        public T maximum() {
            return this.max;
        }

    }

}
