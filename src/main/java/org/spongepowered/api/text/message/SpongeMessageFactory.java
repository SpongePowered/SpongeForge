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

package org.spongepowered.api.text.message;

import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.mod.text.message.SpongeMessageSelector;
import org.spongepowered.mod.text.message.SpongeMessageText;
import org.spongepowered.mod.text.message.SpongeMessageTranslatable;

public class SpongeMessageFactory implements MessageFactory {

    @Override
    public MessageBuilder createEmptyBuilder() {
        return new SpongeMessageText.SpongeMessageTextBuilder("");
    }

    @Override
    public MessageBuilder.Text createTextBuilder(String text) {
        return new SpongeMessageText.SpongeMessageTextBuilder(text);
    }

    @Override
    public MessageBuilder.Translatable createTranslatableBuilder(Translation translation, Object[] args) {
        return new SpongeMessageTranslatable.SpongeMessageTranslatableBuilder(translation, args);
    }

    @Override
    public MessageBuilder.Selector createSelectorBuilder(String selector) {
        return new SpongeMessageSelector.SpongeMessageSelectorBuilder(selector);
    }

    @Override
    public MessageBuilder.Score createScoreBuilder(Object score) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message.Text createPlain(String text) {
        return createTextBuilder(text).build();
    }

    @Override
    public char getColorChar() {
        return '§';
    }

    @Override
    public Message.Text parseLegacyMessage(String text, char color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stripLegacyCodes(String text, char color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String replaceLegacyCodes(String text, char from, char to) {
        throw new UnsupportedOperationException();
    }
}
