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
import com.google.common.collect.Lists;
import net.minecraft.network.play.server.S45PacketTitle;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.TitleBuilder;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.text.message.SpongeMessage;

import java.util.List;

@NonnullByDefault
public class SpongeTitle implements Title {
    private final boolean isClear;
    private final boolean isReset;
    private final Optional<Message> titleMessage;
    private final Optional<Message> subtitleMessage;
    private final Optional<Integer> fadeIn;
    private final Optional<Integer> stay;
    private final Optional<Integer> fadeOut;

    public SpongeTitle(boolean isClear, boolean isReset, Optional<Message> titleMessage,
                       Optional<Message> subtitleMessage, Optional<Integer> fadeIn, Optional<Integer> stay,
                       Optional<Integer> fadeOut) {
        this.isClear = isClear;
        this.isReset = isReset;
        this.titleMessage = titleMessage;
        this.subtitleMessage = subtitleMessage;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public Optional<Message> getTitle() {
        return titleMessage;
    }

    @Override
    public Optional<Message> getSubtitle() {
        return subtitleMessage;
    }

    @Override
    public Optional<Integer> getFadeIn() {
        return fadeIn;
    }

    @Override
    public Optional<Integer> getStay() {
        return stay;
    }

    @Override
    public Optional<Integer> getFadeOut() {
        return fadeOut;
    }

    @Override
    public boolean isClear() {
        return isClear || isReset;
    }

    @Override
    public boolean isReset() {
        return isReset;
    }

    @Override
    public TitleBuilder builder() {
        return new SpongeTitleBuilder(this);
    }

    public List<S45PacketTitle> getPackets() {
        List<S45PacketTitle> packets = Lists.newArrayList();

        if(isReset) {
            packets.add(new S45PacketTitle(S45PacketTitle.Type.RESET, null));
        } else if(isClear) {
            packets.add(new S45PacketTitle(S45PacketTitle.Type.CLEAR, null));
        }

        if(fadeIn.isPresent() && stay.isPresent() && fadeOut.isPresent()) {
            packets.add(new S45PacketTitle(fadeIn.get(), stay.get(), fadeOut.get()));
        }

        if(titleMessage.isPresent()) {
            SpongeMessage message = (SpongeMessage)titleMessage.get();
            packets.add(new S45PacketTitle(S45PacketTitle.Type.TITLE, message.getHandle()));
        }

        if(subtitleMessage.isPresent()) {
            SpongeMessage message = (SpongeMessage)subtitleMessage.get();
            packets.add(new S45PacketTitle(S45PacketTitle.Type.SUBTITLE, message.getHandle()));
        }

        return packets;
    }
}
