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
package org.spongepowered.granite.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.granite.Granite;

import java.io.File;
import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer {

    public MixinDedicatedServer(File workDir, Proxy proxy, File profileCacheDir) {
        super(workDir, proxy, profileCacheDir);
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE_STRING", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V",
            args = {"ldc=Loading properties"}, shift = At.Shift.BY, by = -2, remap = false))
    public void onServerLoad(CallbackInfoReturnable<Boolean> ci) {
        Granite.instance.load();
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;setConfigManager"
            + "(Lnet/minecraft/server/management/ServerConfigurationManager;)V", shift = At.Shift.BY, by = -7))
    public void onServerInitialize(CallbackInfoReturnable<Boolean> ci) {
        Granite.instance.initialize();
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;loadAllWorlds"
            + "(Ljava/lang/String;Ljava/lang/String;JLnet/minecraft/world/WorldType;Ljava/lang/String;)V", shift = At.Shift.BY, by = -24))
    public void onServerAboutToStart(CallbackInfoReturnable<Boolean> ci) {
        Granite.instance.postState(ServerAboutToStartEvent.class);
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;loadAllWorlds"
            + "(Ljava/lang/String;Ljava/lang/String;JLnet/minecraft/world/WorldType;Ljava/lang/String;)V", shift = At.Shift.AFTER))
    public void onServerStarting(CallbackInfoReturnable<Boolean> ci) {
        Granite.instance.postState(ServerStartingEvent.class);
    }

}
