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
package org.spongepowered.mod.mixin.bungee.network;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinNetworkManager;

/**
 * @author dualspiral
 *
 * Mixin that manually tells Forge that the user that is connecting is a Forge client, based on an entry in the
 * login profile.
 */
@Mixin(value = NetHandlerHandshakeTCP.class)
public abstract class MixinNetHandlerHandshakeTCP {

    @Redirect(method = "processHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;handleServerHandshake"
        + "(Lnet/minecraft/network/handshake/client/C00Handshake;Lnet/minecraft/network/NetworkManager;)Z", ordinal = 0, remap = false))
    public boolean redirectFmlCheck(FMLCommonHandler handler, C00Handshake packetIn, NetworkManager networkManager) {
        // Don't bother if the player is not allowed to log in.
        if (handler.shouldAllowPlayerLogins() && packetIn.getRequestedState() == EnumConnectionState.LOGIN) {
            Property[] pr = ((IMixinNetworkManager) networkManager).getSpoofedProfile();
            if (pr != null) {
                for (Property p : pr) {
                    if (p.getName().equalsIgnoreCase("forgeClient") && p.getValue().equalsIgnoreCase("true")) {
                        // Manually tell the system that we're a FML client.
                        networkManager.channel().attr(NetworkRegistry.FML_MARKER).set(true);
                        return true;
                    }
                }
            }
        }

        return handler.handleServerHandshake(packetIn, networkManager);
    }
}