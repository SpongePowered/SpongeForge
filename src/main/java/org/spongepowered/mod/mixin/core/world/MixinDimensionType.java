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
import net.minecraftforge.common.util.EnumHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.WorldManager;

@Mixin(DimensionType.class)
public abstract class MixinDimensionType {

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/EnumHelper;addEnum(Ljava/lang/Class;Ljava/lang"
            + "/String;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Enum;"), remap = false)
    private static <T extends Enum<? >> T onAddEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object... paramValues) {
        final DimensionType dimensionType = (DimensionType) EnumHelper.addEnum(enumType, enumName, paramTypes, paramValues);
        WorldManager.registerDimensionType(dimensionType);
        return (T) dimensionType;
    }
}
