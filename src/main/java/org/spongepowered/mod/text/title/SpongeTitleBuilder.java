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

package org.spongepowered.mod.text.title;

import com.google.common.base.Optional;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.TitleBuilder;
import org.spongepowered.api.util.annotation.NonnullByDefault;


@NonnullByDefault
public class SpongeTitleBuilder implements TitleBuilder {

    private boolean isClear = false;
    private boolean isReset = false;
    private Optional<Message> titleMessage = Optional.absent();
    private Optional<Message> subtitleMessage = Optional.absent();
    private Optional<Integer> fadeIn = Optional.absent();
    private Optional<Integer> stay = Optional.absent();
    private Optional<Integer> fadeOut = Optional.absent();

    public SpongeTitleBuilder() {

    }

    public SpongeTitleBuilder(Title title) {
        this.isReset = title.isReset();
        this.isClear = title.isClear() && !this.isReset;

        this.titleMessage = title.getTitle();
        this.subtitleMessage = title.getSubtitle();

        this.fadeIn = title.getFadeIn();
        this.stay = title.getStay();
        this.fadeOut = title.getFadeOut();
    }

    @Override
    public TitleBuilder title(Message message) {
        this.titleMessage = Optional.of(message);
        return this;
    }

    @Override
    public TitleBuilder subtitle(Message message) {
        this.subtitleMessage = Optional.of(message);
        return this;
    }

    @Override
    public TitleBuilder fadeIn(int ticks) {
        this.fadeIn = Optional.of(ticks);
        return this;
    }

    @Override
    public TitleBuilder stay(int ticks) {
        this.stay = Optional.of(ticks);
        return this;
    }

    @Override
    public TitleBuilder fadeOut(int ticks) {
        this.fadeOut = Optional.of(ticks);
        return this;
    }

    @Override
    public TitleBuilder clear() {
        this.isClear = true;
        doClear();
        return this;
    }

    @Override
    public TitleBuilder reset() {
        this.isClear = false;
        this.isReset = true;
        doClear();
        doReset();
        return this;
    }

    @Override
    public Title build() {
        if (this.fadeIn.isPresent() || this.stay.isPresent() || this.fadeOut.isPresent()) {
            if (!(this.fadeIn.isPresent() && this.stay.isPresent() && this.fadeOut.isPresent())) {
                // We don't actually know the client's current settings and cannot update only one of the three.
                throw new IllegalStateException("fadeIn, stay, and fadeOut must all be specified if one is");
            }
        }

        return new SpongeTitle(this.isClear, this.isReset, this.titleMessage, this.subtitleMessage, this.fadeIn, this.stay, this.fadeOut);
    }

    private void doClear() {
        this.titleMessage = Optional.absent();
        this.subtitleMessage = Optional.absent();
    }

    private void doReset() {
        this.fadeIn = Optional.absent();
        this.stay = Optional.absent();
        this.fadeOut = Optional.absent();
    }
}
