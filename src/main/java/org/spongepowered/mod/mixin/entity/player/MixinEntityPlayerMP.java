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
package org.spongepowered.mod.mixin.entity.player;

import java.util.Locale;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.chat.SpongeChatType;
import org.spongepowered.mod.text.message.SpongeMessage;
import org.spongepowered.mod.text.message.SpongeMessageText;

import com.mojang.authlib.GameProfile;
import org.spongepowered.mod.text.title.SpongeTitle;

@NonnullByDefault
@Mixin(EntityPlayerMP.class)
@Implements(@Interface(iface = Player.class, prefix = "playermp$"))
public abstract class MixinEntityPlayerMP extends EntityPlayer implements CommandSource {

    @Shadow
    private String translator;
    @Shadow
    public NetHandlerPlayServer playerNetServerHandler;

    public MixinEntityPlayerMP(World worldIn, GameProfile gameprofile) {
        super(worldIn, gameprofile);
    }

    public Message playermp$getDisplayName() {
        return new SpongeMessageText.SpongeTextBuilder(getName()).build();
    }

    public boolean playermp$getAllowFlight() {
        return this.capabilities.allowFlying;
    }

    public void playermp$setAllowFlight(boolean allowFlight) {
        this.capabilities.allowFlying = allowFlight;
    }

    public Locale playermp$getLocale() {
        return new Locale(this.translator);
    }

    public void playermp$sendMessage(String... messages) {
        playermp$sendMessage(ChatTypes.CHAT, messages);
    }

    public void playermp$sendMessage(Message... messages) {
        playermp$sendMessage(ChatTypes.CHAT, messages);
    }

    public void playermp$sendMessage(Iterable<Message> messages) {
        playermp$sendMessage(ChatTypes.CHAT, messages);
    }

    public void playermp$sendMessage(ChatType type, String... messages) {
        for (String string : messages) {
            ChatComponentText component = new ChatComponentText(string);
            playerNetServerHandler.sendPacket(new S02PacketChat(component, ((SpongeChatType)type).getId()));
        }
    }

    public void playermp$sendMessage(ChatType type, Message... messages) {
        for (Message message : messages) {
            playerNetServerHandler.sendPacket(new S02PacketChat(((SpongeMessage)message).getHandle(), ((SpongeChatType)type).getId()));
        }
    }

    public void playermp$sendMessage(ChatType type, Iterable<Message> messages) {
        for (Message message : messages) {
            playerNetServerHandler.sendPacket(new S02PacketChat(((SpongeMessage)message).getHandle(), ((SpongeChatType)type).getId()));
        }
    }

    public void playermp$sendTitle(Title title) {
        SpongeTitle spongeTitle = (SpongeTitle)title;

        for(S45PacketTitle packet : spongeTitle.getPackets()) {
            playerNetServerHandler.sendPacket(packet);
        }
    }

    public void playermp$resetTitle() {
        throw new UnsupportedOperationException();
    }

    public void playermp$clearTitle() {
        throw new UnsupportedOperationException();
    }
}
