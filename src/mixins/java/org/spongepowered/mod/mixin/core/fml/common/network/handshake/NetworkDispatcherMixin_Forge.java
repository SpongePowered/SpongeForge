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
package org.spongepowered.mod.mixin.core.fml.common.network.handshake;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.unix.Errors;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;

@Mixin(value = NetworkDispatcher.class, remap = false)
public class NetworkDispatcherMixin_Forge {

    @Shadow private EntityPlayerMP player;

    @Inject(method = "exceptionCaught", at = @At(value = "HEAD"))
    private void forgeImpl$printErrorforFailedPipe(final ChannelHandlerContext ctx, final Throwable cause, final CallbackInfo ci) {
        if (cause instanceof Errors.NativeIoException && "syscall:writev(..) failed: Broken pipe".equals(cause.getMessage())) {
            final String message = String.format(
                "Detected broken pipe Netty error - closing channel. This: '%s' Player: '%s' Channel: '%s'",
                this, this.player, ctx.isRemoved());
            SpongeImpl.getLogger().error(message);
            ctx.close();
        }
    }

}
