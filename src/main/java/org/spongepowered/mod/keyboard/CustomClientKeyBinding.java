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
package org.spongepowered.mod.keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.text.IMixinText;
import org.spongepowered.common.keyboard.SpongeKeyBinding;
import org.spongepowered.mod.client.interfaces.IMixinGameSettings;
import org.spongepowered.mod.client.interfaces.IMixinKeyBinding;

import java.io.IOException;

import javax.annotation.Nullable;

public class CustomClientKeyBinding extends net.minecraft.client.settings.KeyBinding implements IClientKeyBinding {

    private final String id;
    private final ITextComponent displayName;
    @Nullable private ITextComponent categoryTitle;
    @Nullable private String categoryTranslationKey;

    /**
     * Creates a new custom key binding for the
     * specified key binding settings.
     *
     * @param keyBinding The key binding
     */
    public CustomClientKeyBinding(SpongeKeyBinding keyBinding, @Nullable Tuple<KeyModifier, Integer> data) {
        super("", KeyConflictContext.UNIVERSAL, data == null ? KeyModifier.NONE : data.getFirst(), data == null ? 0 : data.getSecond(), "");
        ((IMixinKeyBinding) this).setInternalId(keyBinding.getInternalId());
        this.id = keyBinding.getId();
        this.displayName = ((IMixinText) keyBinding.getDisplayName()).toComponent();
        if (keyBinding.getCategory().isDefault()) {
            this.categoryTranslationKey = "key.categories." + keyBinding.getCategory().getName();
        } else {
            this.categoryTitle = ((IMixinText) keyBinding.getCategory().getTitle()).toComponent();
        }
    }

    @Override
    public void setKeyModifierAndCode(KeyModifier keyModifier, int keyCode) {
        KeyModifier oldKeyModifier = this.getKeyModifier();
        int oldKeyCode = this.getKeyCode();
        super.setKeyModifierAndCode(keyModifier, keyCode);
        if (oldKeyModifier != keyModifier || keyCode != oldKeyCode) {
            this.saveKey(keyModifier, keyCode);
        }
    }

    @Override
    public void setKeyCode(int keyCode) {
        int oldKeyCode = this.getKeyCode();
        super.setKeyCode(keyCode);
        if (oldKeyCode != keyCode) {
            this.saveKey(this.getKeyModifier(), keyCode);
        }
    }

    private void saveKey(KeyModifier keyModifier, int keyCode) {
        KeyBindingStorage storage = ((IMixinGameSettings) Minecraft.getMinecraft().gameSettings).getKeyBindingStorage();
        storage.put(this.id, keyModifier, keyCode);
        try {
            storage.save();
        } catch (IOException e) {
            SpongeImpl.getLogger().error("An error occurred while saving the key bindings file", e);
        }
    }

    /**
     * Gets the identifier of the custom key binding.
     *
     * @return The identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the display name of the key binding.
     *
     * @return The display name
     */
    @Override
    public String getKeyDescription() {
        return this.displayName.getUnformattedText();
    }

    @Override
    public String getKeyCategory() {
        if (this.categoryTitle != null) {
            return this.categoryTitle.getUnformattedText();
        }
        return this.categoryTranslationKey;
    }

    @Override
    public String getFormattedCategory() {
        if (this.categoryTitle != null) {
            return this.categoryTitle.getUnformattedText();
        }
        return I18n.format(this.categoryTranslationKey);
    }

    @Override
    public String getFormattedDisplayName() {
        return this.displayName.getUnformattedText();
    }
}
