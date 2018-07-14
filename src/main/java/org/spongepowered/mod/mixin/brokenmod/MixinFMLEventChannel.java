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
package org.spongepowered.mod.mixin.brokenmod;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkEventFiringHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.mod.interfaces.IMixinFMLEventChannel;
import org.spongepowered.mod.network.brokenmod.BrokenModSimpleChannelInboundHandlerWrapper;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.EnumMap;

@Mixin(value = FMLEventChannel.class, remap = false)
public abstract class MixinFMLEventChannel implements IMixinFMLEventChannel {

    @Shadow abstract void fireRead(FMLProxyPacket msg, ChannelHandlerContext ctx);

    @Override
    public void spongeFireRead(FMLProxyPacket msg, ChannelHandlerContext ctx) {
        this.fireRead(msg, ctx);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/network/NetworkRegistry;newChannel(Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;"))
    public EnumMap<Side,FMLEmbeddedChannel> onNewChannel(NetworkRegistry networkRegistry, String name, ChannelHandler... handlers) {

        if (StaticMixinForgeHelper.shouldTakeOverModNetworking(Loader.instance().activeModContainer())) {
            handlers[0] = new BrokenModSimpleChannelInboundHandlerWrapper((FMLEventChannel) (Object) this);
        }
        return networkRegistry.newChannel(name, handlers);
    }
}
