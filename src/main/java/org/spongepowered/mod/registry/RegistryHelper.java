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

package org.spongepowered.mod.registry;

import com.google.common.base.Function;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class RegistryHelper {

    public static boolean mapFields(Class<?> apiClass, Map<String, ?> mapping, Collection<String> ignoredFields) {
        boolean mappingSuccess = true;
        for (Field f : apiClass.getDeclaredFields()) {
            if (ignoredFields.contains(f.getName())) {
                continue;
            }
            try {
                if (!mapping.containsKey(f.getName())) {
                    continue;
                }
                f.set(null, mapping.get(f.getName()));
            } catch (Exception e) {
                e.printStackTrace();
                mappingSuccess = false;
            }
        }
        return mappingSuccess;
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction) {
        boolean mappingSuccess = true;
        for (Field f : apiClass.getDeclaredFields()) {
            try {
                f.set(null, mapFunction.apply(f.getName()));
            } catch (Exception e) {
                e.printStackTrace();
                mappingSuccess = false;
            }
        }
        return mappingSuccess;
    }

    public static boolean mapFields(Class<?> apiClass, Map<String, ?> mapping) {
        return mapFields(apiClass, mapping, Collections.<String>emptyList());
    }

    public static boolean setFactory(Class<?> apiClass, Object factory) {
        try {
            apiClass.getDeclaredField("factory").set(null, factory);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
