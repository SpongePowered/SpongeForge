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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.event.state.ServerStoppedEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.granite.Granite;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, CommandSource {

    @Shadow
    public abstract void addChatMessage(IChatComponent message);

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;addFaviconToStatusResponse"
            + "(Lnet/minecraft/network/ServerStatusResponse;)V", shift = At.Shift.AFTER))
    public void onServerStarted(CallbackInfo ci) {
        Granite.instance.postState(ServerStartedEvent.class);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;finalTick(Lnet/minecraft/crash/CrashReport;)V",
            ordinal = 0, shift = At.Shift.BY, by = -9))
    public void onServerStopping(CallbackInfo ci) {
        Granite.instance.postState(ServerStoppingEvent.class);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;systemExitNow()V"))
    public void onServerStopped(CallbackInfo ci) {
        Granite.instance.postState(ServerStoppedEvent.class);
    }

    @Overwrite
    public String getServerModName() {
        return Granite.instance.getName();
    }

    @Override
    public void sendMessage(Text... messages) {
        for (Text message : messages) {
            //TODO Incorrect, needs to be fixed for Text API 2
            addChatMessage(new ChatComponentText(message.toString()));
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

}
