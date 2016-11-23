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
package org.spongepowered.mod.mixin.core.network.packet;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;

import java.io.IOException;

@Mixin(C00Handshake.class)
public abstract class MixinC00Handshake {

    @Shadow private int protocolVersion;
    @Shadow public String ip;
    @Shadow public int port;
    @Shadow private EnumConnectionState requestedState;
    @Shadow(remap = false) private boolean hasFMLMarker = false;

    /**
     * @author bloodmc, dualspiral
     *
     * Forge strips out its FML marker when clients connect. This causes
     * BungeeCord's IP forwarding data to be stripped. In order to 
     * workaround the issue, we will only strip data if the FML marker
     * is found and wasn't already in the extra data.
     */
    @Inject(method = "readPacketData(Lnet/minecraft/network/PacketBuffer;)V", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void readPacketData(PacketBuffer buf, CallbackInfo callbackInfo) throws IOException {

        // Sponge start
        this.protocolVersion = buf.readVarInt();

        if (!SpongeImpl.getGlobalConfig().getConfig().getModules().usePluginBungeeCord()
                || !SpongeImpl.getGlobalConfig().getConfig().getBungeeCord().getIpForwarding()) {
            this.ip = buf.readString(255);
        } else {
            this.ip = buf.readString(Short.MAX_VALUE);
            String split[] = this.ip.split("\0\\|", 2);
            this.ip = split[0];
            // If we have extra data, check to see if it is telling us we have a
            // FML marker
            if (split.length == 2) {
                this.hasFMLMarker = split[1].contains("\0FML\0");
            }
        }

        // Check for FML marker and strip if found, but only if it wasn't
        // already in the extra data.
        if (!this.hasFMLMarker) {
            this.hasFMLMarker = this.ip.contains("\0FML\0");
            if (this.hasFMLMarker) {
                this.ip = this.ip.split("\0")[0];
            }
        }

        this.port = buf.readUnsignedShort();
        this.requestedState = EnumConnectionState.getById(buf.readVarInt());
        // Sponge end

        callbackInfo.cancel();
    }
}
