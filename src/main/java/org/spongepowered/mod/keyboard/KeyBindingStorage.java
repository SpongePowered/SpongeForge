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

import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.api.util.Tuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class KeyBindingStorage {

    private final Map<String, Tuple<KeyModifier, Integer>> keyBindings = new HashMap<>();
    private final Path path;

    public KeyBindingStorage(Path path) {
        this.path = path;
    }

    public Optional<Tuple<KeyModifier, Integer>> get(String identifier) {
        return Optional.ofNullable(this.keyBindings.get(identifier));
    }

    public void put(String identifier, KeyModifier keyModifier, int keyCode) {
        this.keyBindings.put(identifier, new Tuple<>(keyModifier, keyCode));
    }

    public void save() throws IOException {
        if (Files.isDirectory(this.path)) {
            throw new IOException("Path must be a file!");
        }

        Properties properties = new Properties();
        for (Map.Entry<String, Tuple<KeyModifier, Integer>> entry : this.keyBindings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getSecond().toString();

            KeyModifier modifier = entry.getValue().getFirst();
            if (modifier != KeyModifier.NONE) {
                value += ';' + modifier.toString().toLowerCase();
            }

            properties.put(key, value);
        }

        properties.store(Files.newOutputStream(this.path), "The sponge (plugin) key binding mappings");
    }

    public void load() throws IOException {
        if (Files.isDirectory(this.path)) {
            throw new IOException("Path must be a file!");
        }
        this.keyBindings.clear();
        if (!Files.exists(this.path)) {
            return;
        }

        Properties properties = new Properties();
        properties.load(Files.newInputStream(this.path));

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            KeyModifier keyModifier = KeyModifier.NONE;

            int index = value.indexOf(';');
            if (index != -1) {
                keyModifier = KeyModifier.valueFromString(value.substring(index + 1).toUpperCase());
                value = value.substring(0, index);
            }

            int keyCode = Integer.parseInt(value);
            this.put(key, keyModifier, keyCode);
        }
    }
}
