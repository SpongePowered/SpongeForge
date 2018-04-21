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
package org.spongepowered.mod.mixin.core.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.util.PEBKACException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;

import java.lang.reflect.Method;

@Mixin(NBTTagCompound.class)
public class MixinNBTTagCompound {

    @Inject(method = "setTag(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V", at = @At("HEAD"))
    private void checkNullTag(String key, NBTBase value, CallbackInfo callbackInfo) {
        if (value == null) {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Null being stored in NBT!").centre().hr();
            printer.addWrapped("Sponge is forcing a shutdown of the game because someone is storing nulls in an NBTTagCompound. Our implementation "
              + "and Minecraft's strictly prevents this from happening, however, certain mods are not null checking. Please provide this report in "
              + "a report to the associated mod!");
            final ModContainer mod = Loader.instance().activeModContainer();
            if (mod != null) {
                printer.add("Potential Culprit: " + mod.getModId());
            } else {
                printer.add("Potential Culprit: Not known, analyze stacktrace for clues");
            }
            printer.add("NBT Key: %s", key);
            printer.add("Exception!");
            final PEBKACException pebkacException = new PEBKACException("Someone is trying to store a null to an NBTTagCompound!");
            printer.add(pebkacException);
            printer.log(SpongeImpl.getLogger(), Level.ERROR);
            try {
                final Class<?> terminateVm = Class.forName("org.spongepowered.mixin.handler.TerminateVM");
                final Method terminate = terminateVm.getMethod("terminate");
                terminate.setAccessible(true);
                terminate.invoke(null, "net.minecraftforge.fml", -2);
            } catch (Exception e) {
                FMLCommonHandler.instance().exitJava(-2, true);
            }
        }
    }

}
