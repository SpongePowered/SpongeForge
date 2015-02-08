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

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ChatComponentSelector;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;
import org.spongepowered.mod.text.selector.SpongeSelector;
import org.spongepowered.mod.text.selector.SpongeSelectorType;

import java.util.ArrayDeque;
import java.util.Map;

public class SpongeMessageSelector extends SpongeMessage<org.spongepowered.api.text.selector.Selector> implements Message.Selector {

    @Override
    public MessageBuilder.Selector builder() {
        return new SpongeMessageSelectorBuilder(this.content);
    }

    private SpongeMessageSelector(SpongeMessageSelectorBuilder builder) {
        super(builder);
        this.content = builder.content;
    }

    public static class SpongeMessageSelectorBuilder extends SpongeMessageBuilder<MessageBuilder.Selector> implements MessageBuilder.Selector {

        protected org.spongepowered.api.text.selector.Selector content;

        public SpongeMessageSelectorBuilder(String selector) {
            this.content = new SpongeSelector(new SpongeSelectorType(selector), new ImmutableMap.Builder<String, String>().build(), false);
            this.children = new ArrayDeque<Message>();
            this.handle = new ChatComponentSelector(selector);
        }

        public SpongeMessageSelectorBuilder(org.spongepowered.api.text.selector.Selector selector) {
            this.content = selector;
            this.children = new ArrayDeque<Message>();
            this.handle = new ChatComponentSelector(selector.getType().getId());
        }

        public SpongeMessageSelectorBuilder(ChatComponentSelector component, Map<String, String> arguments, boolean requiresLocation) {
            this.content = new SpongeSelector(new SpongeSelectorType(component.getUnformattedText()), arguments, requiresLocation);
            this.children = new ArrayDeque<Message>();
            this.handle = component;
        }

        @Override
        public Message.Selector build() {
            return new SpongeMessageSelector(this);
        }

        @Override
        public Selector content(
                org.spongepowered.api.text.selector.Selector selector) {
            this.content = selector;
            return this;
        }
    }
}
