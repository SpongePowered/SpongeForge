/**
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.util.ChatComponentStyle;

import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.mod.text.message.SpongeMessageText.SpongeTextBuilder;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public abstract class SpongeMessage implements Message {

    protected final String content;
    protected final List<Message> children;
    protected final TextColor color;
    protected final TextStyle style;
    protected final Optional<ClickAction<?>> clickAction;
    protected final Optional<HoverAction<?>> hoverAction;
    protected final Optional<ShiftClickAction<?>> shiftClickAction;
    protected final ChatComponentStyle handle;

    protected SpongeMessage(SpongeTextBuilder builder) {
        this.children = ImmutableList.copyOf(builder.children);
        this.content = builder.content;
        this.color = builder.color;
        this.style = builder.style;
        this.clickAction = builder.clickAction;
        this.hoverAction = builder.hoverAction;
        this.shiftClickAction = builder.shiftClickAction;
        this.handle = builder.handle;
    }

    @Override
    public Iterator<Message> iterator() {
        List<Message> messageList = new ArrayList<Message>();
        messageList.add(this);
        messageList.addAll(children);
        return messageList.iterator();
    }

    @Override
    public TextColor getColor() {
        return color;
    }

    @Override
    public TextStyle getStyle() {
        return style;
    }

    @Override
    public List<Message> getChildren() {
        return children;
    }

    @Override
    public Optional<ClickAction<?>> getClickAction() {
        return clickAction;
    }

    @Override
    public Optional<HoverAction<?>> getHoverAction() {
        return hoverAction;
    }

    @Override
    public Optional<ShiftClickAction<?>> getShiftClickAction() {
        return shiftClickAction;
    }

    @Override
    public String getContent() {
        return content;
    }

    public ChatComponentStyle getHandle() {
        return handle;
    }
}
