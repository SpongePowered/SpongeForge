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

import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeChatStyle;

import java.util.Arrays;

@Mixin(ChatStyle.class)
public abstract class MixinChatStyle implements SpongeChatStyle {

    @Shadow private ChatStyle parentStyle;

    @Shadow public abstract EnumChatFormatting getColor();
    @Shadow public abstract boolean getBold();
    @Shadow public abstract boolean getItalic();
    @Shadow public abstract boolean getStrikethrough();
    @Shadow public abstract boolean getUnderlined();
    @Shadow public abstract boolean getObfuscated();
    @Shadow public abstract boolean isEmpty();

    @Override
    public char[] asFormattingCode() {
        if (this.isEmpty()) {
            return this.parentStyle != null ? ((SpongeChatStyle) this.parentStyle).asFormattingCode() : ArrayUtils.EMPTY_CHAR_ARRAY;
        } else {
            char[] buf = new char[6];
            int i = 0;

            EnumChatFormatting color = getColor();
            if (color != null) {
                buf[i++] = color.formattingCode;
            }

            if (getBold()) {
                buf[i++] = EnumChatFormatting.BOLD.formattingCode;
            }

            if (getItalic()) {
                buf[i++] = EnumChatFormatting.ITALIC.formattingCode;
            }

            if (getUnderlined()) {
                buf[i++] = EnumChatFormatting.UNDERLINE.formattingCode;
            }

            if (getObfuscated()) {
                buf[i++] = EnumChatFormatting.STRIKETHROUGH.formattingCode;
            }

            if (getStrikethrough()) {
                buf[i++] = EnumChatFormatting.STRIKETHROUGH.formattingCode;
            }

            return i == buf.length ? buf : Arrays.copyOf(buf, i);
        }
    }

}
