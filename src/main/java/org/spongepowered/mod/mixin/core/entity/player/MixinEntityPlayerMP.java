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
package org.spongepowered.mod.mixin.core.entity.player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinInitCause;
import org.spongepowered.common.mixin.core.entity.player.MixinEntityPlayer;
import org.spongepowered.common.text.SpongeTexts;

@Mixin(value = EntityPlayerMP.class, priority = 1001)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer implements Player {
    @Shadow private NetHandlerPlayServer connection;

    public boolean usesCustomClient() {
        return this.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
    }

    @Override
    public MessageChannelEvent.Chat simulateChat(Text message) {
        String messageRaw = SpongeTexts.toLegacy(message);
        ITextComponent itextcomponent = new TextComponentTranslation("chat.type.text", SpongeTexts.toComponent(getDisplayNameText()), net.minecraftforge.common.ForgeHooks.newChatWithLinks(messageRaw));

        EntityPlayerMP thisPlayer = (EntityPlayerMP)(Object) this;

        final ServerChatEvent event = new ServerChatEvent(thisPlayer, messageRaw, itextcomponent);
        ((IMixinInitCause) event).initCause(Cause.of(NamedCause.source(this)));

        if (!MinecraftForge.EVENT_BUS.post(event)) {
            MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) event;
            Text theMessage = spongeEvent.getMessage();
            if (!spongeEvent.isMessageCancelled()) {
                spongeEvent.getChannel().ifPresent(channel -> channel.send(this, theMessage, ChatTypes.CHAT));
            }
        }

        return (MessageChannelEvent.Chat) event;
    }
}
