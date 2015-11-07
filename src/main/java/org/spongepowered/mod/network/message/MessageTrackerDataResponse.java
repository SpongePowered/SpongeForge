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
package org.spongepowered.mod.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.mod.client.interfaces.IMixinGuiOverlayDebug;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;

public class MessageTrackerDataResponse implements IMessage, IMessageHandler<MessageTrackerDataResponse, IMessage> {

    private String owner;
    private String notifier;

    public MessageTrackerDataResponse() {
    }

    public MessageTrackerDataResponse(String owner, String notifier) {
        this.owner = owner;
        this.notifier = notifier;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.owner.length());
        buffer.writeBytes(this.owner.getBytes());
        buffer.writeInt(this.notifier.length());
        buffer.writeBytes(this.notifier.getBytes());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        int ownerLength = buffer.readInt();
        this.owner = new String(buffer.readBytes(ownerLength).array());
        int notifierLength = buffer.readInt();
        this.notifier = new String(buffer.readBytes(notifierLength).array());
    }

    @Override
    public IMessage onMessage(MessageTrackerDataResponse message, MessageContext ctx) {
        IMixinMinecraft spongeMc = (IMixinMinecraft) FMLClientHandler.instance().getClient();
        IMixinGuiOverlayDebug debugGui = (IMixinGuiOverlayDebug) spongeMc.getDebugGui();
        debugGui.setPlayerTrackerData(message.owner, message.notifier);
        return null;
    }

    @Override
    public String toString() {
        return String.format("SpongeMessageTrackerData - owner:%s, notifier:%s", this.owner, this.notifier);
    }
}
