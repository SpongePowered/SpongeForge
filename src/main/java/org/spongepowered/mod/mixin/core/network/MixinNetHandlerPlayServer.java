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
package org.spongepowered.mod.mixin.core.network;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.conversation.Conversant;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.util.Set;

@Mixin(value = NetHandlerPlayServer.class, priority = 1001)
public abstract class MixinNetHandlerPlayServer implements IMixinNetPlayHandler {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public NetworkManager netManager;
    @Shadow @Final private MinecraftServer serverController;
    @Shadow public EntityPlayerMP player;
    @Shadow private int chatSpamThresholdCount;

    private final Set<String> registeredChannels = Sets.newHashSet();

    @Shadow public abstract void sendPacket(final Packet<?> packetIn);
    @Shadow public abstract void disconnect(String message);

    @Inject(method = "processChatMessage", at = @At(value = "INVOKE", target = "net.minecraftforge.common.ForgeHooks.onServerChatEvent"
            + "(Lnet/minecraft/network/NetHandlerPlayServer;Ljava/lang/String;Lnet/minecraft/util/text/ITextComponent;)"
            + "Lnet/minecraft/util/text/ITextComponent;", remap = false),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void injectChatEvent(CPacketChatMessage packetIn, CallbackInfo ci, String s, ITextComponent component) {
        final ServerChatEvent event = new ServerChatEvent(this.player, s, component);
        MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) ((IMixinEventBus) MinecraftForge.EVENT_BUS).postForgeAndCreateSpongeEvent(event);
        spongeEvent.setMessageCancelled(Sponge.getConversationManager().process((Conversant) this.player, s));
        if (!spongeEvent.isCancelled()) {
            Text message = spongeEvent.getMessage();
            if (!spongeEvent.isMessageCancelled()) {
                spongeEvent.getChannel().ifPresent(channel -> channel.send(this.player, message, ChatTypes.CHAT));
            }

            // Chat spam suppression from MC
            this.chatSpamThresholdCount += 20;
            if (this.chatSpamThresholdCount > 200 && !SpongeImpl.getServer().getPlayerList().canSendCommands(this.player.getGameProfile())) {
                this.disconnect("disconnect.spam");
            }
        }

        ci.cancel();
    }

    @Override
    public Set<String> getRegisteredChannels() {
        return this.registeredChannels;
    }
}
