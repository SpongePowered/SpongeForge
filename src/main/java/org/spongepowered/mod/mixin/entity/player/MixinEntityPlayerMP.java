/**
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
import net.minecraft.world.World;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.translation.locale.Locales;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.chat.SpongeChatType;
import org.spongepowered.mod.text.message.SpongeMessage;
import org.spongepowered.mod.text.message.SpongeMessageText;

import com.mojang.authlib.GameProfile;

@NonnullByDefault
@Mixin(EntityPlayerMP.class)
@Implements(@Interface(iface = Player.class, prefix = "playermp$"))
public abstract class MixinEntityPlayerMP extends EntityPlayer {

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

    /**
     * Returns whether the {@link Player} can fly via the fly key.
     *
     * @return {@code True} if the {@link Player} is allowed to fly
     */
    public boolean playermp$getAllowFlight() {
        return this.capabilities.allowFlying;
    }

    /**
     * Sets if the {@link Player} can fly via the fly key.
     *
     * @param allowFlight {@code True} if the player is allowed to fly
     */
    public void playermp$setAllowFlight(boolean allowFlight) {
        this.capabilities.allowFlying = allowFlight;
    }

    /**
     * Gets the locale used by the player.
     *
     * @return The player's locale
     * @see Locales
     */
    public Locale playermp$getLocale() {
        return new Locale(this.translator);
    }

    /**
     * Sends the message(s) with the specified {@link ChatType} on the client.
     *
     * @param type The chat type to send the messages to
     * @param messages The message(s) to send
     */
    public void playermp$sendMessage(ChatType type, Message... messages) {
        for (Message message : messages) {
            playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S02PacketChat(((SpongeMessage)message).getHandle(), ((SpongeChatType)type).getId()));
        }
    }

    /**
     * Sends the message(s) with the specified {@link ChatType} on the client.
     *
     * @param type The chat type to send the messages to
     * @param messages The message(s) to send
     */
    public void playermp$sendMessage(ChatType type, Iterable<Message> messages) {
        for (Message message : messages) {
            playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S02PacketChat(((SpongeMessage)message).getHandle(), ((SpongeChatType)type).getId()));
        }
    }

    /**
     * Sends a {@link Title} to this player.
     *
     * @param title The {@link Title} to send to the player
     */
    public void playermp$sendTitle(Title title) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the currently displayed {@link Title} from the player and resets
     * all settings back to default values.
     */
    public void playermp$resetTitle() {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the currently displayed {@link Title} from the player's screen.
     */
    public void playermp$clearTitle() {
        throw new UnsupportedOperationException();
    }
}
