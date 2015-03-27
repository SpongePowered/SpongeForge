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
package org.spongepowered.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.event.EventManager;

import javax.inject.Singleton;

@Singleton
public abstract class SpongeGame implements Game {

    private static final String API_VERSION = Objects.firstNonNull(SpongeGame.class.getPackage().getSpecificationVersion(), "UNKNOWN");
    private static final String IMPLEMENTATION_VERSION =
            Objects.firstNonNull(SpongeGame.class.getPackage().getImplementationVersion(), "UNKNOWN");

    private static final MinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion("1.8", 47);

    private final PluginManager pluginManager;
    private final EventManager eventManager;
    private final GameRegistry gameRegistry;
    private final ServiceManager serviceManager;

    protected SpongeGame(PluginManager pluginManager, EventManager eventManager, GameRegistry gameRegistry, ServiceManager serviceManager) {
        this.pluginManager = checkNotNull(pluginManager, "pluginManager");
        this.eventManager = checkNotNull(eventManager, "eventManager");
        this.gameRegistry = checkNotNull(gameRegistry, "gameRegistry");
        this.serviceManager = checkNotNull(serviceManager, "serviceManager");
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getImplementationVersion() {
        return IMPLEMENTATION_VERSION;
    }

    @Override
    public MinecraftVersion getMinecraftVersion() {
        return MINECRAFT_VERSION;
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public GameRegistry getRegistry() {
        return this.gameRegistry;
    }

    @Override
    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    @Override
    public CommandService getCommandDispatcher() {
        return this.serviceManager.provideUnchecked(CommandService.class);
    }

}
