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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.SpongeMod;

import java.lang.reflect.Method;
import java.util.Map;

@NonnullByDefault
public class SpongeArgumentType<T> extends SpongeArgumentHolder<ArgumentType<T>> implements ArgumentType<T> {

    private static final Map<Class<?>, Function<String, ?>> converters = Maps.newHashMap();
    static {
        converters.put(String.class, Functions.<String>identity());
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<String, T> getConverter(final Class<T> type) {
        if (!converters.containsKey(type)) {
            try {
                final Method valueOf = type.getMethod("valueOf", String.class);
                converters.put(type, SpongeSelectorFactory.<String, T>methodAsFunction(valueOf, true));
            } catch (NoSuchMethodException ignored) {
                if (CatalogType.class.isAssignableFrom(type)) {
                    final Class<? extends CatalogType> type2 = type.asSubclass(CatalogType.class);
                    converters.put(type, new Function<String, T>() {

                        @Override
                        public T apply(String input) {
                            // assume it exists for now
                            return (T) SpongeMod.instance.getGame().getRegistry().getType(type2, input).get();
                        }

                    });
                } else {
                    throw new IllegalStateException("can't convert " + type);
                }
            } catch (SecurityException ignored) {
            }
        }
        return (Function<String, T>) converters.get(type);
    }

    private final String key;
    private final Function<String, T> converter;

    public SpongeArgumentType(String key, Class<T> type) {
        this(key, getConverter(type));
    }

    public SpongeArgumentType(String key, Function<String, T> converter) {
        this.key = key;
        this.converter = converter;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    protected T convert(String s) {
        return this.converter.apply(s);
    }

    public static class Invertible<T> extends SpongeArgumentType<T> implements ArgumentType.Invertible<T> {

        public Invertible(String key, Class<T> type) {
            super(key, type);
        }

        public Invertible(String key, Function<String, T> converter) {
            super(key, converter);
        }

    }
}
