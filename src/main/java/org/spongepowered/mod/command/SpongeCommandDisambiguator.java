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
package org.spongepowered.mod.command;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.dispatcher.Disambiguator;
import org.spongepowered.api.util.command.dispatcher.SimpleDispatcher;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.mixin.plugin.CoreMixinPlugin;

import java.util.List;
import java.util.Set;

public class SpongeCommandDisambiguator implements Disambiguator {
    private final Game game;

    /**
     * Disambiguator that takes preferences from the global configuration, falling back to {@link SimpleDispatcher#FIRST_DISAMBIGUATOR}.
     *
     * @param game The game instance to be used
     */
    public SpongeCommandDisambiguator(Game game) {
        this.game = game;
    }

    @Override
    @NonnullByDefault
    public Optional<CommandMapping> disambiguate(CommandSource source, String aliasUsed, List<CommandMapping> availableOptions) {
        if (availableOptions.size() > 1) {
            final String chosenPlugin = CoreMixinPlugin.getGlobalConfig().getConfig().getCommands().getAliases().get(aliasUsed.toLowerCase());
            if (chosenPlugin != null) {
                Optional<PluginContainer> container = this.game.getPluginManager().getPlugin(chosenPlugin);
                if (!container.isPresent()) {
                    SpongeMod.instance.getLogger().warn("Unable to find plugin '" + chosenPlugin + "' for command '" + aliasUsed + "', falling back"
                            + " to default");
                } else {
                    final Set<CommandMapping> ownedCommands = this.game.getCommandDispatcher().getOwnedBy(container.get());
                    final List<CommandMapping> ownedMatchingCommands = ImmutableList.copyOf(Iterables.filter(availableOptions,
                            Predicates.in(ownedCommands)));
                    if (ownedMatchingCommands.isEmpty()) {
                        SpongeMod.instance.getLogger().warn("Plugin " + container.get().getName() + " was specified as the preferred owner for "
                                + aliasUsed + ", but does not have any such command!");
                    } else if (ownedMatchingCommands.size() > 1) {
                        throw new IllegalStateException("Plugin " + container.get().getName() + " seems to have multiple commands registered as "
                                + aliasUsed + "! This is a programming error!");
                    } else {
                        return Optional.of(ownedMatchingCommands.get(0));
                    }

                }
            }
        }
        return SimpleDispatcher.FIRST_DISAMBIGUATOR.disambiguate(source, aliasUsed, availableOptions);
    }
}
