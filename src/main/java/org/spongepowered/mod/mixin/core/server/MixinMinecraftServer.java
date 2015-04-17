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
package org.spongepowered.mod.mixin.core.server;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.source.ConsoleSource;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinWorldInfo;
import org.spongepowered.mod.interfaces.Subjectable;
import org.spongepowered.mod.text.SpongeText;
import org.spongepowered.mod.world.SpongeDimensionType;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@NonnullByDefault
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, ConsoleSource, Subjectable {

    @Shadow private static Logger logger;
    @Shadow public WorldServer[] worldServers;
    @Shadow private ServerConfigurationManager serverConfigManager;
    @Shadow public Profiler theProfiler;
    @Shadow private boolean enableBonusChest;
    @Shadow private boolean worldIsBeingDeleted;
    @Shadow private int tickCounter;
    @Shadow protected abstract void convertMapIfNeeded(String worldNameIn);
    @Shadow protected abstract void setUserMessage(String message);
    @Shadow protected abstract void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn);
    @Shadow public abstract boolean canStructuresSpawn();
    @Shadow public abstract WorldSettings.GameType getGameType();
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow public abstract boolean isHardcore();
    @Shadow public abstract boolean isSinglePlayer();
    @Shadow public abstract boolean isDemo();
    @Shadow public abstract String getFolderName();
    @Shadow public abstract void setDifficultyForAllWorlds(EnumDifficulty difficulty);
    @Shadow public abstract ServerConfigurationManager getConfigurationManager();
    @Shadow @SideOnly(Side.SERVER) public abstract String getServerHostname();
    @Shadow @SideOnly(Side.SERVER) public abstract int getPort();
    @Shadow public abstract void addChatMessage(IChatComponent message);
    @Shadow public abstract boolean isServerInOnlineMode();
    @Shadow public abstract void initiateShutdown();
    @Shadow public abstract boolean isServerRunning();
    @Shadow protected abstract void outputPercentRemaining(String message, int percent);
    @Shadow protected abstract void clearCurrentTask();

    @Overwrite
    protected void loadAllWorlds(String overworldFolder, String unused, long seed, WorldType type, String generator) {
        this.convertMapIfNeeded(overworldFolder);
        this.setUserMessage("menu.loadingLevel");

        List<Integer> idList = new LinkedList<Integer>(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
        idList.remove(Integer.valueOf(0));
        idList.add(0, 0); // load overworld first
        for (int dim : idList) {
            WorldProvider provider = WorldProvider.getProviderForDimension(dim);
            String worldFolder = "";
            if (dim == 0) {
                worldFolder = overworldFolder;
            } else {
                worldFolder = SpongeMod.instance.getSpongeRegistry().getWorldFolder(dim);
                if (worldFolder != null) {
                    final Optional<World> optExisting = getWorld(worldFolder);
                    if (optExisting.isPresent()) {
                        continue; // world is already loaded
                    }
                } else {
                    worldFolder = provider.getSaveFolder();
                }
            }

            WorldInfo worldInfo = null;
            WorldSettings newWorldSettings = null;
            AnvilSaveHandler worldsavehandler = null;

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                worldsavehandler =
                        new AnvilSaveHandler(dim == 0 ? FMLCommonHandler.instance().getSavesDirectory() : new File(FMLCommonHandler.instance().getSavesDirectory() + File.separator
                                + getFolderName()),
                                worldFolder, true);
            } else {
                worldsavehandler = new AnvilSaveHandler(new File(dim == 0 ? "." : getFolderName()), worldFolder, true);
            }
            worldInfo = worldsavehandler.loadWorldInfo();
            if (worldInfo == null) {
                newWorldSettings = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
                newWorldSettings.setWorldName(generator);

                if (this.enableBonusChest) {
                    newWorldSettings.enableBonusChest();
                }

                worldInfo = new WorldInfo(newWorldSettings, worldFolder);
                ((IMixinWorldInfo) worldInfo).setUUID(UUID.randomUUID());
                if (dim == 0 || dim == -1 || dim == 1) {// if vanilla dimension
                    ((WorldProperties) worldInfo).setKeepSpawnLoaded(true);
                    ((WorldProperties) worldInfo).setLoadOnStartup(true);
                    ((WorldProperties) worldInfo).setEnabled(true);
                    ((WorldProperties) worldInfo).setGeneratorType(GeneratorTypes.DEFAULT);
                    SpongeMod.instance.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
                }
            } else {
                worldInfo.setWorldName(worldFolder);
                newWorldSettings = new WorldSettings(worldInfo);
            }

            if (dim == 0) {
                this.setResourcePackFromWorld(this.getFolderName(), worldsavehandler);
            }

            ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
            ((IMixinWorldInfo) worldInfo).setDimensionType(((Dimension) provider).getType());
            UUID uuid = ((WorldProperties) worldInfo).getUniqueId();
            SpongeMod.instance.getSpongeRegistry().registerWorldUniqueId(uuid, worldFolder);

            WorldServer world = (WorldServer) new WorldServer((MinecraftServer) (Object) this, worldsavehandler, worldInfo, dim,
                    this.theProfiler).init();

            world.initialize(newWorldSettings);
            world.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, world));

            if (!this.isSinglePlayer()) {
                world.getWorldInfo().setGameType(this.getGameType());
            }
            SpongeMod.instance.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Load(world));
        }

        this.serverConfigManager.setPlayerManager(new WorldServer[] {DimensionManager.getWorld(0)});
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialWorldChunkLoad();
    }

    @Overwrite
    protected void initialWorldChunkLoad() {
        for (WorldServer worldserver : DimensionManager.getWorlds()) {
            WorldProperties worldProperties = ((World) worldserver).getProperties();
            if (worldProperties.doesKeepSpawnLoaded()) {
                prepareSpawnArea(worldserver);
            }
        }

        this.clearCurrentTask();
    }

    protected void prepareSpawnArea(WorldServer world) {
        int i = 0;
        this.setUserMessage("menu.generatingTerrain");
        logger.info("Preparing start region for level " + world.provider.getDimensionId());
        BlockPos blockpos = world.getSpawnPoint();
        long j = MinecraftServer.getCurrentTimeMillis();

        for (int k = -192; k <= 192 && this.isServerRunning(); k += 16) {
            for (int l = -192; l <= 192 && this.isServerRunning(); l += 16) {
                long i1 = MinecraftServer.getCurrentTimeMillis();

                if (i1 - j > 1000L) {
                    this.outputPercentRemaining("Preparing spawn area", i * 100 / 625);
                    j = i1;
                }

                ++i;
                world.theChunkProviderServer.loadChunk(blockpos.getX() + k >> 4, blockpos.getZ() + l >> 4);
            }
        }

        this.clearCurrentTask();
    }

    @Override
    public Optional<World> loadWorld(UUID uuid) {
        String worldFolder = SpongeMod.instance.getSpongeRegistry().getWorldFolder(uuid);
        if (worldFolder != null) {
            return loadWorld(worldFolder);
        }
        return Optional.absent();
    }

    @Override
    public Optional<World> loadWorld(String worldName) {
        final Optional<World> optExisting = getWorld(worldName);
        if (optExisting.isPresent()) {
            return optExisting;
        }

        File file = new File(getFolderName(), worldName);

        if ((file.exists()) && (!file.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + worldName + "' and isn't a folder");
        }

        AnvilSaveHandler savehandler = null;
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            savehandler =
                    new AnvilSaveHandler(new File(FMLCommonHandler.instance().getSavesDirectory() + File.separator + getFolderName()), worldName,
                            true);
        } else {
            savehandler = new AnvilSaveHandler(new File(getFolderName()), worldName, true);
        }
        int dim;
        WorldInfo worldInfo = savehandler.loadWorldInfo();
        if (worldInfo != null) {
            // check if enabled
            if (!((WorldProperties) worldInfo).isEnabled()) {
                SpongeMod.instance.getLogger().error("Unable to load world " + worldName + ". World is disabled!");
                return Optional.absent();
            }
            if (!SpongeMod.instance.getSpongeRegistry().getWorldProperties(((WorldProperties) worldInfo).getUniqueId()).isPresent()) {
                SpongeMod.instance.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
            } else {
                worldInfo = (WorldInfo) SpongeMod.instance.getSpongeRegistry().getWorldProperties(((WorldProperties) worldInfo).getUniqueId()).get();
            }
            dim = ((IMixinWorldInfo) worldInfo).getDimensionId();
            if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
                DimensionManager
                        .registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
            }
            if (SpongeMod.instance.getSpongeRegistry().getWorldFolder(dim) == null) {
                SpongeMod.instance.getSpongeRegistry().registerWorldDimensionId(dim, worldName);
            }
        } else {
            return Optional.absent(); // no world data found
        }

        WorldSettings settings = new WorldSettings(worldInfo);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }

        WorldServer world = (WorldServer) new WorldServer((MinecraftServer) (Object) this, savehandler, worldInfo, dim, this.theProfiler).init();

        world.initialize(settings);
        world.provider.setDimension(dim);

        world.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
        if (!isSinglePlayer())
        {
            world.getWorldInfo().setGameType(getGameType());
        }
        this.setDifficultyForAllWorlds(this.getDifficulty());
        if (((WorldProperties) worldInfo).doesKeepSpawnLoaded()) {
            this.prepareSpawnArea(world);
        }

        return Optional.of((World) world);
    }

    @Override
    public Optional<WorldProperties> createWorld(WorldCreationSettings settings) {
        String worldName = settings.getWorldName();
        final Optional<World> optExisting = getWorld(worldName);
        if (optExisting.isPresent()) {
            return Optional.of(optExisting.get().getProperties());
        }

        int dim;
        AnvilSaveHandler savehandler = null;
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            savehandler =
                    new AnvilSaveHandler(new File(FMLCommonHandler.instance().getSavesDirectory() + File.separator + getFolderName()), worldName,
                            true);
        } else {
            savehandler = new AnvilSaveHandler(new File(getFolderName()), worldName, true);
        }
        WorldInfo worldInfo = savehandler.loadWorldInfo();

        if (worldInfo != null)
        {
            if (!SpongeMod.instance.getSpongeRegistry().getWorldProperties(((WorldProperties) worldInfo).getUniqueId()).isPresent()) {
                SpongeMod.instance.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
                return Optional.of((WorldProperties) worldInfo);
            } else {
                return SpongeMod.instance.getSpongeRegistry().getWorldProperties(((WorldProperties) worldInfo).getUniqueId());
            }
        } else {
            dim = DimensionManager.getNextFreeDimId();
            worldInfo = new WorldInfo((WorldSettings) (Object) settings, settings.getWorldName());
            ((WorldProperties) worldInfo).setKeepSpawnLoaded(settings.doesKeepSpawnLoaded());
            ((WorldProperties) worldInfo).setLoadOnStartup(settings.loadOnStartup());
            ((WorldProperties) worldInfo).setEnabled(settings.isEnabled());
            ((WorldProperties) worldInfo).setGeneratorType(settings.getGeneratorType());
            SpongeMod.instance.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
            SpongeMod.instance.getSpongeRegistry().registerWorldDimensionId(dim, worldName);
        }

        ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
        ((IMixinWorldInfo) worldInfo).setDimensionType(settings.getDimensionType());
        UUID uuid = UUID.randomUUID();
        ((IMixinWorldInfo) worldInfo).setUUID(uuid);
        SpongeMod.instance.getSpongeRegistry().registerWorldUniqueId(uuid, worldName);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }
        savehandler.saveWorldInfoWithPlayer(worldInfo, getConfigurationManager().getHostPlayerData());

        SpongeMod.instance.getEventManager().post(SpongeEventFactory.createWorldCreate(SpongeMod.instance.getGame(), (WorldProperties)
                worldInfo, settings));
        return Optional.of((WorldProperties) worldInfo);
    }

    @Override
    public boolean unloadWorld(World world) {
        int dim = ((net.minecraft.world.World) world).provider.getDimensionId();
        if (DimensionManager.getWorld(dim) != null) {
            DimensionManager.unloadWorld(((net.minecraft.world.World) world).provider.getDimensionId());
            return true;
        }
        return false;
    }

    @Override
    public Collection<World> getWorlds() {
        List<World> worlds = new ArrayList<World>();
        for (WorldServer worldServer : DimensionManager.getWorlds()) {
            worlds.add((World) worldServer);
        }
        return worlds;
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        for (WorldServer worldserver : DimensionManager.getWorlds()) {
            if (((World) worldserver).getUniqueId().equals(uniqueId)) {
                return Optional.of((World) worldserver);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<World> getWorld(String worldName) {
        for (World world : getWorlds()) {
            if (world.getName().equals(worldName)) {
                return Optional.of(world);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return SpongeMod.instance.getSpongeRegistry().getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return SpongeMod.instance.getSpongeRegistry().getAllWorldProperties();
    }

    @Override
    public void broadcastMessage(Text message) {
        getConfigurationManager().sendChatMsg(((SpongeText) message).toComponent());
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.fromNullable(new InetSocketAddress(getServerHostname(), getPort()));
    }

    @Override
    public boolean hasWhitelist() {
        return this.serverConfigManager.isWhiteListEnabled();
    }

    @Override
    public void setHasWhitelist(boolean enabled) {
        this.serverConfigManager.setWhiteListEnabled(enabled);
    }

    @Override
    public boolean getOnlineMode() {
        return isServerInOnlineMode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Player> getOnlinePlayers() {
        return ImmutableList.copyOf((List<Player>) getConfigurationManager().playerEntityList);
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUsername(name));
    }

    @Override
    public Text getMotd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxPlayers() {
        return getConfigurationManager().getMaxPlayers();
    }

    @Override
    public int getRunningTimeTicks() {
        return this.tickCounter;
    }

    @Override
    public void sendMessage(Text... messages) {
        for (Text message : messages) {
            addChatMessage(((SpongeText) message).toComponent());
        }
    }

    @Override
    public void sendMessage(Iterable<Text> messages) {
        for (Text message : messages) {
            addChatMessage(((SpongeText) message).toComponent());
        }
    }

    @Override
    public String getIdentifier() {
        return getName();
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.TRUE;
    }

    @Override
    public String getName() {
        return "Server";
    }

    @Override
    public ConsoleSource getConsole() {
        return this;
    }

    @Override
    public void shutdown(Text kickMessage) {
        /*
         * for (Player player : getOnlinePlayers()) { ((EntityPlayerMP)
         * player).playerNetServerHandler
         * .kickPlayerFromServer(kickMessage.toLegacy()); //TODO update with the
         * new Text API }
         */

        initiateShutdown();
    }
}
