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
package org.spongepowered.mod.mixin.core.text;

import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.ChatComponentIterable;
import org.spongepowered.mod.text.SpongeChatComponent;
import org.spongepowered.mod.text.SpongeChatStyle;
import org.spongepowered.mod.text.SpongeClickEvent;
import org.spongepowered.mod.text.SpongeHoverEvent;
import org.spongepowered.mod.text.format.SpongeTextColor;

import java.util.List;

@Mixin(ChatComponentStyle.class)
public abstract class MixinChatComponentStyle implements SpongeChatComponent {

    @Shadow private ChatStyle style;
    @Shadow protected List<IChatComponent> siblings;

    private Iterable<IChatComponent> childrenIterable;

    private char[] formatting;

    protected TextBuilder createBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<IChatComponent> withChildren() {
        if (this.childrenIterable == null) {
            this.childrenIterable = new ChatComponentIterable((IChatComponent) this);
        }

        return this.childrenIterable;
    }

    private char[] getFormatting() {
        if (this.formatting == null) {
            this.formatting = ((SpongeChatStyle) this.style).asFormattingCode();
        }

        return this.formatting;
    }

    @Override
    public String toPlain() {
        StringBuilder builder = new StringBuilder();

        for (IChatComponent component : withChildren()) {
            builder.append(component.getUnformattedTextForChat());
        }

        return builder.toString();
    }

    @Override
    public String toLegacy(char code) {
        StringBuilder builder = new StringBuilder();

        for (IChatComponent component : withChildren()) {
            char[] formatting = ((MixinChatComponentStyle) component).getFormatting();
            builder.ensureCapacity(formatting.length * 2 + 16);
            for (char formattingCode : formatting) {
                builder.append(code).append(formattingCode);
            }

            builder.append(component.getUnformattedTextForChat());
            builder.append(code).append(EnumChatFormatting.RESET.formattingCode);
        }

        return builder.toString();
    }

    @Override
    public Text toText() {
        TextBuilder builder = createBuilder();

        if (this.style != null) {
            if (this.style.color != null) {
                builder.color(SpongeTextColor.of(this.style.color));
            }

            builder.style(new TextStyle(this.style.bold, this.style.italic, this.style.underlined, this.style.strikethrough, this.style.obfuscated));

            if (this.style.chatClickEvent != null) {
                builder.onClick(((SpongeClickEvent) this.style.chatClickEvent).getHandle());
            }
            if (this.style.chatHoverEvent != null) {
                builder.onHover(((SpongeHoverEvent) this.style.chatHoverEvent).getHandle());
            }
            if (this.style.insertion != null) {
                builder.onShiftClick(TextActions.insertText(this.style.insertion));
            }
        }

        for (IChatComponent child : this.siblings) {
            builder.append(((SpongeChatComponent) child).toText());
        }

        return builder.build();
    }

}
