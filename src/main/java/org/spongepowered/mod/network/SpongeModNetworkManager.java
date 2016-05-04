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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.netty.buffer.Unpooled.wrappedBuffer;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.CustomPacketRegistrationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBinding.IndexedMessageChannel;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelRegistrationException;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.network.SpongeNetworkManager;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.util.Map;
import java.util.Set;

public class SpongeModNetworkManager extends SpongeNetworkManager {

    private final Map<String, SpongeModChannelBinding> channelMap = Maps.newHashMap();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCustomPacketRegistration(CustomPacketRegistrationEvent<?> event) {
        Set<String> channels = ((IMixinNetPlayHandler) event.handler).getRegisteredChannels();
        if (event.operation.equals("REGISTER")) {
            channels.addAll(event.registrations);
            for (String channel : event.registrations) {
                SpongeImpl.postEvent(SpongeEventFactory.createChannelRegistrationEventRegister(Cause.of(NamedCause.source(event.handler)), channel));
            }
        } else if (event.operation.equals("UNREGISTER")) {
            channels.removeAll(event.registrations);
            for (String channel : event.registrations) {
                SpongeImpl.postEvent(SpongeEventFactory.createChannelRegistrationEventUnregister(Cause.of(NamedCause.source(event.handler)), channel));
            }
        }
    }

    protected static C17PacketCustomPayload getRegPacketClient(String channelName) {
        return new C17PacketCustomPayload("REGISTER", new PacketBuffer(wrappedBuffer(channelName.getBytes(Charsets.UTF_8))));
    }

    protected static C17PacketCustomPayload getUnregPacketClient(String channelName) {
        return new C17PacketCustomPayload("UNREGISTER", new PacketBuffer(wrappedBuffer(channelName.getBytes(Charsets.UTF_8))));
    }

    private <T extends SpongeModChannelBinding> T registerChannel(T channel) {
        this.channelMap.put(channel.getName(), channel);
        MinecraftServer server = MinecraftServer.getServer();
        ServerConfigurationManager players = server.getConfigurationManager();
        if (server.isDedicatedServer() && players != null) {
            // Register channel to all players (when registering server side)
            String channelName = channel.getName();
            S3FPacketCustomPayload packet = getRegPacket(channelName);
            for (EntityPlayerMP player : players.getPlayerList()) {
                if (((IMixinNetPlayHandler) player.playerNetServerHandler).getRegisteredChannels().add(channelName)) {
                    player.playerNetServerHandler.sendPacket(packet);
                    SpongeImpl.postEvent(SpongeEventFactory.createChannelRegistrationEventRegister(Cause.of(NamedCause.owner(player)), channelName));
                }

            }
        }
        if (SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            EntityPlayerSP clientPlayer = Minecraft.getMinecraft().thePlayer;
            if (clientPlayer != null) {
                // Register channel on server (when on client side)
                clientPlayer.sendQueue.addToSendQueue(getRegPacketClient(channel.getName()));
            }
        }
        return channel;
    }

    @Override
    public IndexedMessageChannel createChannel(Object plugin, String channelName) throws ChannelRegistrationException {
        SpongeIndexedMessageChannel channel;
        PluginContainer pluginContainer = checkCreateChannelArgs(plugin, channelName);
        try {
            channel = new SpongeIndexedMessageChannel(this, channelName, pluginContainer);
        } catch (Exception e) {
            throw new ChannelRegistrationException("Error registering channel \"" + channelName + "\" to " + pluginContainer, e);
        }
        return this.registerChannel(channel);
    }

    @Override
    public RawDataChannel createRawChannel(Object plugin, String channelName) throws ChannelRegistrationException {
        SpongeRawChannel channel;
        PluginContainer pluginContainer = checkCreateChannelArgs(plugin, channelName);
        try {
            channel = new SpongeRawChannel(this, channelName, pluginContainer);
        } catch (Exception e) {
            throw new ChannelRegistrationException("Error registering channel \"" + channelName + "\" to " + pluginContainer, e);
        }
        return this.registerChannel(channel);
    }

    @Override
    public void unbindChannel(ChannelBinding channel) {
        checkArgument(checkNotNull(channel, "channel") instanceof SpongeModChannelBinding, "Custom channel implementation not supported");
        SpongeModChannelBinding boundChannel = this.channelMap.remove(channel.getName());
        checkState(boundChannel != null, "Channel is already unbound");
        boundChannel.invalidate();

        ServerConfigurationManager confMgr = MinecraftServer.getServer().getConfigurationManager();
        if (confMgr != null) { // Server side
            confMgr.sendPacketToAllPlayers(getUnregPacket(channel.getName()));
        }
        if (SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            EntityPlayerSP clientPlayer = Minecraft.getMinecraft().thePlayer;
            if (clientPlayer != null) { // Client side
                clientPlayer.sendQueue.addToSendQueue(getUnregPacketClient(channel.getName()));
            }
        }
    }

    @Override
    public Set<String> getRegisteredChannels(Platform.Type side) {
        checkArgument(checkNotNull(side, "side").isKnown(), "Invalid side given");
        if (side == Platform.Type.SERVER) {
            return ImmutableSet.copyOf(NetworkRegistry.INSTANCE.channelNamesFor(Side.SERVER));
        } else {
            return ImmutableSet.copyOf(NetworkRegistry.INSTANCE.channelNamesFor(Side.CLIENT));
        }
    }

    @Override
    public boolean isChannelAvailable(String channelName) {
        checkNotNull(channelName, "channelName");
        if (channelName.startsWith("MC|") || channelName.startsWith("\u0001") || channelName.startsWith("FML")) {
            return false;
        }
        return !NetworkRegistry.INSTANCE.channelNamesFor(Side.SERVER).contains(channelName)
                && !NetworkRegistry.INSTANCE.channelNamesFor(Side.CLIENT).contains(channelName);
    }

}
