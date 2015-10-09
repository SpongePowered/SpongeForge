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
package org.spongepowered.mod.mixin.core.util;

import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.mod.util.StaticMixinHelper;

@Mixin(targets = "net/minecraft/network/PacketThreadUtil$1")
public class MixinPacketThreadUtil {

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V"))
    public void onProcessPacket(Packet packetIn, INetHandler netHandler) {
        StaticMixinHelper.processingPacket = packetIn;
        if (netHandler instanceof NetHandlerPlayServer) {
            StaticMixinHelper.processingPlayer = ((NetHandlerPlayServer)netHandler).playerEntity;
            IMixinWorld world = (IMixinWorld)StaticMixinHelper.processingPlayer.worldObj;
            if (StaticMixinHelper.processingPlayer.getHeldItem() != null && (packetIn instanceof C07PacketPlayerDigging || packetIn instanceof C08PacketPlayerBlockPlacement)) {
                StaticMixinHelper.lastPlayerItem = ItemStack.copyItemStack(StaticMixinHelper.processingPlayer.getHeldItem());
            }
            world.setProcessingCaptureCause(true);
            packetIn.processPacket(netHandler);
            ((IMixinWorld)StaticMixinHelper.processingPlayer.worldObj).handlePostTickCaptures(Cause.of(StaticMixinHelper.processingPlayer));
            StaticMixinHelper.processingPlayer = null;
            world.setProcessingCaptureCause(false);
        } else { // client
            packetIn.processPacket(netHandler);
        }
        StaticMixinHelper.processingPacket = null;
    }

}
