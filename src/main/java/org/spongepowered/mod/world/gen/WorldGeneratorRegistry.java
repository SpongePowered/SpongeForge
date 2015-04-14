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
package org.spongepowered.mod.world.gen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class that handles the registry of world generators. The methods in
 * {@link GameRegistry} simply call methods on this class.
 *
 * @see GameRegistry#getType(Class, String) with {@link WorldGeneratorModifier}
 * @see GameRegistry#getAllOf(Class) with {@link WorldGeneratorModifier}
 * @see GameRegistry#registerWorldGeneratorModifier(WorldGeneratorModifier)
 */
public final class WorldGeneratorRegistry {

    private static final Logger logger = LogManager.getLogger();

    public static WorldGeneratorRegistry getInstance() {
        SpongeGameRegistry registry = (SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry();
        return registry.getWorldGeneratorRegistry();
    }

    /**
     * Map of id => modifier.
     */
    private final Map<String, WorldGeneratorModifier> modifiers = Maps.newHashMap();

    public Map<String, WorldGeneratorModifier> viewModifiersMap() {
        return Collections.unmodifiableMap(this.modifiers);
    }

    public void registerModifier(WorldGeneratorModifier modifier) {
        String id = modifier.getId();
        checkId(id, "World generator ID");
        Preconditions.checkNotNull(modifier, "modifier");

        this.modifiers.put(id, modifier);
    }

    private void checkId(String id, String subject) {
        Preconditions.checkArgument(id.indexOf(' ') == -1, subject + " " + id + " may not contain a space");
    }

    /**
     * Gets the string list for the modifiers, for saving purposes.
     *
     * @param modifiers
     *            The modifiers
     * @return The string list
     * @throws IllegalArgumentException
     *             If any of the modifiers is not registered
     */
    public ImmutableCollection<String> toIds(Collection<WorldGeneratorModifier> modifiers) {
        ImmutableList.Builder<String> ids = ImmutableList.builder();
        for (WorldGeneratorModifier modifier : modifiers) {
            Preconditions.checkNotNull(modifier, "modifier (in collection)");
            String id = modifier.getId();
            Preconditions.checkArgument(this.modifiers.containsKey(id),
                    "unregistered modifier in collection");
            ids.add(id);
        }
        return ids.build();
    }

    /**
     * Gets the world generator modifiers with the given id. If no world
     * generator modifier can be found with a certain id, a message is logged
     * and the id is skipped.
     *
     * @param ids
     *            The ids
     * @return The modifiers
     */
    public Collection<WorldGeneratorModifier> toModifiers(Collection<String> ids) {
        List<WorldGeneratorModifier> modifiers = Lists.newArrayList();
        for (String id : ids) {
            WorldGeneratorModifier modifier = this.modifiers.get(id);
            if (modifier != null) {
                modifiers.add(modifier);
            } else {
                logger.error("World generator modifier with id " + id + " not found. Missing plugin?");
            }
        }
        return modifiers;
    }

    /**
     * Checks that all modifiers are registered.
     * 
     * @param modifiers
     *            The modifiers
     * @throws IllegalArgumentException
     *             If a modifier is not registered
     */
    public void checkAllRegistered(Collection<WorldGeneratorModifier> modifiers) {
        // We simply call toIds, that checks all world generators
        toIds(modifiers);
    }

}
