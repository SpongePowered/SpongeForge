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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.mod.event.SpongeEventHooks;
import org.spongepowered.mod.guice.SpongeGuiceModule;
import org.spongepowered.mod.plugin.SpongePluginContainer;
import org.spongepowered.mod.registry.SpongeGameRegistry;

public class SpongeMod extends DummyModContainer implements PluginContainer {

    public static SpongeMod instance;

    private final Game game;
    @SuppressWarnings("unused")
    private Injector spongeInjector = Guice.createInjector(new SpongeGuiceModule());
    private Logger logger;
    private SpongeGameRegistry registry;

    // This is a special Mod, provided by the IFMLLoadingPlugin. It will be instantiated before FML scans the system
    // For mods (or plugins)
    public SpongeMod() {
        super(new ModMetadata());
        // Register our special instance creator with FML
        ModContainerFactory.instance().registerContainerType(Type.getType(Plugin.class), SpongePluginContainer.class);

        this.getMetadata().name = "SpongeAPIMod";
        this.getMetadata().modId = "SpongeAPIMod";
        SpongeMod.instance = this;
        this.game = this.spongeInjector.getInstance(Game.class);
        this.registry = (SpongeGameRegistry) this.game.getRegistry();
    }

    @Override
    public Object getMod() {
        return this;
    }

    public Game getGame() {
        return this.game;
    }

    public Injector getInjector() {
        return this.spongeInjector;
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent e) {
        this.logger = e.getModLog();
        MinecraftForge.EVENT_BUS.register(new SpongeEventHooks());

        // Add the SyncScheduler as a listener for ServerTickEvents
        FMLCommonHandler.instance().bus().register(this.getGame().getSyncScheduler());
    }

    @Subscribe
    public void onInitialization(FMLInitializationEvent e) {
        this.registry.init();
        try {
            getGame().getServiceManager().setProvider(this, CommandService.class, new SimpleCommandService(getGame().getPluginManager()));
        } catch (ProviderExistsException e1) {
            this.logger.warn("Non-Sponge CommandService already registered: " + e1.getLocalizedMessage());
        }
    }

    @Subscribe
    public void onInitialization(FMLPostInitializationEvent e) {
        this.registry.postInit();
    }

    @Override
    public String getId() {
        return getModId();
    }

    @Override
    public Object getInstance() {
        return getMod();
    }
}
