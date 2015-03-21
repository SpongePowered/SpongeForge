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
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.mod.command.CommandSponge;
import org.spongepowered.mod.event.SpongeEventBus;
import org.spongepowered.mod.event.SpongeEventHooks;
import org.spongepowered.mod.guice.SpongeGuiceModule;
import org.spongepowered.mod.plugin.SpongePluginContainer;
import org.spongepowered.mod.registry.SpongeGameRegistry;
import org.spongepowered.mod.service.scheduler.AsyncScheduler;
import org.spongepowered.mod.service.scheduler.SyncScheduler;
import org.spongepowered.mod.service.sql.SqlServiceImpl;
import org.spongepowered.mod.service.persistence.SpongeSerializationService;
import org.spongepowered.mod.util.SpongeHooks;

import java.io.File;
import java.io.IOException;

public class SpongeMod extends DummyModContainer implements PluginContainer {

    public static SpongeMod instance;
    private static final Logger logger = LogManager.getLogger("Sponge");

    private final Game game;
    private final File spongeConfigDir = new File(Loader.instance().getConfigDir() + File.separator + "sponge" + File.separator);
    private Injector spongeInjector = Guice.createInjector(new SpongeGuiceModule());
    private LoadController controller;
    private SpongeGameRegistry registry;

    // This is a special Mod, provided by the IFMLLoadingPlugin. It will be
    // instantiated before FML scans the system for mods (or plugins)
    public SpongeMod() {
        super(new ModMetadata());
        // Register our special instance creator with FML
        ModContainerFactory.instance().registerContainerType(Type.getType(Plugin.class), SpongePluginContainer.class);

        this.getMetadata().name = "Sponge";
        this.getMetadata().modId = "Sponge";
        SpongeMod.instance = this;
        this.game = this.spongeInjector.getInstance(Game.class);
        this.registry = (SpongeGameRegistry) this.game.getRegistry();
        try {
            SimpleCommandService commandService = new SimpleCommandService(this.game.getPluginManager());
            this.game.getServiceManager().setProvider(this, CommandService.class, commandService);
            ((SpongeEventBus) this.game.getEventManager()).register(this, commandService);
        } catch (ProviderExistsException e) {
            logger.warn("Non-Sponge CommandService already registered: " + e.getLocalizedMessage());
        }
        try {
            game.getServiceManager().setProvider(this, SqlService.class, new SqlServiceImpl());
        } catch (ProviderExistsException e) {
            logger.warn("Non-Sponge SqlService already registered: " + e.getLocalizedMessage());
        }
        try {
            game.getServiceManager().setProvider(this, SynchronousScheduler.class, SyncScheduler.getInstance());
            game.getServiceManager().setProvider(this, AsynchronousScheduler.class, AsyncScheduler.getInstance());
        } catch (ProviderExistsException e) {
            logger.error("Non-Sponge scheduler has been registered. Cannot continue!");
            FMLCommonHandler.instance().exitJava(1, false);
        }
        try {
            SerializationService serializationService = new SpongeSerializationService();
            this.game.getServiceManager().setProvider(this, SerializationService.class, serializationService);
        } catch (ProviderExistsException e2) {
            logger.warn("Non-Sponge SerializationService already registered: " + e2.getLocalizedMessage());
        }
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

    public LoadController getController() {
        return this.controller;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getConfigDir() {
        return this.spongeConfigDir;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        this.controller = controller;
        return true;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new SpongeEventHooks());

        // Add the SyncScheduler as a listener for ServerTickEvents
        FMLCommonHandler.instance().bus().register(this.getGame().getSyncScheduler());

        if (e.getSide() == Side.SERVER) {
            SpongeHooks.enableThreadContentionMonitoring();
        }
        this.registry.preInit();
    }

    @Subscribe
    public void onInitialization(FMLInitializationEvent e) {
        this.registry.init();
    }

    @Subscribe
    public void onInitialization(FMLPostInitializationEvent e) {
        this.registry.postInit();
        SerializationService service = this.game.getServiceManager().provide(SerializationService.class).get();
        ((SpongeSerializationService) service).completeRegistration();
    }

    @Subscribe
    public void onServerStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandSponge());
    }

    @Subscribe
    public void onServerStopped(FMLServerStoppedEvent e) throws IOException {
        ((SqlServiceImpl) getGame().getServiceManager().provideUnchecked(SqlService.class)).close();
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
