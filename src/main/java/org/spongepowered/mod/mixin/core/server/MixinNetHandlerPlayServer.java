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

package org.spongepowered.mod.mixin.core.server;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.net.PlayerConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.text.SpongeTexts;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection {

    private ChatComponentTranslation tmpQuitMessage;
    private ServerConfigurationManager tmpConfigManager;

    /**
     * @author Simon816
     *
     * Store the quit message and ServerConfigurationManager instance for use
     * in {@link #onDisconnectPlayer}.
     */
    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/ServerConfigurationManager;sendChatMsg(Lnet/minecraft/util/IChatComponent;)V"))
    public void onSendChatMsgCall(ServerConfigurationManager thisCtx, IChatComponent chatcomponenttranslation) {
        this.tmpQuitMessage = (ChatComponentTranslation) chatcomponenttranslation;
        this.tmpConfigManager = thisCtx;
    }

    /**
     * @author Simon816
     *
     * Fire the PlayerQuitEvent before playerLoggedOut is called in order for
     * event handlers to change the quit message captured from
     * {@link #onSendChatMsgCall}.
     */
    @Inject(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/ServerConfigurationManager;playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    public void onDisconnectPlayer(IChatComponent reason, CallbackInfo ci) {
        PlayerQuitEvent event = (PlayerQuitEvent) new PlayerEvent.PlayerLoggedOutEvent(((NetHandlerPlayServer) (Object) this).playerEntity);
        Text message = SpongeTexts.toText(this.tmpQuitMessage);
        this.tmpQuitMessage = null;
        event.setQuitMessage(message);
        FMLCommonHandler.instance().bus().post((PlayerEvent.PlayerLoggedOutEvent) event);
        this.tmpConfigManager.sendChatMsg(SpongeTexts.toComponent(event.getQuitMessage()));
        this.tmpConfigManager = null;
    }

}
