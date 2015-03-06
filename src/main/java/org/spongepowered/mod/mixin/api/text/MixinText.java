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
package org.spongepowered.mod.mixin.api.text;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeChatComponent;
import org.spongepowered.mod.text.SpongeText;
import org.spongepowered.mod.text.action.SpongeClickAction;
import org.spongepowered.mod.text.action.SpongeHoverAction;
import org.spongepowered.mod.text.format.SpongeTextColor;

@Mixin(value = Text.class, remap = false)
public abstract class MixinText implements SpongeText {

    @Shadow protected TextColor color;
    @Shadow protected TextStyle style;
    @Shadow protected ImmutableList<Text> children;
    @Shadow protected Optional<ClickAction<?>> clickAction;
    @Shadow protected Optional<HoverAction<?>> hoverAction;
    @Shadow protected Optional<ShiftClickAction<?>> shiftClickAction;

    private IChatComponent component;
    private String json;

    protected ChatComponentStyle createComponent() {
        throw new UnsupportedOperationException();
    }

    private void initializeComponent() {
        if (this.component == null) {
            this.component = createComponent();
            ChatStyle style = this.component.getChatStyle();

            if (this.color != TextColors.NONE) {
                style.setColor(((SpongeTextColor) this.color).getHandle());
            }

            if (!this.style.isEmpty()) {
                style.setBold(this.style.isBold().orNull());
                style.setItalic(this.style.isItalic().orNull());
                style.setUnderlined(this.style.hasUnderline().orNull());
                style.setStrikethrough(this.style.hasStrikethrough().orNull());
                style.setObfuscated(this.style.isObfuscated().orNull());
            }

            if (this.clickAction.isPresent()) {
                style.setChatClickEvent(SpongeClickAction.getHandle(this.clickAction.get()));
            }

            if (this.hoverAction.isPresent()) {
                style.setChatHoverEvent(SpongeHoverAction.getHandle(this.hoverAction.get()));
            }

            if (this.shiftClickAction.isPresent()) {
                ShiftClickAction.InsertText insertion = (ShiftClickAction.InsertText) this.shiftClickAction.get();
                style.setInsertion(insertion.getResult());
            }

            for (Text child : this.children) {
                this.component.appendSibling(((SpongeText) child).toComponent());
            }
        }
    }

    private IChatComponent getHandle() {
        initializeComponent();
        return this.component;
    }

    @Override
    public IChatComponent toComponent() {
        return getHandle().createCopy(); // Mutable instances are not nice :(
    }

    @Override
    public String toPlain() {
        return ((SpongeChatComponent) getHandle()).toPlain();
    }

    @Override
    public String toJson() {
        if (this.json == null) {
            this.json = IChatComponent.Serializer.componentToJson(getHandle());
        }

        return this.json;
    }

    @Override
    public String toLegacy(char code) {
        return ((SpongeChatComponent) getHandle()).toLegacy(code);
    }

}
