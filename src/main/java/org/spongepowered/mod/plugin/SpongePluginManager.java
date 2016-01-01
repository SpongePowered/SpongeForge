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
package org.spongepowered.mod.plugin;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NonnullByDefault
public class SpongePluginManager implements PluginManager {
    private static final PluginContainer MINECRAFT_CONTAINER = new WrappedModContainer(Loader.instance().getMinecraftModContainer());

    @Override
    public Optional<PluginContainer> getPlugin(String s) {
        if (s.equalsIgnoreCase(MINECRAFT_CONTAINER.getId())) {
            return Optional.of((PluginContainer) Loader.instance().getMinecraftModContainer());
        } else {
            ModContainer container = Loader.instance().getIndexedModList().get(s);
            if (container == null) {
                for (ModContainer mod : Loader.instance().getModList()) {
                    if (mod.getModId().equalsIgnoreCase(s)) {
                        container = mod;
                        break;
                    }
                }
            }
            return Optional.ofNullable((PluginContainer) container);
        }
    }

    @Override
    public Logger getLogger(PluginContainer pluginContainer) {
        return LoggerFactory.getLogger(pluginContainer.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<PluginContainer> getPlugins() {
        return ImmutableSet.<PluginContainer>builder().add((PluginContainer) Loader.instance() // Because java 6
            .getMinecraftModContainer()).addAll((List) Loader.instance().getActiveModList()).build();
    }

    @Override
    public Optional<PluginContainer> fromInstance(Object instance) {
        checkNotNull(instance, "instance");
        if (instance instanceof ModContainer) { // For coremods that don't get to be mixed in
            return getPlugin(((ModContainer) instance).getModId());
        }
        return Optional.ofNullable((PluginContainer) Loader.instance().getReversedModObjectList().get(instance));
    }

    @Override
    public boolean isLoaded(String s) {
        return s.equalsIgnoreCase(MINECRAFT_CONTAINER.getId()) || Loader.isModLoaded(s);
    }
}
