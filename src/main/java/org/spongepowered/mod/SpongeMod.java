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
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Stage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.CertificateHelper;
import net.minecraftforge.fml.common.LoadController;
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
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeInternalListeners;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.entity.ai.SpongeEntityAICommonSuperclass;
import org.spongepowered.common.inject.SpongeGuice;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.interfaces.IMixinServerCommandManager;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.item.recipe.crafting.SpongeCraftingRecipeRegistry;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.PotionEffectTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.SoundRegistryModule;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.registry.type.item.EnchantmentRegistryModule;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.service.permission.SpongeContextCalculator;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.sql.SqlServiceImpl;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;
import org.spongepowered.mod.event.SpongeEventHooks;
import org.spongepowered.mod.inject.SpongeForgeModule;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.network.SpongeModMessageHandler;
import org.spongepowered.mod.plugin.MetaModContainer;
import org.spongepowered.mod.plugin.SpongeModPluginContainer;
import org.spongepowered.mod.registry.SpongeForgeModuleRegistry;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;
import org.spongepowered.mod.registry.SpongeGameData;
import org.spongepowered.mod.service.world.SpongeChunkTicketManager;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;

public class SpongeMod extends MetaModContainer {

    public static SpongeMod instance;

    // USED ONLY TO KEEP TRACK OF THE THREAD. SINCE CLIENTS CAN HAVE MULTIPLE SERVERS
    // WE NEED TO BE ABLE TO STORE A REFERENCE TO THE THREAD TO MAINTAIN SPEED OF ISMAINTHREAD CHECKS
    @javax.annotation.Nullable public static Thread SERVVER_THREAD;

    @Inject private SpongeGame game;
    @Inject private SpongeScheduler scheduler;
    @Inject private Logger logger;

    private LoadController controller;
    private File modFile;

    // treat this field as final
    private static String EXPECTED_CERTIFICATE_FINGERPRINT = "@expected_certificate_fingerprint@";
    private Certificate certificate;

    // Updating
    private @MonotonicNonNull URL updateJsonUrl;

