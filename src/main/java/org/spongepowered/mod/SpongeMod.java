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
package org.spongepowered.mod;

import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
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
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.Type;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.mod.command.CommandSponge;
import org.spongepowered.mod.command.MinecraftCommandWrapper;
import org.spongepowered.mod.command.SpongeCommandDisambiguator;
import org.spongepowered.mod.event.SpongeEventHooks;
import org.spongepowered.mod.guice.SpongeGuiceModule;
import org.spongepowered.mod.interfaces.IMixinServerCommandManager;
import org.spongepowered.mod.plugin.SpongePluginContainer;
import org.spongepowered.mod.registry.SpongeGameRegistry;
import org.spongepowered.mod.service.permission.SpongeContextCalculator;
import org.spongepowered.mod.service.permission.SpongePermissionService;
import org.spongepowered.mod.service.persistence.SpongeSerializationService;
import org.spongepowered.mod.service.scheduler.AsyncScheduler;
import org.spongepowered.mod.service.scheduler.SyncScheduler;
import org.spongepowered.mod.service.sql.SqlServiceImpl;
import org.spongepowered.mod.util.SpongeHooks;
import org.spongepowered.mod.world.SpongeDimensionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
            SimpleCommandService commandService = new SimpleCommandService(this.game, new SpongeCommandDisambiguator(this.game));
            this.game.getServiceManager().setProvider(this, CommandService.class, commandService);
        } catch (ProviderExistsException e) {
            logger.warn("Non-Sponge CommandService already registered: " + e.getLocalizedMessage());
        }
        try {
            this.game.getServiceManager().setProvider(this, SqlService.class, new SqlServiceImpl());
        } catch (ProviderExistsException e) {
            logger.warn("Non-Sponge SqlService already registered: " + e.getLocalizedMessage());
        }
        try {
            this.game.getServiceManager().setProvider(this, SynchronousScheduler.class, SyncScheduler.getInstance());
            this.game.getServiceManager().setProvider(this, AsynchronousScheduler.class, AsyncScheduler.getInstance());
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
        this.game.getCommandDispatcher().register(this, CommandSponge.getCommand(this), "sponge", "sp");
    }

    @Override
    public Object getMod() {
        return this;
    }

    public Game getGame() {
        return this.game;
    }

    public EventManager getEventManager() {
        return this.game.getEventManager();
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
        try {
            MinecraftForge.EVENT_BUS.register(new SpongeEventHooks());

            this.game.getServiceManager().potentiallyProvide(PermissionService.class).executeWhenPresent(new Predicate<PermissionService>() {

                @Override
                public boolean apply(PermissionService input) {
                    input.registerContextCalculator(new SpongeContextCalculator());
                    return true;
                }
            });

            // Add the SyncScheduler as a listener for ServerTickEvents
            FMLCommonHandler.instance().bus().register(this.getGame().getSyncScheduler());

            if (e.getSide() == Side.SERVER) {
                SpongeHooks.enableThreadContentionMonitoring();
            }
            this.registry.preInit();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onInitialization(FMLInitializationEvent e) {
        try {
            this.registry.init();
            if (!this.game.getServiceManager().provide(PermissionService.class).isPresent()) {
                try {
                    this.game.getServiceManager().setProvider(this, PermissionService.class, new SpongePermissionService());
                } catch (ProviderExistsException e1) {
                    // It's a fallback, ignore
                }
            }
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onPostInitialization(FMLPostInitializationEvent e) {
        try {
            this.registry.postInit();
            SerializationService service = this.game.getServiceManager().provide(SerializationService.class).get();
            ((SpongeSerializationService) service).completeRegistration();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onServerAboutToStart(FMLServerAboutToStartEvent e) {
        registerAllEnabledWorlds();
    }

    @Subscribe
    public void onServerStarting(FMLServerStartingEvent e) {
        try {
            // Register vanilla-style commands (if necessary -- not necessary on client)
            ((IMixinServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerEarlyCommands(this.game);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onServerStarted(FMLServerStartedEvent e) {
        try {
            ((IMixinServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerLowPriorityCommands(this.game);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }

    }

    @Subscribe
    public void onServerStopped(FMLServerStoppedEvent e) throws IOException {
        try {
            CommandService service = getGame().getCommandDispatcher();
            for (CommandMapping mapping : service.getCommands()) {
                if (mapping.getCallable() instanceof MinecraftCommandWrapper) {
                    service.removeMapping(mapping);
                }
            }
            ((SqlServiceImpl) getGame().getServiceManager().provideUnchecked(SqlService.class)).close();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Override
    public String getId() {
        return getModId();
    }

    @Override
    public Object getInstance() {
        return getMod();
    }

    public SpongeGameRegistry getSpongeRegistry() {
        return this.registry;
    }

    public void registerAllEnabledWorlds() {
        File[] directoryListing = DimensionManager.getCurrentSaveRootDirectory().listFiles();
        if (directoryListing == null) {
            return;
        }

        for (File child : directoryListing) {
            File levelData = new File(child, "level_sponge.dat");
            if (!child.isDirectory() || !levelData.exists()) {
                continue;
            }

            try {
                NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(levelData));
                if (nbt.hasKey(getModId())) {
                    NBTTagCompound spongeData = nbt.getCompoundTag(getModId());
                    boolean enabled = spongeData.getBoolean("enabled");
                    boolean loadOnStartup = spongeData.getBoolean("loadOnStartup");
                    int dimensionId = spongeData.getInteger("dimensionId");
                    if (!(dimensionId == -1) && !(dimensionId == 0) && !(dimensionId == 1)) {
                        if (!enabled) {
                            getLogger().info("World "+ child.getName() + " is currently disabled. Skipping world load...");
                            continue;
                        }
                        if (!loadOnStartup) {
                            getLogger().info("World "+ child.getName() + " 'loadOnStartup' is disabled.. Skipping world load...");
                            continue;
                        }
                    } else if (dimensionId == -1) {
                        if (!MinecraftServer.getServer().getAllowNether()) {
                            continue;
                        }
                    }
                    if (spongeData.hasKey("uuid_most") && spongeData.hasKey("uuid_least")) {
                        UUID uuid = new UUID(spongeData.getLong("uuid_most"), spongeData.getLong("uuid_least"));
                        this.registry.registerWorldUniqueId(uuid, child.getName());
                    }
                    if (spongeData.hasKey("dimensionId") && spongeData.getBoolean("enabled")) {
                        int dimension = spongeData.getInteger("dimensionId");
                        for (Map.Entry<Class<? extends Dimension>, DimensionType> mapEntry : getSpongeRegistry().dimensionClassMappings
                                .entrySet()) {
                            if (mapEntry.getKey().getCanonicalName().equalsIgnoreCase(spongeData.getString("dimensionType"))) {
                                this.registry.registerWorldDimensionId(dimension, child.getName());
                                if (!DimensionManager.isDimensionRegistered(dimension)) {
                                    DimensionManager.registerDimension(dimension,
                                            ((SpongeDimensionType) mapEntry.getValue()).getDimensionTypeId());
                                }
                            }
                        }
                    } else {
                        logger.info("World " + child.getName() + " is disabled! Skipping world registration...");
                    }
                }
            } catch (Throwable t) {
                logger.error("Error during world registration.", t);
            }
        }
    }
}
