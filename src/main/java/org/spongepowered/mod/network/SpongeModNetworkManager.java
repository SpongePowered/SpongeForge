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
import com.google.inject.Singleton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.CustomPacketRegistrationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBinding.IndexedMessageChannel;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelRegistrationException;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.network.SpongeNetworkManager;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class SpongeModNetworkManager extends SpongeNetworkManager {

    public static final EventContextKey<INetHandler> NET_HANDLER = new EventContextKey<INetHandler>() {
        @Override
        public Class<INetHandler> getAllowedType() {
            return INetHandler.class;
        }

        @Override
        public String getId() {
            return "sponge:nethandler";
        }

        @Override
        public String getName() {
            return "NetHandler";
        }
    };

    private final Map<String, SpongeModChannelBinding> channelMap = Maps.newHashMap();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCustomPacketRegistration(CustomPacketRegistrationEvent<?> event) {
        Set<String> channels = ((IMixinNetPlayHandler) event.getHandler()).getRegisteredChannels();
        ;
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        try (final CauseStackManager.StackFrame frame = isMainThread ? Sponge.getCauseStackManager().pushCauseFrame() : null) {
            if (isMainThread) {
                if (event.getHandler() instanceof NetHandlerPlayServer) {
                    frame.pushCause(((NetHandlerPlayServer) event.getHandler()).player);
                }
                frame.addContext(NET_HANDLER, event.getHandler());
            }

            if (event.getOperation().equals("REGISTER")) {
                channels.addAll(event.getRegistrations());
                for (String channel : event.getRegistrations()) {
                    final Cause
                        currentCause =
                        isMainThread ? frame.getCurrentCause() : Cause.of(EventContext.empty(), Sponge.getGame());
                    SpongeImpl.postEvent(SpongeEventFactory.createChannelRegistrationEventRegister(currentCause, channel));
                }
            } else if (event.getOperation().equals("UNREGISTER")) {
                channels.removeAll(event.getRegistrations());
                for (String channel : event.getRegistrations()) {
                    final Cause
                        currentCause =
                        isMainThread ? frame.getCurrentCause() : Cause.of(EventContext.empty(), Sponge.getGame());
                    SpongeImpl.postEvent(SpongeEventFactory.createChannelRegistrationEventUnregister(currentCause, channel));
                }
            }
        }
    }

    protected static CPacketCustomPayload getRegPacketClient(String channelName) {
        return new CPacketCustomPayload("REGISTER", new PacketBuffer(wrappedBuffer(channelName.getBytes(Charsets.UTF_8))));
    }

    protected static CPacketCustomPayload getUnregPacketClient(String channelName) {
        return new CPacketCustomPayload("UNREGISTER", new PacketBuffer(wrappedBuffer(channelName.getBytes(Charsets.UTF_8))));
    }

    private <T extends SpongeModChannelBinding> T registerChannel(T channel) {
        this.channelMap.put(channel.getName(), channel);
        if (SpongeImpl.getGame().isServerAvailable()) {
            final PlayerList playerList = SpongeImpl.getServer().getPlayerList();
            if (playerList != null) {
                // Register channel to all players (when registering server side)
                playerList.sendPacketToAllPlayers(getRegPacket(channel.getName()));
            }
        }
        if (SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
            if (clientPlayer != null) {
                // Register channel on server (when on client side)
                clientPlayer.connection.sendPacket(getRegPacketClient(channel.getName()));
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
    public Optional<ChannelBinding> getChannel(String channel) {
        return Optional.ofNullable(this.channelMap.get(channel));
    }

    @Override
    public void unbindChannel(ChannelBinding channel) {
        checkArgument(checkNotNull(channel, "channel") instanceof SpongeModChannelBinding, "Custom channel implementation not supported");
        SpongeModChannelBinding boundChannel = this.channelMap.remove(channel.getName());
        checkState(boundChannel != null, "Channel is already unbound");
        boundChannel.invalidate();
        // Remove channel from forge's registry
        NetworkRegistry.INSTANCE.channelNamesFor(Side.SERVER).remove(channel.getName());
        NetworkRegistry.INSTANCE.channelNamesFor(Side.CLIENT).remove(channel.getName());

        if (SpongeImpl.getGame().isServerAvailable()) {
            final PlayerList playerList = SpongeImpl.getServer().getPlayerList();
            if (playerList != null) { // Server side
                playerList.sendPacketToAllPlayers(getUnregPacket(channel.getName()));
            }
        }
        if (SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
            if (clientPlayer != null) { // Client side
                clientPlayer.connection.sendPacket(getUnregPacketClient(channel.getName()));
            }
        }
    }

    @Override
    public Set<String> getRegisteredChannels(Platform.Type side) {
        checkArgument(checkNotNull(side, "side").isKnown(), "Invalid side given");
        if (side == Platform.Type.SERVER) {
            return ImmutableSet.copyOf(NetworkRegistry.INSTANCE.channelNamesFor(Side.SERVER));
        }
        return ImmutableSet.copyOf(NetworkRegistry.INSTANCE.channelNamesFor(Side.CLIENT));
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
