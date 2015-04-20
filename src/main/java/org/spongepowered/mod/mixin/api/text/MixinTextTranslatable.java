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
package org.spongepowered.mod.mixin.api.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentTranslation;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeChatComponentTranslation;
import org.spongepowered.mod.text.SpongeText;
import org.spongepowered.mod.text.translation.SpongeTranslation;

import java.util.Locale;

@Mixin(value = Text.Translatable.class, remap = false)
public abstract class MixinTextTranslatable extends MixinText {

    @Shadow protected Translation translation;
    @Shadow protected ImmutableList<Object> arguments;

    @Override
    protected ChatComponentStyle createComponent(Locale locale) {
        ChatComponentTranslation ret = new ChatComponentTranslation(this.translation instanceof SpongeTranslation ? this.translation.getId() :
                this.translation.get(locale), unwrapArguments(this.arguments, locale));
        ((SpongeChatComponentTranslation) ret).setTranslation(this.translation);
        return ret;
    }

    private Object[] unwrapArguments(ImmutableList<Object> args, Locale locale) {
        Object[] ret = new Object[args.size()];
        for (int i = 0; i < args.size(); ++i) {
            final Object arg = args.get(i);
            if (arg instanceof SpongeText) {
                ret[i] = ((SpongeText) arg).toComponent(locale);
            } else {
                ret[i] = arg;
            }
        }
        return ret;
    }

}
