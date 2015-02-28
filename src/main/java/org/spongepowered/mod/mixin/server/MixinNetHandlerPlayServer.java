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
package org.spongepowered.mod.mixin.server;

import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.text.Collator;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.net.ChannelBuf;
import org.spongepowered.api.net.PlayerConnection;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.server.ConnectionInfo;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection {

    @Shadow
    public NetworkManager netManager;

    @Shadow
    public EntityPlayerMP playerEntity;

    @Shadow
    public MinecraftServer serverController;

    @Shadow
    public abstract void sendPacket(final Packet packetIn);

    @Override
    public Player getPlayer() {
        return (Player) this.playerEntity;
    }

    @Override
    public InetSocketAddress getAddress() {
        return ((ConnectionInfo) this.netManager).getAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return ((ConnectionInfo) this.netManager).getVirtualHost();
    }

    @Override
    public int getPing() {
        return this.playerEntity.ping;
    }

    @Override
    public void sendCustomPayload(Object plugin, String channel, ChannelBuf dataStream) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void sendCustomPayload(Object plugin, String channel, byte[] data) {
        sendPacket(new S3FPacketCustomPayload(channel, new PacketBuffer(Unpooled.wrappedBuffer(data))));
    }

    @Overwrite
    @SuppressWarnings("unchecked")
    public void processTabComplete(C14PacketTabComplete packetIn) {
        List<String> tabComplete = serverController.func_180506_a(playerEntity, packetIn.getMessage(), packetIn.func_179709_b());

        String message = packetIn.getMessage();

        if (message.startsWith("/")) {
            message = message.substring(1, message.length());

            try {
                for (String command : SpongeMod.instance.getGame().getCommandDispatcher().getSuggestions((CommandSource) playerEntity, message)) {
                    if (!message.contains(" ") && !command.startsWith("/")) {
                        tabComplete.add("/" + command);
                    } else {
                        tabComplete.add(command);
                    }
                }
            } catch (CommandException e) {
            }
        }

        Collections.sort(tabComplete, Collator.getInstance());

        playerEntity.playerNetServerHandler.sendPacket(new S3APacketTabComplete(tabComplete.toArray(new String[tabComplete.size()])));
    }

}
