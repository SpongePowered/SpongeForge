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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.entity.ai.SpongeEntityAICommonSuperclass;
import org.spongepowered.common.interfaces.IMixinServerCommandManager;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.service.permission.SpongeContextCalculator;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.sql.SqlServiceImpl;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.mod.event.SpongeEventHooks;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.guice.SpongeGuiceModule;
import org.spongepowered.mod.network.SpongeModMessageHandler;
import org.spongepowered.mod.plugin.SpongeModPluginContainer;
import org.spongepowered.mod.registry.SpongeForgeModuleRegistry;
import org.spongepowered.mod.service.world.SpongeChunkTicketManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class SpongeMod extends DummyModContainer implements PluginContainer {

    public static SpongeMod instance;
    private final SpongeGame game;
    private LoadController controller;
    private File modFile;

    // This is a special Mod, provided by the IFMLLoadingPlugin. It will be
    // instantiated before FML scans the system for mods (or plugins)
    public SpongeMod() {
        super(SpongeMod.createMetadata(ImmutableMap.<String, Object>of("modid", SpongeImpl.ECOSYSTEM_ID, "name", SpongeImpl.ECOSYSTEM_NAME,
                "version", "DEV")));
        // Register our special instance creator with FML
        ModContainerFactory.instance().registerContainerType(Type.getType(Plugin.class), SpongeModPluginContainer.class);

        SpongeMod.instance = this;
        this.modFile = SpongeJava6Bridge.modFile;

        // Initialize Sponge
        Guice.createInjector(new SpongeGuiceModule()).getInstance(SpongeImpl.class);

        this.game = SpongeImpl.getGame();
        VillagerRegistry.instance();
        this.game.getRegistry().preRegistryInit();

        this.game.getEventManager().registerListeners(this, this);
        RegistryHelper.setFinalStatic(Sponge.class, "game", this.game);
    }

    @Override
    public Object getMod() {
        return this;
    }

    @Override
    public File getSource() {
        return this.modFile;
    }

    public LoadController getController() {
        return this.controller;
    }

    private <T> boolean registerService(Class<T> serviceClass, T serviceImpl) {
        try {
            SpongeImpl.getGame().getServiceManager().setProvider(SpongeImpl.getPlugin(), serviceClass, serviceImpl);
            return true;
        } catch (ProviderExistsException e) {
            SpongeImpl.getLogger().warn("Non-Sponge {} already registered: {}", serviceClass.getSimpleName(), e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        this.controller = controller;
        return true;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        if (getSource().isDirectory()) {
            return FMLFolderResourcePack.class;
        } else {
            return FMLFileResourcePack.class;
        }
    }

    @Subscribe
    public void onStateEvent(FMLStateEvent event) {
        // We can't control Guava's event bus priority, so
        // we make sure to avoid double-firing here.
        if (!event.getClass().equals(FMLConstructionEvent.class)) {
            ((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post((Event) event, true);;
        }
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event) {
        try {
            registerService(ChunkTicketManager.class, new SpongeChunkTicketManager());
            SpongeBootstrap.initializeServices();
            SpongeBootstrap.initializeCommands();
            SpongeImpl.getRegistry().preInit();
            DataRegistrar.setupSerialization(SpongeImpl.getGame());
            SpongeForgeModuleRegistry.registerForgeData();
            SpongeModMessageHandler.init();

            Preconditions.checkArgument(Class.forName("org.spongepowered.api.entity.ai.task.AbstractAITask").getSuperclass().equals(SpongeEntityAICommonSuperclass.class));

            MinecraftForge.EVENT_BUS.register(new SpongeEventHooks());

            this.game.getServiceManager().potentiallyProvide(PermissionService.class)
                    .executeWhenPresent(input -> input.registerContextCalculator(new SpongeContextCalculator()));

            // Add the SyncScheduler as a listener for ServerTickEvents
            FMLCommonHandler.instance().bus().register(this);

            FMLCommonHandler.instance().bus().register(this.game.getChannelRegistrar());

            if (event.getSide().isServer()) {
                SpongeHooks.enableThreadContentionMonitoring();
            }
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            SpongeScheduler.getInstance().tickSyncScheduler();
        }
    }

    @Subscribe
    public void onInitialization(FMLInitializationEvent event) {
        try {
            SpongeImpl.getRegistry().init();
            if (!this.game.getServiceManager().provide(PermissionService.class).isPresent()) {
                try {
                    final SpongePermissionService service = new SpongePermissionService(this.game);
                    // Setup default permissions
                    service.getGroupForOpLevel(1).getSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "minecraft.selector", Tristate.TRUE);
                    service.getGroupForOpLevel(2).getSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "minecraft.commandblock", Tristate.TRUE);
                    this.game.getServiceManager().setProvider(this, PermissionService.class, service);
                } catch (ProviderExistsException e1) {
                    // It's a fallback, ignore
                }
            }
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onPostInitialization(FMLPostInitializationEvent event) {
        try {
            SpongeImpl.getRegistry().postInit();
            SpongeDataManager service = SpongeDataManager.getInstance();
            service.completeRegistration();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        SpongeImpl.getRegistry().registerAdditionals();
    }

    @Subscribe
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        try {
            SpongeBootstrap.registerWorlds();
            // Register vanilla-style commands (if necessary -- not necessary on client)
            ((IMixinServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerEarlyCommands(this.game);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onServerStarted(FMLServerStartedEvent event) {
        try {
            ((IMixinServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerLowPriorityCommands(this.game);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }

    }

    @Subscribe
    public void onServerStopped(FMLServerStoppedEvent event) throws IOException {
        try {
            CommandManager service = this.game.getCommandManager();
            for (CommandMapping mapping : service.getCommands()) {
                if (mapping.getCallable() instanceof MinecraftCommandWrapper) {
                    service.removeMapping(mapping);
                }
            }
            ((SqlServiceImpl) this.game.getServiceManager().provideUnchecked(SqlService.class)).close();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Override
    public String getId() {
        return getModId();
    }

    @Override
    public Logger getLogger() {
        return SpongeImpl.getSlf4jLogger();
    }

    @Override
    public Optional<Object> getInstance() {
        return Optional.of(this);
    }

    private static ModMetadata createMetadata(Map<String, Object> defaults) {
        try {
            return MetadataCollection.from(SpongeMod.class.getResourceAsStream("/mcmod.info"), SpongeImpl.ECOSYSTEM_ID).getMetadataForId(
                SpongeImpl.ECOSYSTEM_ID, defaults);
        } catch (Exception ex) {
            return new ModMetadata();
        }
    }

    @SuppressWarnings("rawtypes")
    public String getModIdFromClass(Class clazz) {
        String modId = clazz.getName().contains("net.minecraft.") ? "minecraft" : "unknown";
        String modPackage = clazz.getName().replace("." + clazz.getSimpleName(), "");
        for (ModContainer mc : Loader.instance().getActiveModList()) {
            if (mc.getOwnedPackages().contains(modPackage)) {
                modId = mc.getModId();
                break;
            }
        }

        return modId;
    }

    public boolean isClientThread() {
        return (Thread.currentThread().getName().equals("Client thread"));
    }
}
