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
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyle.TextStyleComponent;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.util.Deque;

public abstract class SpongeMessageBuilder<T extends MessageBuilder> implements MessageBuilder {

    protected Deque<Message> children;
    protected TextColor color;
    protected TextStyle style;
    protected Optional<ClickAction<?>> clickAction;
    protected Optional<HoverAction<?>> hoverAction;
    protected Optional<ShiftClickAction<?>> shiftClickAction;
    protected IChatComponent handle;

    @SuppressWarnings("unchecked")
    @Override
    public T append(Message... children) {
        for (Message message : children) {
            this.children.add(message);
            this.handle.appendSibling(((SpongeMessageText) message).handle);
        }

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T append(Iterable<Message> children) {
        for (Message message : children) {
            this.children.add(message);
            this.handle.appendSibling(((SpongeMessageText) message).handle);
        }

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T color(TextColor color) {
        this.color = color;
        this.handle.getChatStyle().setColor(SpongeGameRegistry.textColorToEnumMappings.get(color));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T style(TextStyle... styles) {
        TextStyle style = TextStyles.ZERO.and(styles);
        for (TextStyle.Base baseStyle : SpongeGameRegistry.textStyleMappings.values()) {
            TextStyleComponent component = style.applied(baseStyle);

            if (component != TextStyleComponent.UNAPPLIED) {
                if (baseStyle == TextStyles.BOLD) {
                    this.handle.getChatStyle().setBold((component == TextStyleComponent.APPLIED));
                } else if (baseStyle == TextStyles.ITALIC) {
                    this.handle.getChatStyle().setItalic((component == TextStyleComponent.APPLIED));
                } else if (baseStyle == TextStyles.OBFUSCATED) {
                    this.handle.getChatStyle().setObfuscated((component == TextStyleComponent.APPLIED));
                } else if (baseStyle == TextStyles.STRIKETHROUGH) {
                    this.handle.getChatStyle().setStrikethrough((component == TextStyleComponent.APPLIED));
                } else if (baseStyle == TextStyles.UNDERLINE) {
                    this.handle.getChatStyle().setUnderlined((component == TextStyleComponent.APPLIED));
                } else if (baseStyle == TextStyles.RESET) {
                    // TODO
                }
            }
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onClick(ClickAction<?> action) {
        this.clickAction = Optional.<ClickAction<?>>fromNullable(action);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onHover(HoverAction<?> action) {
        this.hoverAction = Optional.<HoverAction<?>>fromNullable(action);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onShiftClick(ShiftClickAction<?> action) {
        this.shiftClickAction = Optional.<ShiftClickAction<?>>fromNullable(action);
        return (T) this;
    }
}
