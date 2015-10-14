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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.common.util.SpongeHooks;

@Sharable
public class SpongeRawDataInboundHandler extends SimpleChannelInboundHandler<FMLProxyPacket> {

    private final SpongeRawChannel channel;

    public SpongeRawDataInboundHandler(SpongeRawChannel channel) {
        this.channel = channel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception {
        INetHandler iNetHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        if (iNetHandler instanceof RemoteConnection) {
            // NetHandlerPlayServer and NetHandlerPlayClient
            this.channel.handlePacket(msg, (RemoteConnection) iNetHandler);
        } else {
            ctx.fireChannelRead(msg); // Propagate message
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SpongeHooks.logSevere("SpongeRawDataInboundHandler exception", cause);
        super.exceptionCaught(ctx, cause);
    }
}
