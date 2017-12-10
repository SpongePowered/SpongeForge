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
package org.spongepowered.mod.registry;

import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.reflect.Field;

public final class SpongeGameData {

    private SpongeGameData() {
    }

    private static Field callbackField;

    @SuppressWarnings("unchecked")
    public static <I extends IForgeRegistryEntry<I>> void addRegistryCallback(IForgeRegistry<I> registry,
            final ForgeRegistry.AddCallback<I> callback) throws ReflectiveOperationException {
        if (callbackField == null) {
            callbackField = ForgeRegistry.class.getDeclaredField("add");
            callbackField.setAccessible(true);
        }

        ForgeRegistry.AddCallback<I> newCallback = callback;

        final ForgeRegistry.AddCallback<I> currentCallback =
                (ForgeRegistry.AddCallback<I>) callbackField.get(registry);
        if (currentCallback != null) {
            newCallback = (owner, stage, id, obj, oldObj) -> {
                currentCallback.onAdd(owner, stage, id, obj, oldObj);
                callback.onAdd(owner, stage, id, obj, oldObj);
            };
        }

        callbackField.set(registry, newCallback);
    }

}
