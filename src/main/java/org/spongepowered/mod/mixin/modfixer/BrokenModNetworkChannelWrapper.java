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
package org.spongepowered.mod.mixin.modfixer;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.common.SpongeImpl;

import java.util.function.Supplier;

public class BrokenModNetworkChannelWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleChannelHandlerWrapper<REQ, REPLY> {

    private final Side sideFromSponge;
    private Supplier<IThreadListener> scheduler;

    public BrokenModNetworkChannelWrapper(Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler, Side side, Class<REQ> requestType) {
        super(handler, side, requestType);
        this.sideFromSponge = side;
        this.onInit();
    }

    public BrokenModNetworkChannelWrapper(IMessageHandler<? super REQ, ? extends REPLY> handler, Side side, Class<REQ> requestType) {
        super(handler, side, requestType);
        this.sideFromSponge = side;
        this.onInit();
    }

    private void onInit() {
        if (this.sideFromSponge == Side.CLIENT) {
            Minecraft minecraft = Minecraft.getMinecraft();
            scheduler = () -> minecraft;
        } else {
            // The server is likely not yet running when mods register their network handlers
            scheduler = SpongeImpl::getServer;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, REQ msg) throws Exception {
        this.scheduler.get().addScheduledTask(() -> {
            try {
                super.channelRead0(ctx, msg);
            } catch (Exception e) {
                throw new RuntimeException("Exception when invoking mod packet handler!", e);
            }
        });
    }

}
