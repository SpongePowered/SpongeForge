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
package org.spongepowered.mod.mixin.core.fml.common.crafting;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(CraftingHelper.class)
public class MixinCraftingHelper {

    @Redirect(method = "findFiles(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;Ljava/util/function/Function;"
            + "Ljava/util/function/BiFunction;ZZ)Z", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraftforge/fml/common/ModContainer;getSource()Ljava/io/File;"))
    private static File onFindFiles(ModContainer mod) {
        final File source = mod.getSource();
        if (!source.isDirectory() || new File(source, "assets").exists()) {
            return source;
        }
        // TODO: Clean way to handle this? Fix this in forge?
        // In a development environment are the resources and classes separated
        // into two source directories, at least in IntelliJ.
        // This is a temporary fix to make sure that advancement/recipe files
        // are loaded in the IDE.
        final String path = source.getAbsolutePath().replace('\\', '/');
        final int index = path.lastIndexOf("production/classes");
        if (index != -1) {
            final File res = new File(path.substring(0, index) + "production/resources");
            if (res.exists()) {
                return res;
            }
        }
        return source;
    }
}
