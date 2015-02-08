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

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ChatComponentTranslation;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.mod.text.translation.SpongeTranslation;

import java.util.ArrayDeque;
import java.util.List;

public class SpongeMessageTranslatable extends SpongeMessage<Translation> implements Message.Translatable {

    private List<Object> arguments;

    @Override
    public MessageBuilder.Translatable builder() {
        return new SpongeMessageTranslatableBuilder(this.getContent(), this.getArguments());
    }

    private SpongeMessageTranslatable(SpongeMessageTranslatableBuilder builder) {
        super(builder);
        this.arguments = ImmutableList.copyOf(builder.arguments);
    }

    @Override
    public List<Object> getArguments() {
        return this.arguments;
    }

    public static class SpongeMessageTranslatableBuilder extends SpongeMessageBuilder<MessageBuilder.Translatable>
            implements MessageBuilder.Translatable {

        protected Translation content;
        protected Object[] arguments;

        public SpongeMessageTranslatableBuilder(String text, Object... args) {
            this.content = new SpongeTranslation(text);
            this.arguments = args;
            this.children = new ArrayDeque<Message>();
            this.handle = new ChatComponentTranslation(text, args);
        }

        public SpongeMessageTranslatableBuilder(Translation content, Object... args) {
            this.content = content;
            this.arguments = args;
            this.children = new ArrayDeque<Message>();
            this.handle = new ChatComponentTranslation(content.getId(), args);
        }

        public SpongeMessageTranslatableBuilder(ChatComponentTranslation component) {
            this.content = new SpongeTranslation(component.getUnformattedText());
            this.arguments = component.getFormatArgs();
            this.children = new ArrayDeque<Message>();
            this.handle = component;
        }

        @Override
        public Translatable content(Translation translation, Object... args) {
            this.content = translation;
            this.arguments = args;
            this.handle = new ChatComponentTranslation(translation.getId(), args);
            return this;
        }

        @Override
        public Translatable content(org.spongepowered.api.text.translation.Translatable translatable, Object... args) {
            return content(translatable.getTranslation(), args);
        }

        @Override
        public Message.Translatable build() {
            return new SpongeMessageTranslatable(this);
        }
    }

}