    // This is a special Mod, provided by the IFMLLoadingPlugin. It will be
    // instantiated before FML scans the system for mods (or plugins)
    public SpongeMod() throws Exception {
        super(SpongeModMetadata.getSpongeForgeMetadata());

        this.readMetadata();

        // Register our special instance creator with FML
        ModContainerFactory.instance().registerContainerType(Type.getType(Plugin.class), SpongeModPluginContainer.class);

        SpongeMod.instance = this;
        this.modFile = SpongeCoremod.modFile;

        // Initialize Sponge
        final Stage stage = SpongeGuice.getInjectorStage((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") ? Stage.DEVELOPMENT : Stage.PRODUCTION);
        // Do not replace `SpongeImpl.getLogger()` with `this.getLogger()`. You've been warned.
        SpongeImpl.getLogger().info("Creating injector in stage '{}'", stage);
        Guice.createInjector(stage, new SpongeModule(), new SpongeForgeModule());

        SpongeImpl.getRegistry().preRegistryInit();
        SpongeGameData.addRegistryCallback(ForgeRegistries.BLOCKS, (owner, manager, id, obj, oldObj) -> {
            final ResourceLocation key = ForgeRegistries.BLOCKS.getKey(obj);
            if (key == null || ((IMixinBlock) obj).isDummy()) {
                return;
            }
            BlockTypeRegistryModule.getInstance().registerFromGameData(key, (BlockType) obj);

        });
        SpongeGameData.addRegistryCallback(ForgeRegistries.ITEMS, (owner, manager, id, obj, oldObj) -> {
            final ResourceLocation key = ForgeRegistries.ITEMS.getKey(obj);
            if (key == null) {
                return;
            }
            ItemTypeRegistryModule.getInstance().registerFromGameData(key.toString(),
                    (ItemType) obj);
        });
        SpongeGameData.addRegistryCallback(ForgeRegistries.ENCHANTMENTS, (owner, manager, id, obj, oldObj) -> {
            final ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(obj);
            if (key == null) {
                return;
            }
            EnchantmentRegistryModule.getInstance().registerFromGameData(key, (EnchantmentType) obj);
        });
        SpongeGameData.addRegistryCallback(ForgeRegistries.POTIONS, (owner, manager, id, obj, oldObj) -> {
            final ResourceLocation key = ForgeRegistries.POTIONS.getKey(obj);
            if (key == null) {
                return;
            }
            PotionEffectTypeRegistryModule.getInstance().registerFromGameData(key,
                    (PotionEffectType) obj);
        });
        SpongeGameData.addRegistryCallback(ForgeRegistries.VILLAGER_PROFESSIONS, ((owner, manager, id, obj, oldObj) -> {
            final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) obj;
            if (mixinProfession.getSpongeProfession().isPresent()) {
                return;
            }
            final SpongeProfession spongeProfession = new SpongeProfession(id, mixinProfession.getId(), mixinProfession.getProfessionName());
            mixinProfession.setSpongeProfession(spongeProfession);
            ProfessionRegistryModule.getInstance().registerAdditionalCatalog(spongeProfession);
            for (VillagerRegistry.VillagerCareer villagerCareer : mixinProfession.getCareers()) {
                SpongeForgeVillagerRegistry.fromNative(villagerCareer);
            }
        }));
        SpongeGameData.addRegistryCallback(ForgeRegistries.SOUND_EVENTS, (owner, manager, id, obj, oldObj) ->
                SoundRegistryModule.inst().registerAdditionalCatalog((SoundType) obj)
        );
        SpongeForgeModuleRegistry.registerForgeData();

        this.game.getEventManager().registerListeners(this, this);
        SpongeImpl.getInternalPlugins().add((PluginContainer) ForgeModContainer.getInstance());
    }

    private void readMetadata() {
        final ModMetadata metadata = this.getMetadata();
        if (!Strings.isNullOrEmpty(metadata.updateJSON)) {
            try {
                this.updateJsonUrl = new URL(metadata.updateJSON);
            } catch (final MalformedURLException e) {
                this.getLogger().warn("Encountered an exception while constructing version check data URL", e);
            }
        }
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

    private <T> void registerService(Class<T> serviceClass, T serviceImpl) {
        SpongeImpl.getGame().getServiceManager().setProvider(SpongeImpl.getPlugin(), serviceClass, serviceImpl);
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        this.controller = controller;
        return true;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        if (this.getSource().isDirectory()) {
            return FMLFolderResourcePack.class;
        }
        return FMLFileResourcePack.class;
    }

    @Subscribe
    public void onStateEvent(FMLStateEvent event) {
        // We can't control Guava's event bus priority, so
        // we make sure to avoid double-firing here.
        if (!event.getClass().equals(FMLConstructionEvent.class)) {
            SpongeImpl.postEvent((Event) event, true);
        }
    }

    @Subscribe
    public void construction(final FMLConstructionEvent event) {
        this.checkFingerprint();
    }

    // CSI: Sponge
    private void checkFingerprint() {
        final Certificate[] certificates = this.getClass().getProtectionDomain().getCodeSource().getCertificates();
        final List<String> fingerprints = CertificateHelper.getFingerprints(certificates);
        if (((Boolean) Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false))) {
            SpongeImpl.getLogger().debug("Skipping certificate fingerprint check - we're in a deobfuscated environment");
            return;
        }
        if (!EXPECTED_CERTIFICATE_FINGERPRINT.isEmpty()) {
            if (!fingerprints.contains(EXPECTED_CERTIFICATE_FINGERPRINT)) {
                final PrettyPrinter pp = new PrettyPrinter(60).wrapTo(60);
                pp.add("Uh oh! Something's fishy here.").centre().hr();
                pp.addWrapped("It looks like we didn't find the certificate fingerprint we were expecting.");
                pp.add();
                pp.add("%s: %s", "Expected Fingerprint", EXPECTED_CERTIFICATE_FINGERPRINT);
                if (fingerprints.size() > 1) {
                    pp.add("Actual Fingerprints:");
                    for (final String fingerprint : fingerprints) {
                        pp.add(" - %s", fingerprint);
                    }
                } else {
                    pp.add("%s: %s", "Actual Fingerprint", fingerprints.get(0));
                }
                pp.log(SpongeImpl.getLogger(), Level.ERROR);
            } else {
                this.certificate = certificates[fingerprints.indexOf(EXPECTED_CERTIFICATE_FINGERPRINT)];
            }
        } else {
            SpongeImpl.getLogger().warn("There's no certificate fingerprint available");
        }
    }

    @Override
    public Certificate getSigningCertificate() {
        return this.certificate;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event) {
        try {
            SpongeImpl.getGame().getEventManager().registerListeners(SpongeImpl.getPlugin().getInstance().get(), SpongeInternalListeners.getInstance());
            this.registerService(ChunkTicketManager.class, new SpongeChunkTicketManager());
            SpongeBootstrap.initializeServices();
            SpongeBootstrap.initializeCommands();
            SpongeImpl.getRegistry().preInit();
            SpongeModMessageHandler.init();

            Preconditions.checkArgument(Class.forName("org.spongepowered.api.entity.ai.task.AbstractAITask").getSuperclass().equals(SpongeEntityAICommonSuperclass.class));

            MinecraftForge.EVENT_BUS.register(new SpongeEventHooks());

            SpongeInternalListeners.getInstance().registerServiceCallback(PermissionService.class,
                    input -> input.registerContextCalculator(new SpongeContextCalculator()));

            // Add the SyncScheduler as a listener for ServerTickEvents
            MinecraftForge.EVENT_BUS.register(this);

            MinecraftForge.EVENT_BUS.register(this.game.getChannelRegistrar());

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
            this.scheduler.tickSyncScheduler();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // If we haven't launched the integrated server, allow the sync scheduler to still pulse tasks
        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
            if (event.phase == TickEvent.Phase.START) {
                this.scheduler.tickSyncScheduler();
            }
        }
    }

    @SubscribeEvent
    public void onRecipeRegister(RegistryEvent.Register<IRecipe> event) {
        for (CraftingRecipe craftingRecipe : SpongeCraftingRecipeRegistry.getInstance().getCustomRecipes()) {
            event.getRegistry().register((IRecipe) craftingRecipe);
        }
        SpongeCraftingRecipeRegistry.getInstance().disableRegistrations();
    }

    @SubscribeEvent
    public void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
        for (EntityTypeRegistryModule.FutureRegistration registration : EntityTypeRegistryModule.getInstance().getCustomEntities()) {
            EntityRegistry.registerModEntity(registration.name, registration.type, registration.name.getPath(), registration.id,
                    registration.name.getNamespace(), 0, 0, false);
        }
    }

