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
package org.spongepowered.mod.text.format;

import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.api.text.format.TextColor;

import java.awt.Color;

public class SpongeTextColor implements TextColor.Base {

    private Color color;
    private String name;
    private EnumChatFormatting handle;

    public SpongeTextColor(Color color, EnumChatFormatting format) {
        this.color = color;
        this.handle = format;
        this.name = format.getFriendlyName();
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public boolean isReset() {
        return this.color == Color.WHITE;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public EnumChatFormatting getHandle() {
        return this.handle;
    }

    @Override
    @Deprecated
    public char getCode() {
        // TODO
        return 0;
    }

}
