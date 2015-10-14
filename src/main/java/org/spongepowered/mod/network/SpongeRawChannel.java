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
import static io.netty.buffer.Unpooled.buffer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.network.SpongeNetworkManager;

import java.util.EnumMap;
import java.util.Set;
import java.util.function.Consumer;

public class SpongeRawChannel extends SpongeModChannelBinding implements ChannelBinding.RawDataChannel {

    private final EnumMap<Platform.Type, Set<RawDataListener>> listeners = Maps.newEnumMap(Platform.Type.class);

    public SpongeRawChannel(ChannelRegistrar registrar, String channelName, PluginContainer owner) {
        super(registrar, channelName, owner);
        this.listeners.put(Platform.Type.CLIENT, Sets.newHashSet());
        this.listeners.put(Platform.Type.SERVER, Sets.newHashSet());
    }

    @Override
    protected ChannelHandler[] getHandlers() {
        return new ChannelHandler[] {new SpongeRawDataInboundHandler(this)};
    }

    @Override
    public void addListener(RawDataListener listener) {
        checkValidState();
        this.listeners.get(Platform.Type.SERVER).add(checkNotNull(listener, "listener"));
        this.listeners.get(Platform.Type.CLIENT).add(listener);
    }

    @Override
    public void addListener(Platform.Type side, RawDataListener listener) {
        checkValidState();
        checkArgument(checkNotNull(side, "side").isKnown(), "Invalid side");
        this.listeners.get(side).add(checkNotNull(listener, "listener"));
    }

    @Override
    public void removeListener(RawDataListener listener) {
        checkValidState();
        this.listeners.get(Platform.Type.SERVER).remove(checkNotNull(listener, "listener"));
        this.listeners.get(Platform.Type.CLIENT).remove(listener);
    }

    void handlePacket(FMLProxyPacket msg, RemoteConnection con) {
        ChannelBuf payload = SpongeNetworkManager.toChannelBuf(msg.payload());
        Platform.Type side = msg.getTarget().isClient() ? Platform.Type.CLIENT : Platform.Type.SERVER;
        Set<RawDataListener> listeners = this.listeners.get(side);
        for (RawDataListener listener : listeners) {
            listener.handlePayload(payload, con, side);
        }
    }

    private FMLProxyPacket createPacket(Consumer<ChannelBuf> payloadConsumer) {
        PacketBuffer payload = new PacketBuffer(buffer());
        checkNotNull(payloadConsumer, "payloadConsumer").accept((ChannelBuf) payload);
        return new FMLProxyPacket(payload, getName());
    }

    @Override
    public void sendTo(Player player, Consumer<ChannelBuf> payload) {
        super.sendTo(player, createPacket(payload));
    }

    @Override
    public void sendToServer(Consumer<ChannelBuf> payload) {
        super.sendToServer(createPacket(payload));
    }

    @Override
    public void sendToAll(Consumer<ChannelBuf> payload) {
        super.sendToAll(createPacket(payload));
    }
}
