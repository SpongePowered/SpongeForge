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
package org.spongepowered.granite.text.format;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;

@NonnullByDefault
public class GraniteTextStyle extends TextStyle.Base {

    private final EnumChatFormatting handle;

    GraniteTextStyle(EnumChatFormatting handle, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underline,
                     @Nullable Boolean strikethrough, @Nullable Boolean obfuscated) {
        super(bold, italic, underline, strikethrough, obfuscated);
        this.handle = checkNotNull(handle, "handle");
    }

    @Override
    public String getName() {
        return this.handle.name();
    }

    @Override
    public char getCode() {
        return this.handle.formattingCode;
    }

    public static GraniteTextStyle of(EnumChatFormatting handle) {
        if (handle == EnumChatFormatting.RESET) {
            return new GraniteTextStyle(handle, false, false, false, false, false);
        }

        return new GraniteTextStyle(handle,
                equalsOrNull(handle, EnumChatFormatting.BOLD),
                equalsOrNull(handle, EnumChatFormatting.ITALIC),
                equalsOrNull(handle, EnumChatFormatting.UNDERLINE),
                equalsOrNull(handle, EnumChatFormatting.STRIKETHROUGH),
                equalsOrNull(handle, EnumChatFormatting.OBFUSCATED)
        );
    }

    @Nullable
    private static Boolean equalsOrNull(EnumChatFormatting handle, EnumChatFormatting check) {
        return handle == check ? true : null;
    }

}
