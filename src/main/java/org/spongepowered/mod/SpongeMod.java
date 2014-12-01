/**
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

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.mod.guice.SpongeGuiceModule;
import org.spongepowered.mod.plugin.SpongePluginContainer;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SpongeMod extends DummyModContainer {

    public static SpongeMod instance;

    private final Game game;

    private Map<Object, PluginContainer> plugins = Maps.newHashMap();
    private EventBus eventBus;
    private LoadController controller;
    private Injector spongeInjector = Guice.createInjector(new SpongeGuiceModule());
    private Logger logger;

    // This is a special Mod, provided by the IFMLLoadingPlugin. It will be instantiated before FML scans the system
    // For mods (or plugins)
    public SpongeMod() {
        super(new ModMetadata());
        // Register our special instance creator with FML
        ModContainerFactory.instance().registerContainerType(Type.getType(Plugin.class), SpongePluginContainer.class);

        this.getMetadata().name = "SpongeAPIMod";
        this.getMetadata().modId = "SpongeAPIMod";
        SpongeMod.instance = this;
        game = spongeInjector.getInstance(Game.class);
    }

    public void registerPluginContainer(SpongePluginContainer spongePluginContainer, String pluginId, Object proxyInstance) {
        plugins.put(proxyInstance, spongePluginContainer);
        game.getEventManager().register(spongePluginContainer.getInstance(), spongePluginContainer.getInstance());
    }

    public Collection<PluginContainer> getPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    public PluginContainer getPlugin(String s) {
        Object proxy = Loader.instance().getIndexedModList().get(s);
        return plugins.get(proxy);
    }

    public Game getGame() {
        return game;
    }

    public Injector getInjector() {
        return spongeInjector;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        bus.register(game.getEventManager());
        this.eventBus = bus;
        this.controller = controller;
        return true;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
    }

    @Subscribe
    public void onInitialization(FMLInitializationEvent e) {
        SpongeGameRegistry registry = (SpongeGameRegistry) game.getRegistry();

        registry.setBlockTypes();
        registry.setItemTypes();
    }
}
