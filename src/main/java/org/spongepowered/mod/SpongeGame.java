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
package org.spongepowered.mod;

import com.google.common.base.Optional;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.GameVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.service.scheduler.AsyncScheduler;
import org.spongepowered.mod.service.scheduler.SyncScheduler;

import javax.annotation.Nullable;
import javax.inject.Inject;

@NonnullByDefault
public final class SpongeGame implements Game {

    @Nullable
    private static final String apiVersion = Game.class.getPackage().getImplementationVersion();
    @Nullable
    private static final String implementationVersion = SpongeGame.class.getPackage().getImplementationVersion();
    private final PluginManager pluginManager;
    private final EventManager eventManager;
    private final GameRegistry gameRegistry;
    private final SimpleCommandService dispatcher;

    @Inject
    public SpongeGame(PluginManager plugin, EventManager event, GameRegistry registry) {
        this.pluginManager = plugin;
        this.eventManager = event;
        this.gameRegistry = registry;
        this.dispatcher = new SimpleCommandService(this.pluginManager);
    }

    @Override
    public Platform getPlatform() {
        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT:
                return Platform.CLIENT;
            default:
                return Platform.SERVER;
        }
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
    public String getAPIVersion() {
        return apiVersion != null ? apiVersion : "UNKNOWN";
    }

    @Override
    public String getImplementationVersion() {
        return implementationVersion != null ? implementationVersion : "UNKNOWN";
    }

    @Override
    public GameVersion getVersion() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public GameRegistry getRegistry() {
        return this.gameRegistry;
    }

    @Override
    public ServiceManager getServiceManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SynchronousScheduler getSyncScheduler() {
        return SyncScheduler.getInstance();
    }

    @Override
    public AsynchronousScheduler getAsyncScheduler() { return AsyncScheduler.getInstance(); }

    @Override
    public CommandService getCommandDispatcher() {
        return this.dispatcher;
    }

    @Override
    public Optional<Server> getServer() {
        return Optional.fromNullable((Server)FMLCommonHandler.instance().getMinecraftServerInstance());
    }
}
