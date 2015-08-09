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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.Sponge;

import java.io.IOException;

@Mixin(C00Handshake.class)
public abstract class MixinC00Handshake {

    @Shadow private int protocolVersion;
    @Shadow public String ip;
    @Shadow public int port;
    @Shadow private EnumConnectionState requestedState;
    @Shadow private boolean hasFMLMarker = false;

    /**
     * @author bloodmc
     *
     * Forge strips out its FML marker when clients connect. This causes
     * BungeeCord's IP forwarding data to be stripped. In order to 
     * workaround the issue, we will only strip data if the FML marker
     * is found.
     */
    @Overwrite
    public void readPacketData(PacketBuffer buf) throws IOException {

        // Sponge start
        this.protocolVersion = buf.readVarIntFromBuffer();

        if (!Sponge.getGlobalConfig().getConfig().getModules().usePluginBungeeCord()
                || !Sponge.getGlobalConfig().getConfig().getBungeeCord().getIpForwarding()) {
            this.ip = buf.readStringFromBuffer(255);
        } else {
            this.ip = buf.readStringFromBuffer(Short.MAX_VALUE);
        }

        // Check for FML marker and strip if found
        this.hasFMLMarker = this.ip.contains("\0FML\0");
        if (this.hasFMLMarker) {
            this.ip = this.ip.split("\0")[0];
        }

        this.port = buf.readUnsignedShort();
        this.requestedState = EnumConnectionState.getById(buf.readVarIntFromBuffer());
        // Sponge end
    }
}
