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

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.mod.network.brokenmod.BrokenModSimpleNetworkChannelWrapper;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

@Mixin(value = SimpleNetworkWrapper.class, remap = false)
public class SimpleNetworkWrapperMixin_BrokenMod {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "getHandlerWrapper",
        at = @At(
            value = "NEW",
            target = "net/minecraftforge/fml/common/network/simpleimpl/SimpleChannelHandlerWrapper"))
    private SimpleChannelHandlerWrapper<?, ?> onCreateChannelHandler(final IMessageHandler<?, ?> messageHandler, final Side side,
        final Class<?> requestType) {
        if (StaticMixinForgeHelper.shouldTakeOverModNetworking(Loader.instance().activeModContainer())) {
            return new BrokenModSimpleNetworkChannelWrapper(messageHandler, side, requestType);
        }
        return new SimpleChannelHandlerWrapper(messageHandler, side, requestType);
    }

}
