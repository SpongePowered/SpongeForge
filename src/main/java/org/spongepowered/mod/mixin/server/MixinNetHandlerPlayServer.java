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
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.net.ChannelBuf;
import org.spongepowered.api.net.PlayerConnection;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.server.ConnectionInfo;

import com.google.common.collect.Lists;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection, INetHandlerPlayServer {

    @Shadow public NetworkManager netManager;
    @Shadow public EntityPlayerMP playerEntity;
    @Shadow private MinecraftServer serverController;
    
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

    @Override
	public void processTabComplete(C14PacketTabComplete packetIn) {
		PacketThreadUtil.func_180031_a(packetIn, this, this.playerEntity.getServerForPlayer());
		ArrayList<String> arraylist = Lists.newArrayList();

		Iterator<?> iterator = processTab(playerEntity, packetIn.getMessage()).iterator();

		while (iterator.hasNext()) {
			String value = (String) iterator.next();
			arraylist.add(value);
		}

		Iterator<?> vanillaIterator = this.serverController.func_180506_a(this.playerEntity, packetIn.getMessage(), packetIn.func_179709_b()).iterator();

		while (vanillaIterator.hasNext()) {
			String value = (String) vanillaIterator.next();
			arraylist.add(value);
		}
		this.playerEntity.playerNetServerHandler.sendPacket(new S3APacketTabComplete((String[]) arraylist.toArray(new String[arraylist.size()])));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<String> processTab(ICommandSender sender, String string) {

		ArrayList arraylist = Lists.newArrayList();

		if (string.startsWith("/")) {
			string = string.substring(1);
			boolean flag = !string.contains(" ");
			List list;
			try {
				list = SpongeMod.instance.getGame().getCommandDispatcher().getSuggestions((CommandSource) sender, string);
				if (list != null) {
					Iterator iterator = list.iterator();

					while (iterator.hasNext()) {
						String value = (String) iterator.next();

						if (flag) {
							arraylist.add("/" + value);
						} else {
							arraylist.add(value);
						}
					}
				}
			} catch (Exception e) {
			}

		}
		return arraylist;
	}
    
}