    @Subscribe
    public void onInitialization(FMLInitializationEvent event) {
        try {
            SpongeImpl.getRegistry().init();
            if (!this.game.getServiceManager().provide(PermissionService.class).isPresent()) {
                final SpongePermissionService service = new SpongePermissionService(this.game);
                this.game.getServiceManager().setProvider(this, PermissionService.class, service);
            }
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onPostInitialization(FMLPostInitializationEvent event) {
        try {
            SpongeImpl.getRegistry().postInit();
            SpongeImpl.getConfigSaveManager().flush();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Subscribe
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        SpongeImpl.getRegistry().registerAdditionals();
        for (EntityEntry entry : ForgeRegistries.ENTITIES) {
            StaticMixinForgeHelper.registerCustomEntity(entry);
        }

    }

    @Subscribe
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        try {
            try {
                ((IMixinServerCommandManager) SpongeImpl.getServer().getCommandManager()).registerLowPriorityCommands(this.game);
            } catch (Throwable t) {
                this.controller.errorOccurred(this, t);
            }

            // Register vanilla-style commands (if necessary -- not necessary on client)
            ((IMixinServerCommandManager) SpongeImpl.getServer().getCommandManager()).registerEarlyCommands(this.game);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }

        // used for client
        if (this.game.getPlatform().getType().isClient()) {
            WorldManager.registerVanillaTypesAndDimensions();
            ((SqlServiceImpl) this.game.getServiceManager().provideUnchecked(SqlService.class)).buildConnectionCache();
        }
    }

    @Subscribe
    public void onServerStarted(FMLServerStartedEvent event) {
        // Flush what needs to be saved.
        SpongeImpl.getConfigSaveManager().flush();

        // Call this also here instead of the SpongeBootstrap, this
        // is necessary in the client
        Sponge.getServer().getConsole().getContainingCollection();
        // This is intentionally called multiple times on the client -
        // once for each time a new server is started (when a world is selected from the gui)
        SpongePlayerDataHandler.init();
        try {
            ((IMixinServerCommandManager) SpongeImpl.getServer().getCommandManager()).registerLowPriorityCommands(this.game);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }

    }

    @Subscribe
    public void onServerStopped(FMLServerStoppedEvent event) throws IOException {
        try {
            CommandManager service = this.game.getCommandManager();
            service.getCommands().stream().filter(mapping -> mapping.getCallable() instanceof MinecraftCommandWrapper)
                    .forEach(service::removeMapping);
            ((SqlServiceImpl) this.game.getServiceManager().provideUnchecked(SqlService.class)).close();
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }

        // Save all data that is waiting to be saved
        SpongeImpl.getConfigSaveManager().flush();

        // used by client
        if (this.game.getPlatform().getType().isClient()) {
            WorldManager.unregisterAllWorldSettings();
        }
    }

    // This overrides the method in PluginContainer
    // (PluginContainer is implemented indirectly through the ModContainer mixin)
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public URL getUpdateUrl() {
        return this.updateJsonUrl;
    }
}
