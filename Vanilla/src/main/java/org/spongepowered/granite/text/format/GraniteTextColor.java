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
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.granite.registry.GraniteGameRegistry;

import java.awt.Color;

@NonnullByDefault
public class GraniteTextColor implements TextColor.Base {

    private final EnumChatFormatting handle;
    private final Color color;

    public GraniteTextColor(EnumChatFormatting handle, Color color) {
        this.handle = checkNotNull(handle, "handle");
        this.color = checkNotNull(color, "color");
    }

    public EnumChatFormatting getHandle() {
        return this.handle;
    }

    @Override
    public String getName() {
        return this.handle.name();
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override @Deprecated
    public char getCode() {
        return this.handle.formattingCode;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static GraniteTextColor of(EnumChatFormatting color) {
        return GraniteGameRegistry.enumChatColor.get(color);
    }

}
