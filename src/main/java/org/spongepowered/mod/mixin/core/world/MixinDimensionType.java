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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(DimensionType.class)
public abstract class MixinDimensionType {

    @Shadow @Final private Class <? extends WorldProvider > clazz;

    WorldProvider cached;

    @Overwrite
    public WorldProvider createDimension()
    {
        try
        {
            if (cached == null) {
                Constructor<? extends WorldProvider> constructor = this.clazz.getConstructor(new Class[0]);
                cached = (WorldProvider) constructor.newInstance(new Object[0]);
            }
            // "Reset" the dimension id to the default value of an integer. As would be normally if we were always creating
            // a WorldProvider.
            cached.setDimension(0);
            return cached;
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException nosuchmethodexception)
        {
            throw new Error("Could not create new dimension", nosuchmethodexception);
        }
    }
}
