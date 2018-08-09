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
package org.spongepowered.mod.mixin.core.forge;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * This mixin redirects all logic in Forge to our WorldManager.
 */
@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {

    @Shadow @Final @Mutable private static Multiset<Integer> leakedWorlds = HashMultiset.create();

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static int[] getDimensions(DimensionType type) {
        return WorldManager.getRegisteredDimensionIdsFor(type);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static void init() {
        WorldManager.registerVanillaTypesAndDimensions();
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static void registerDimension(int id, DimensionType type) {
        WorldManager.registerDimension(id, type);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static void unregisterDimension(int id) {
        WorldManager.unregisterDimension(id);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static boolean isDimensionRegistered(int dim) {
        return WorldManager.isDimensionRegistered(dim);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static DimensionType getProviderType(int dim) {
        return WorldManager.getDimensionType(dim).orElse(null);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static WorldProvider getProvider(int dim) {
        final Optional<WorldServer> optWorldServer = WorldManager.getWorldByDimensionId(dim);
        if (optWorldServer.isPresent()) {
            return optWorldServer.get().provider;
        }
        SpongeImpl.getLogger().error("Attempt made to get a provider for dimension id [{}] but it has no provider!");
        throw new RuntimeException();
    }

    /**
     * Gets loaded dimension ids
     * @param check Check for leaked worlds
     * @return An array of loaded dimension ids
     * @author Zidane, blood
     * @reason Gets the loaded id's from world manager.
     */
    @Overwrite
    public static Integer[] getIDs(boolean check) {
        if (check) {

            final List<WorldServer> candidateLeakedWorlds = new ArrayList<>(WorldManager.getWeakWorldMap().values());
            candidateLeakedWorlds.removeAll(WorldManager.getWorlds());

            leakedWorlds
                    .addAll(candidateLeakedWorlds.stream().map(System::identityHashCode).collect(Collectors.toList()));

            for (WorldServer worldServer : WorldManager.getWorlds()) {
                final int hashCode = System.identityHashCode(worldServer);
                final int leakCount = leakedWorlds.count(hashCode);


                // Log every 5 loops
                if (leakCount > 0 && leakCount % 5 == 0) {
                    SpongeImpl.getLogger().warn("World [{}] (DIM{}) (HASH: {}) may have leaked. Encountered [{}] times", worldServer.getWorldInfo()
                            .getWorldName(), ((IMixinWorldServer) worldServer).getDimensionId(), hashCode, leakCount);
                }
            }
        }

        return getIDs();
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static Integer[] getIDs() {
        final int[] spongeDimIds = WorldManager.getLoadedWorldDimensionIds();
        Integer[] forgeDimIds = new Integer[spongeDimIds.length];
        for (int i = 0; i < spongeDimIds.length; i++) {
            forgeDimIds[i] = spongeDimIds[i];
        }
        return forgeDimIds;
    }

    /**
     * @author Zidane - Chris Sanders
     * @reason Redirect all logic to the WorldManager.
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static void setWorld(int id, WorldServer world, MinecraftServer server) {
        if (world != null) {
            WorldManager.forceAddWorld(id, world);
            server.worldTickTimes.put(id, new long[100]);
            FMLLog.info("Loading dimension %d (%s) (%s)", id, world.getWorldInfo().getWorldName(), world.getMinecraftServer());
        } else {
            final WorldServer worldServer = WorldManager.getWorldByDimensionId(id).orElse(null);
            if (worldServer != null) {
                WorldManager.unloadWorld(worldServer, false);
                server.worldTickTimes.remove(id);
            }
        }

        WorldManager.reorderWorldsVanillaFirst();
    }

    /**
     * @author Zidane - June 2nd, 2016
     * @reason Forge's initDimension is very different from Sponge's multi-world. We basically rig it into our system so mods work.
     * @param dim The dimension to load
     */
    @Overwrite
    public static void initDimension(int dim) {

        // World is already loaded, bail
        if (WorldManager.getWorldByDimensionId(dim).isPresent()) {
            return;
        }

        if (dim == 0) {
            throw new RuntimeException("Attempt made to initialize overworld!");
        }

        WorldManager.getWorldByDimensionId(0).orElseThrow(() -> new RuntimeException("Attempt made to initialize "
                + "dimension before overworld is loaded!"));

        DimensionType dimensionType = WorldManager.getDimensionType(dim).orElse(null);
        if (dimensionType == null) {
            SpongeImpl.getLogger().warn("Attempt made to initialize dimension id {} which isn't registered!"
                    + ", falling back to overworld.", dim);
            return;
        }

        final WorldProvider provider = dimensionType.createDimension();
        // make sure to set the dimension id to avoid getting a null save folder
        provider.setDimension(dim);
        final String worldFolder = WorldManager.getWorldFolderByDimensionId(dim).orElse(provider.getSaveFolder());
        WorldProperties properties = WorldManager.getWorldProperties(worldFolder).orElse(null);
        final Path worldPath = WorldManager.getCurrentSavesDirectory().get().resolve(worldFolder);
        if (properties == null || !Files.isDirectory(worldPath)) {
            if (properties != null) {
                WorldManager.unregisterWorldProperties(properties, false);
            }
            String modId = StaticMixinForgeHelper.getModIdFromClass(provider.getClass());
            final CatalogKey key = CatalogKey.of(modId, dimensionType.getName().toLowerCase());
            WorldArchetype archetype = Sponge.getRegistry().getType(WorldArchetype.class, key).orElse(null);
            if (archetype == null) {
                final WorldArchetype.Builder builder = WorldArchetype.builder()
                        .dimension((org.spongepowered.api.world.DimensionType) (Object) dimensionType)
                        .keepsSpawnLoaded(dimensionType.shouldLoadSpawn());
                archetype = builder.build(modId + ":" + dimensionType.getName().toLowerCase(), dimensionType.getName());
            }
            IMixinWorldSettings worldSettings = (IMixinWorldSettings) archetype;
            worldSettings.setDimensionType((org.spongepowered.api.world.DimensionType) (Object) dimensionType);
            worldSettings.setLoadOnStartup(false);
            properties = WorldManager.createWorldProperties(worldFolder, archetype, dim);
            ((IMixinWorldInfo) properties).setDimensionId(dim);
            ((IMixinWorldInfo) properties).setIsMod(true);
        }
        if (!properties.isEnabled()) {
            return;
        }

        Optional<WorldServer> optWorld = WorldManager.loadWorld(properties);
        if (!optWorld.isPresent()) {
            SpongeImpl.getLogger().error("Could not load world [{}]!", properties.getWorldName());
        }
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static WorldServer getWorld(int id) {
        return WorldManager.getWorldByDimensionId(id).orElse(null);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static WorldServer[] getWorlds() {
        final Collection<WorldServer> worlds = WorldManager.getWorlds();
        return worlds.toArray(new WorldServer[0]);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static Integer[] getStaticDimensionIDs() {
        final int[] spongeDimIds = WorldManager.getRegisteredDimensionIds();
        Integer[] forgeDimIds = new Integer[spongeDimIds.length];
        for (int i = 0; i < spongeDimIds.length; i++) {
            forgeDimIds[i] = spongeDimIds[i];
        }
        return forgeDimIds;
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static WorldProvider createProviderFor(int dim) {
        final DimensionType dimensionType = WorldManager.getDimensionType(dim).orElseThrow(() -> new RuntimeException("Attempt to create "
                + "provider for dimension id [" + dim + "] failed because it has not been registered!"));
        try {
            final WorldProvider provider = dimensionType.createDimension();
            provider.setDimension(dim);
            return provider;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create provider for dimension id [" + dim + "]!");
        }
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static void unloadWorld(int id) {
        WorldManager.getWorldByDimensionId(id).ifPresent(WorldManager::queueWorldToUnload);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static void unloadWorlds(Hashtable<Integer, long[]> worldTickTimes) {
        WorldManager.unloadQueuedWorlds();
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static int getNextFreeDimId() {
        return WorldManager.getNextFreeDimensionId();
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static NBTTagCompound saveDimensionDataMap() {
        return WorldManager.saveDimensionDataMap();
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    public static void loadDimensionDataMap(NBTTagCompound compoundTag) {
        WorldManager.loadDimensionDataMap(compoundTag);
    }

    /**
     * @author Zidane, blood
     * @reason Reroute Forge's dimension manager to Sponge's, since we do dimension management in common.
     */
    @Overwrite
    @Nullable
    public static File getCurrentSaveRootDirectory() {
        final Optional<Path> optCurrentSavesDir = WorldManager.getCurrentSavesDirectory();
        return optCurrentSavesDir.isPresent() ? optCurrentSavesDir.get().toFile() : null;
    }
}
