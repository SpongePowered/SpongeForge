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
package org.spongepowered.mod.network;

import static com.google.common.base.Preconditions.checkState;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.network.SpongeNetworkManager.AbstractChannelBinding;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.util.EnumMap;

abstract class SpongeModChannelBinding extends AbstractChannelBinding {

    final EnumMap<Side, FMLEmbeddedChannel> channels;
    private boolean valid;

    public SpongeModChannelBinding(ChannelRegistrar registrar, String channelName, PluginContainer owner) {
        super(registrar, channelName, owner);
        this.channels = NetworkRegistry.INSTANCE.newChannel(channelName, getHandlers());
        this.valid = true;
    }

    protected abstract ChannelHandler[] getHandlers();

    protected final void checkValidState() {
        checkState(this.valid, "Channel bindng in invalid state (was it unbound?)");
    }

    protected void sendTo(Player player, Object data) {
        checkValidState();
        if (!((IMixinNetPlayHandler) ((EntityPlayerMP) player).connection).getRegisteredChannels().contains(getName())) {
            return; // Player doesn't accept this channel
        }
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        this.channels.get(Side.SERVER).writeAndFlush(data).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    protected void sendToServer(Object data) {
        checkValidState();
        if (!((IMixinNetPlayHandler) Minecraft.getMinecraft().player.connection).getRegisteredChannels().contains(getName())) {
            return; // Server doesn't accept this channel
        }
        this.channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channels.get(Side.CLIENT).writeAndFlush(data).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    protected void sendToAll(Object data) {
        checkValidState();
        // TODO Somehow make this check which players are registered to accept
        // this channel
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        this.channels.get(Side.SERVER).writeAndFlush(data).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    final void invalidate() {
        this.valid = false;
    }

}
