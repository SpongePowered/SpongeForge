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

import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Set;

class SpongeIndexedMessageChannel extends SpongeModChannelBinding implements ChannelBinding.IndexedMessageChannel {

    private SpongeMessageCodec packetCodec;
    private final Set<Class<? extends Message>> knownClasses = Sets.newHashSet();

    public SpongeIndexedMessageChannel(ChannelRegistrar registrar, String channelName, PluginContainer owner) {
        super(registrar, channelName, owner);
    }

    @Override
    protected ChannelHandler[] getHandlers() {
        return new ChannelHandler[] {this.packetCodec = new SpongeMessageCodec()};
    }

    @Override
    public void registerMessage(Class<? extends Message> messageClass, int messageId) {
        checkValidState();
        checkArgument(!this.knownClasses.contains(checkNotNull(messageClass, "messageClass")), "Message class already registered");
        this.packetCodec.addDiscriminator(messageId, messageClass);
        this.knownClasses.add(messageClass);
    }

    @Override
    public <M extends Message> void registerMessage(Class<M> messageClass, int messageId, MessageHandler<M> handler) {
        checkNotNull(handler, "handler");
        registerMessage(messageClass, messageId);
        addHandlerForSide(Platform.Type.CLIENT, messageClass, handler);
        addHandlerForSide(Platform.Type.SERVER, messageClass, handler);
    }

    @Override
    public <M extends Message> void registerMessage(Class<M> messageClass, int messageId, Platform.Type side, MessageHandler<M> handler) {
        checkNotNull(handler, "handler");
        checkArgument(checkNotNull(side, "side").isKnown(), "Invalid side");
        registerMessage(messageClass, messageId);
        addHandlerForSide(side, messageClass, handler);
    }

    @Override
    public <M extends Message> void addHandler(Class<M> messageClass, Platform.Type side, MessageHandler<M> handler) {
        checkArgument(this.knownClasses.contains(checkNotNull(messageClass, "messageClass")), "Message class %s is not registered", messageClass);
        checkNotNull(handler, "handler");
        checkArgument(checkNotNull(side, "side").isKnown(), "Invalid side");
        addHandlerForSide(side, messageClass, handler);
    }

    @Override
    public <M extends Message> void addHandler(Class<M> messageClass, MessageHandler<M> handler) {
        checkArgument(this.knownClasses.contains(checkNotNull(messageClass, "messageClass")), "Message class %s is not registered", messageClass);
        checkNotNull(handler, "handler");
        addHandlerForSide(Platform.Type.CLIENT, messageClass, handler);
        addHandlerForSide(Platform.Type.SERVER, messageClass, handler);
    }

    private <M extends Message> void addHandlerForSide(Platform.Type side, Class<M> messageClass, MessageHandler<M> handler) {
        FMLEmbeddedChannel channel = this.channels.get(side.isClient() ? Side.CLIENT : Side.SERVER);
        String type = channel.findChannelHandlerNameForType(SpongeMessageCodec.class);
        SpongeMessageInboundHandler<M> channelHandler = new SpongeMessageInboundHandler<>(handler, messageClass, side);
        channel.pipeline().addAfter(type, handler.getClass().getName(), channelHandler);
    }

    private Message checkMessage(Message message) {
        checkArgument(this.knownClasses.contains(checkNotNull(message, "message").getClass()), "unknown message type");
        return message;
    }

    @Override
    public void sendTo(Player player, Message message) {
        super.sendTo(player, checkMessage(message));
    }

    @Override
    public void sendToServer(Message message) {
        super.sendToServer(checkMessage(message));
    }

    @Override
    public void sendToAll(Message message) {
        super.sendToAll(checkMessage(message));
    }

}
