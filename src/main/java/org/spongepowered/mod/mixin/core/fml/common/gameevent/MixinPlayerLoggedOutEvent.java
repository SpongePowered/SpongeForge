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
package org.spongepowered.mod.mixin.core.fml.common.gameevent;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;

@NonnullByDefault
@Mixin(value = PlayerEvent.PlayerLoggedOutEvent.class, remap = false)
public abstract class MixinPlayerLoggedOutEvent extends MixinPlayerEvent implements ClientConnectionEvent.Disconnect {

    private Text message;
    private Text originalMessage;
    private MessageSink messageSink;

    @Override
    public Text getOriginalMessage() {
        return this.originalMessage;
    }

    @Override
    public Text getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(Text quitMessage) {
        if (this.originalMessage == null) {
            // setNewMessage is always called before event fired
            this.originalMessage = quitMessage;
        }
        this.message = quitMessage;
    }

    @Override
    public void setSink(MessageSink sink) {
        this.messageSink = sink;
    }

    @Override
    public MessageSink getSink() {
        return this.messageSink;
    }

    @Override
    public Player getSourceEntity() {
        return (Player) this.player;
    }

}
