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
package org.spongepowered.common.registry;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.common.Sponge;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

public final class RegistryHelper {

    private RegistryHelper() {
    }

    private static boolean isCatalogField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);
    }

    public static boolean mapFields(Class<?> apiClass, Map<String, ?> mapping, String... ignored) {
        boolean mappingSuccess = true;
        Set<String> ignoredFields = ImmutableSet.copyOf(ignored);

        for (Field field : apiClass.getDeclaredFields()) {
            String name = field.getName();
            if (ignoredFields.contains(name) || !mapping.containsKey(name) || !isCatalogField(field)) {
                continue;
            }

            Object value = mapping.get(name);
            try {
                field.set(null, value);
            } catch (Exception e) {
                Sponge.getLogger().error("Failed to set {} = {}", field, value, e);
                mappingSuccess = false;
            }

        }

        return mappingSuccess;
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction) {
        boolean mappingSuccess = true;

        for (Field field : apiClass.getDeclaredFields()) {
            if (!isCatalogField(field)) {
                continue;
            }

            Object value = mapFunction.apply(field.getName());
            if (value != null) {
                try {
                    field.set(null, value);
                } catch (Exception e) {
                    Sponge.getLogger().error("Failed to set {} = {}", field, value, e);
                    mappingSuccess = false;
                }
            }
        }

        return mappingSuccess;
    }

    public static boolean setFactory(Class<?> apiClass, Object factory) {
        try {
            apiClass.getDeclaredField("factory").set(null, factory);
            return true;
        } catch (Exception e) {
            Sponge.getLogger().error("Failed to set factory of {} to {}", apiClass, factory, e);
            return false;
        }
    }

}
