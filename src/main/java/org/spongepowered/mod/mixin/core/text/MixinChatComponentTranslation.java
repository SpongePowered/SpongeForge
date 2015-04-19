/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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

import com.google.common.collect.Iterators;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.ChatComponentIterable;
import org.spongepowered.mod.text.SpongeChatComponent;
import org.spongepowered.mod.text.translation.SpongeTranslation;

import java.util.Iterator;
import java.util.List;

@Mixin(ChatComponentTranslation.class)
public abstract class MixinChatComponentTranslation extends MixinChatComponentStyle {

    @Shadow private String key;
    @Shadow private Object[] formatArgs;

    @Shadow List<IChatComponent> children;
    @Shadow abstract void ensureInitialized();

    @Override
    protected TextBuilder createBuilder() {
        return Texts.builder(new SpongeTranslation(this.key), wrapFormatArgs(this.formatArgs));
    }

    @Override
    public Iterator<IChatComponent> childrenIterator() {
        ensureInitialized();
        return Iterators.concat(this.children.iterator(), super.childrenIterator());
    }

    @Override
    public Iterable<IChatComponent> withChildren() {
        return new ChatComponentIterable(this, false);
    }

    private static Object[] wrapFormatArgs(Object... formatArgs) {
        Object[] ret = new Object[formatArgs.length];
        for (int i = 0; i < formatArgs.length; ++i) {
            if (formatArgs[i] instanceof SpongeChatComponent) {
                ret[i] = ((SpongeChatComponent) formatArgs[i]).toText();
            } else {
                ret[i] = formatArgs[i];
            }
        }
        return ret;
    }

}
