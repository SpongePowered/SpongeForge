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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.IMixinInitCause;
import org.spongepowered.mod.interfaces.IMixinInitMessageChannelEvent;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = PlayerEvent.PlayerLoggedInEvent.class, remap = false)
public abstract class MixinPlayerLoggedInEvent extends MixinPlayerEvent implements ClientConnectionEvent.Join, IMixinInitMessageChannelEvent, IMixinInitCause {

    private MessageFormatter formatter = new MessageFormatter();
    private boolean messageCancelled;
    private Text originalMessage;
    private MessageChannel originalChannel;
    @Nullable private MessageChannel channel;
    private Cause cause;


    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public void initCause(Cause cause) {
        this.cause = cause;
    }

    @Override
    public boolean isMessageCancelled() {
        return this.messageCancelled;
    }

    @Override
    public void setMessageCancelled(boolean cancelled) {
        this.messageCancelled = cancelled;
    }

    @Override
    public MessageFormatter getFormatter() {
        return this.formatter;
    }

    @Override
    public Text getOriginalMessage() {
        return this.originalMessage;
    }

    @Override
    public void initMessage(MessageFormatter formatter, boolean messageCancelled) {
        this.formatter = formatter;
        this.originalMessage = this.formatter.format();
        this.messageCancelled = messageCancelled;
    }

    @Override
    public MessageChannel getOriginalChannel() {
        return this.originalChannel;
    }

    @Override
    public Optional<MessageChannel> getChannel() {
        return Optional.ofNullable(this.channel);
    }

    @Override
    public void setChannel(@Nullable MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void initChannel(MessageChannel original, @Nullable MessageChannel channel) {
        this.originalChannel = checkNotNull(original, "original channel");
        this.channel = channel;
    }

}
