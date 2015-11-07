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
package org.spongepowered.mod.mixin.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.registry.type.world.WorldPropertyRegistryModule;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;

import java.util.UUID;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMixinMinecraft {

    private GuiOverlayDebug debugGui;

    @Inject(method = "launchIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;startServerThread()V", shift = At.Shift.BEFORE) , locals = LocalCapture.CAPTURE_FAILHARD)
    public void onlaunchIntegratedServerBeforeStart(String folderName, String worldName, WorldSettings worldSettingsIn, CallbackInfo ci,
            ISaveHandler isavehandler, WorldInfo worldInfo) {
        WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
    }

    @Inject(method = "launchIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/ISaveHandler;saveWorldInfo(Lnet/minecraft/world/storage/WorldInfo;)V", shift = At.Shift.AFTER) , locals = LocalCapture.CAPTURE_FAILHARD)
    public void onlaunchIntegratedServerAfterSaveWorldInfo(String folderName, String worldName, WorldSettings worldSettingsIn, CallbackInfo ci,
            ISaveHandler isavehandler, WorldInfo worldInfo) {
        // initialize overworld properties
        UUID uuid = UUID.randomUUID();
        ((IMixinWorldInfo) worldInfo).setUUID(uuid);
        ((WorldProperties) worldInfo).setKeepSpawnLoaded(true);
        ((WorldProperties) worldInfo).setLoadOnStartup(true);
        ((WorldProperties) worldInfo).setEnabled(true);
    }

    @Override
    public void setDebugGui(GuiOverlayDebug debugGui) {
        this.debugGui = debugGui;
    }

    @Override
    public GuiOverlayDebug getDebugGui() {
        return this.debugGui;
    }
}
