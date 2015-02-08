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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.message.Message;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public abstract class SpongeMessage<T> implements Message {

    protected T content;
    protected final List<Message> children;
    protected final TextColor color;
    protected final TextStyle style;
    protected final Optional<ClickAction<?>> clickAction;
    protected final Optional<HoverAction<?>> hoverAction;
    protected final Optional<ShiftClickAction<?>> shiftClickAction;
    protected final IChatComponent handle;

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected SpongeMessage(SpongeMessageBuilder builder) {
        this.children = ImmutableList.copyOf(builder.children);
        this.color = builder.color;
        this.style = builder.style;
        this.clickAction = builder.clickAction;
        this.hoverAction = builder.hoverAction;
        this.shiftClickAction = builder.shiftClickAction;
        this.handle = builder.handle;
    }

    @Override
    public Iterable<Message> withChildren() {
        Deque<Message> withChildren = new ArrayDeque<Message>();
        withChildren.add(this);
        withChildren.addAll(this.children);
        return withChildren;
    }

    @Override
    public TextColor getColor() {
        return this.color;
    }

    @Override
    public TextStyle getStyle() {
        return this.style;
    }

    @Override
    public List<Message> getChildren() {
        return this.children;
    }

    @Override
    public Optional<ClickAction<?>> getClickAction() {
        return this.clickAction;
    }

    @Override
    public Optional<HoverAction<?>> getHoverAction() {
        return this.hoverAction;
    }

    @Override
    public Optional<ShiftClickAction<?>> getShiftClickAction() {
        return this.shiftClickAction;
    }

    @Override
    public T getContent() {
        return this.content;
    }

    @Override
    @Deprecated
    public String toLegacy() {
        // TODO
        return "";
    }

    @Override
    @Deprecated
    public String toLegacy(char code) {
        // TODO
        return "";
    }

    public IChatComponent getHandle() {
        return this.handle;
    }

    public static Message of(IChatComponent component) {
        return null; // TODO
    }
}
