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
package org.spongepowered.mod.mixin.core.network;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.util.Set;

@Mixin(value = NetHandlerPlayServer.class, priority = 1001)
public abstract class MixinNetHandlerPlayServer implements IMixinNetPlayHandler {

    @Shadow public EntityPlayerMP player;
    @Shadow private int chatSpamThresholdCount;

    private final Set<String> registeredChannels = Sets.newHashSet();

    @Shadow public abstract void disconnect(ITextComponent message); // disconnect

    @Override
    public int getChatSpamThresholdCount() {
        return this.chatSpamThresholdCount;
    }

    @Override
    public void setChatSpamThresholdCount(int count) {
        this.chatSpamThresholdCount = count;
    }

    @Override
    public Set<String> getRegisteredChannels() {
        return this.registeredChannels;
    }
}
