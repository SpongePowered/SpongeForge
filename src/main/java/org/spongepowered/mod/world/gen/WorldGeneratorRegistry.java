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

package org.spongepowered.mod.world.gen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class that handles the registry of world generators. The methods in
 * {@link GameRegistry} simply call methods on this class.
 *
 * @see GameRegistry#getWorldGeneratorModifier(String)
 * @see GameRegistry#getWorldGeneratorModifiers()
 * @see GameRegistry#registerWorldGeneratorModifier(PluginContainer, String,
 *      WorldGeneratorModifier)
 */
public final class WorldGeneratorRegistry {

    /**
     * Map of prefixed id => modifier.
     */
    private final Map<String, WorldGeneratorModifier> modifiers = Maps.newHashMap();
    private static final char NAMESPACE_SEPARATOR = ':';

    public Optional<WorldGeneratorModifier> getModifier(String id) {
        Optional<WorldGeneratorModifier> modifier = getFromPrefixed(id);
        if (!modifier.isPresent()) {
            modifier = getFromUnprefixed(id);
        }
        return modifier;
    }

    private Optional<WorldGeneratorModifier> getFromPrefixed(String prefixedId) {
        return Optional.fromNullable(this.modifiers.get(prefixedId));
    }

    private Optional<WorldGeneratorModifier> getFromUnprefixed(String unprefixedId) {
        // Requires a linear search. If this ever becomes a problem, a second
        // map of unprefixed keys needs to be created.
        for (Entry<String, WorldGeneratorModifier> entry : this.modifiers.entrySet()) {
            String key = entry.getKey();
            String unprefixedKey = key.substring(key.indexOf(NAMESPACE_SEPARATOR));
            if (unprefixedKey.equals(unprefixedId)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.absent();
    }

    public Collection<WorldGeneratorModifier> getModifiers() {
        return this.modifiers.values();
    }

    public void registerModifier(PluginContainer plugin, String id, WorldGeneratorModifier modifier) {
        checkId(id, "World generator ID");
        checkId(plugin.getId(), "Plugin ID");
        Preconditions.checkNotNull(modifier, "modifier");

        this.modifiers.put(plugin.getId() + NAMESPACE_SEPARATOR + id, modifier);
    }

    private void checkId(String id, String subject) {
        Preconditions.checkArgument(id.indexOf(NAMESPACE_SEPARATOR) == -1, subject + " " + id + " may not contain a colon (:)");
        Preconditions.checkArgument(id.indexOf(' ') == -1, subject + " " + id + " may not contain a space");
    }

}
