/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.mod.text.message;

import net.minecraft.util.ChatComponentText;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;

import java.util.ArrayDeque;

public class SpongeMessageText extends SpongeMessage<String> implements Message.Text {

    @Override
    public MessageBuilder.Text builder() {
        return new SpongeMessageTextBuilder(this.content);
    }

    private SpongeMessageText(SpongeMessageTextBuilder builder) {
        super(builder);
        this.content = builder.content;
    }

    public static class SpongeMessageTextBuilder extends SpongeMessageBuilder<MessageBuilder.Text> implements MessageBuilder.Text {

        protected String content;

        public SpongeMessageTextBuilder(String text) {
            this.content = text;
            this.children = new ArrayDeque<Message>();
            this.handle = new ChatComponentText(text);
        }

        public SpongeMessageTextBuilder(ChatComponentText component) {
            this.content = component.getUnformattedText();
            this.children = new ArrayDeque<Message>();
            this.handle = component;
        }

        @Override
        public MessageBuilder.Text content(String content) {
            this.content = content;
            this.handle = new ChatComponentText(content);
            return this;
        }

        @Override
        public Message.Text build() {
            return new SpongeMessageText(this);
        }
    }
}
