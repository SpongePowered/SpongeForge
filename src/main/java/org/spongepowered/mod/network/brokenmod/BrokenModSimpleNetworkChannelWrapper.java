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
package org.spongepowered.mod.network.brokenmod;

import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class BrokenModSimpleNetworkChannelWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleChannelHandlerWrapper<REQ, REPLY> {

    private BrokenModData brokenModData;

    public BrokenModSimpleNetworkChannelWrapper(Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler, Side side, Class<REQ> requestType) {
        super(handler, side, requestType);
        this.onInit(side);
    }

    public BrokenModSimpleNetworkChannelWrapper(IMessageHandler<? super REQ, ? extends REPLY> handler, Side side, Class<REQ> requestType) {
        super(handler, side, requestType);
        this.onInit(side);
    }

    private void onInit(Side side) {
        this.brokenModData = new BrokenModData(() -> side);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, REQ msg) throws Exception {
        this.brokenModData.schedule(() -> {
            try {
                super.channelRead0(ctx, msg);
            } catch (Exception e) {
                throw new RuntimeException("Exception when invoking mod packet handler!", e);
            }
        });
    }

}
