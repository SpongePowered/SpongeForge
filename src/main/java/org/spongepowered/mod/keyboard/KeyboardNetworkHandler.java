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
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.common.keyboard.SpongeKeyBinding;
import org.spongepowered.common.network.message.MessageKeyboardData;
import org.spongepowered.mod.client.interfaces.IMixinGameSettings;
import org.spongepowered.mod.client.interfaces.IMixinKeyBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class KeyboardNetworkHandler {

    private static boolean initialized;

    public static void handleKeyboardData(MessageKeyboardData message, RemoteConnection connection, Platform.Type side) {
        if (initialized) {
            return;
        }

        // Inject the default internal ids
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        ((IMixinKeyBinding) gameSettings.keyBindAttack).setInternalId(0);
        ((IMixinKeyBinding) gameSettings.keyBindPickBlock).setInternalId(1);
        ((IMixinKeyBinding) gameSettings.keyBindUseItem).setInternalId(2);
        ((IMixinKeyBinding) gameSettings.keyBindDrop).setInternalId(3);
        for (int i = 0; i < 9; i++) {
            ((IMixinKeyBinding) gameSettings.keyBindsHotbar[i]).setInternalId(4 + i);
        }
        ((IMixinKeyBinding) gameSettings.keyBindInventory).setInternalId(13);
        ((IMixinKeyBinding) gameSettings.keyBindSwapHands).setInternalId(14);
        ((IMixinKeyBinding) gameSettings.keyBindFullscreen).setInternalId(15);
        ((IMixinKeyBinding) gameSettings.keyBindScreenshot).setInternalId(16);
        ((IMixinKeyBinding) gameSettings.keyBindSmoothCamera).setInternalId(17);
        ((IMixinKeyBinding) gameSettings.keyBindSpectatorOutlines).setInternalId(18);
        ((IMixinKeyBinding) gameSettings.keyBindTogglePerspective).setInternalId(19);
        ((IMixinKeyBinding) gameSettings.keyBindBack).setInternalId(20);
        ((IMixinKeyBinding) gameSettings.keyBindForward).setInternalId(21);
        ((IMixinKeyBinding) gameSettings.keyBindJump).setInternalId(22);
        ((IMixinKeyBinding) gameSettings.keyBindLeft).setInternalId(23);
        ((IMixinKeyBinding) gameSettings.keyBindRight).setInternalId(24);
        ((IMixinKeyBinding) gameSettings.keyBindSneak).setInternalId(25);
        ((IMixinKeyBinding) gameSettings.keyBindSprint).setInternalId(26);
        ((IMixinKeyBinding) gameSettings.keyBindChat).setInternalId(27);
        ((IMixinKeyBinding) gameSettings.keyBindCommand).setInternalId(28);
        ((IMixinKeyBinding) gameSettings.keyBindPlayerList).setInternalId(29);

        // Get the key binding storage
        KeyBindingStorage storage = ((IMixinGameSettings) Minecraft.getMinecraft().gameSettings).getKeyBindingStorage();

        // Build all the custom key bindings
        List<net.minecraft.client.settings.KeyBinding> keyBindings = new ArrayList<>(Arrays.asList(gameSettings.keyBindings));
        for (SpongeKeyBinding keyBinding : message.getKeyBindings()) {
            CustomClientKeyBinding clientKeyBinding = new CustomClientKeyBinding(
                    keyBinding, storage.get(keyBinding.getId()).orElse(null));
            keyBindings.add(clientKeyBinding);
        }
        // Update the key bindings array in the settings menu
        gameSettings.keyBindings = keyBindings.toArray(new net.minecraft.client.settings.KeyBinding[keyBindings.size()]);
        initialized = true;
    }

    public static void handleCleanup() {
        if (!initialized) {
            return;
        }
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        List<net.minecraft.client.settings.KeyBinding> keyBindings = new ArrayList<>(Arrays.asList(gameSettings.keyBindings));
        Iterator<net.minecraft.client.settings.KeyBinding> it = keyBindings.iterator();
        while (it.hasNext()) {
            net.minecraft.client.settings.KeyBinding keyBinding = it.next();
            if (keyBinding instanceof CustomClientKeyBinding) {
                ((IMixinKeyBinding) keyBinding).remove();
                it.remove();
            } else {
                ((IMixinKeyBinding) keyBinding).setInternalId(-1);
            }
        }
        // Update the key bindings array in the settings menu
        gameSettings.keyBindings = keyBindings.toArray(new net.minecraft.client.settings.KeyBinding[keyBindings.size()]);
        // Reprocess the key bindings mappings after removal
        net.minecraft.client.settings.KeyBinding.resetKeyBindingArrayAndHash();
        initialized = false;
    }
}
