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
package org.spongepowered.mod.mixin.core.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;
import org.lwjgl.Sys;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.network.message.MessageKeyState;
import org.spongepowered.common.network.message.SpongeMessageHandler;
import org.spongepowered.mod.client.interfaces.IMixinKeyBinding;
import org.spongepowered.mod.keyboard.IClientKeyBinding;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NonnullByDefault
@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding implements IMixinKeyBinding, Comparable<KeyBinding> {

    @Shadow public abstract String shadow$getKeyDescription();
    @Shadow public abstract String shadow$getKeyCategory();
    @Shadow public abstract boolean shadow$isKeyDown();

    @Final @Shadow private static List<KeyBinding> KEYBIND_ARRAY;
    @Final @Shadow private static KeyBindingMap HASH;
    @Shadow private boolean pressed;
    @Shadow private int pressTime;

    private boolean lastPressedState;
    private int internalId = -1;

    @Override
    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    @Override
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
        this.trySendPressedUpdate();
    }

    private void trySendPressedUpdate() {
        boolean pressed = shadow$isKeyDown();
        if (this.lastPressedState != pressed && this.internalId != -1) {
            SpongeMessageHandler.getChannel().sendToServer(new MessageKeyState(this.internalId, pressed));
        }
        this.lastPressedState = pressed;
    }

    @Override
    public void remove() {
        KEYBIND_ARRAY.remove(this);
        HASH.removeKey((KeyBinding) (Object) this);
    }

    @Override
    public String getFormattedCategory() {
        return I18n.format(shadow$getKeyCategory());
    }

    @Override
    public String getFormattedDisplayName() {
        return I18n.format(shadow$getKeyDescription());
    }

    @Overwrite
    private void unpressKey() {
        setPressed(false);
        this.pressTime = 0;
    }

    /**
     * Is actually getCategories
     *
     * @author Cybermaxke
     * @reason This is overwritten to use the key bindings that are in the array,
     * it is hard to keep track of categories in a set if one key binding
     * using that category gets removed.
     *
     * @return The category names
     */
    @Overwrite
    public static Set<String> getKeybinds() {
        return Arrays.stream(Minecraft.getMinecraft().gameSettings.keyBindings)
                .map(KeyBinding::getKeyCategory)
                .collect(Collectors.toSet());
    }

    /**
     * @author Cybermaxke
     * @reason Overwritten to delegate the pressed state change through a method,
     * which allows us to track the change and send network messages.
     *
     * @param keyCode The key code
     * @param pressed The pressed state
     */
    @Overwrite
    public static void setKeyBindState(int keyCode, boolean pressed) {
        if (keyCode != 0) {
            for (KeyBinding keyBinding : HASH.lookupAll(keyCode)) {
                ((IMixinKeyBinding) keyBinding).setPressed(pressed);
            }
        }
    }

    /**
     * @author Cybermaxke
     * @reason Overwritten to properly compare the key bindings.
     *
     * @param keyBinding The other key binding
     * @return The result
     */
    @Overwrite
    @Override
    public int compareTo(KeyBinding keyBinding) {
        int i = getFormattedCategory().compareTo(((IClientKeyBinding) keyBinding).getFormattedCategory());
        if (i == 0) {
            i = getFormattedDisplayName().compareTo(((IClientKeyBinding) keyBinding).getFormattedDisplayName());
        }
        return i;
    }
}
