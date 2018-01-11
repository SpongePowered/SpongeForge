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
package org.spongepowered.mod.mixin.core.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinSaveHandler;
import org.spongepowered.common.plugin.PluginContainerExtension;
import org.spongepowered.mod.SpongeMod;

import java.io.File;

@Mixin(value = SaveHandler.class, priority = 1001)
public abstract class MixinSaveHandler {

    @Shadow @Final protected DataFixer dataFixer;
    @Shadow @Final private File worldDirectory;
    private File modWorldDirectory = null; 

    @Redirect(method = "saveWorldInfoWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;handleWorldDataSave"
            + "(Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/nbt/NBTTagCompound;)V", remap = false))
    public void redirectHandleWorldDataSaveWithPlayer(FMLCommonHandler fml, SaveHandler handler, WorldInfo worldInformation, NBTTagCompound
            compound) {
        if (fml.getSavesDirectory().equals(this.worldDirectory.getParentFile())) {
            fml.handleWorldDataSave(handler, worldInformation, compound);
        }
    }

    @Redirect(method = "loadWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/SaveFormatOld;loadAndFix(Ljava/io/File;Lnet/minecraft/util/datafix/DataFixer;Lnet/minecraft/world/storage/SaveHandler;)Lnet/minecraft/world/storage/WorldInfo;", remap = false))
    private WorldInfo onLoadWorldInfo(File file, DataFixer fixer, SaveHandler handler) {
        final WorldInfo worldInfo = SaveFormatOld.loadAndFix(file, fixer, handler);
        if (worldInfo != null) {
            try {
                ((IMixinSaveHandler) handler).loadSpongeDatData(worldInfo);
            } catch (Exception e) {
                throw new RuntimeException("derp", e);
            }
        }
        return worldInfo;
    }

    @Inject(method = "getWorldDirectory", at = @At("HEAD"), cancellable = true)
    public void onGetWorldDirectory(CallbackInfoReturnable<File> cir) {
        final ModContainer activeContainer = Loader.instance().activeModContainer();
        // Since Forge uses a single save handler mods will expect this method to return overworld's world directory
        // Fixes mods such as ComputerCraft and FuturePack
        if ((activeContainer != null && activeContainer != SpongeMod.instance && !(activeContainer instanceof PluginContainerExtension))) {
            if (this.modWorldDirectory != null) {
                cir.setReturnValue(this.modWorldDirectory);
            } else {
                final String defaultWorldName = Sponge.getServer().getDefaultWorldName();
                final String defaultWorldPath = Sponge.getPlatform().getType().isClient() ? "saves" + File.separator + defaultWorldName :
                        defaultWorldName;

                this.modWorldDirectory = SpongeImpl.getGameDir().resolve(defaultWorldPath).toFile();

                cir.setReturnValue(this.modWorldDirectory);
            }
        }
    }
}
