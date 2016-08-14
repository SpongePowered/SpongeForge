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
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.mod.SpongeMod;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This mixin redirects all logic in Forge to our WorldManager.
 */
@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {

    @Shadow static Multiset<Integer> leakedWorlds = HashMultiset.create();

    @Overwrite
    public static int[] getDimensions(DimensionType type) {
        return (int[]) (Object) WorldManager.getRegisteredDimensionIdsFor(type);
    }

    @Overwrite
    public static void init() {
        WorldManager.registerVanillaTypesAndDimensions();
    }

    @Overwrite
    public static void registerDimension(int id, DimensionType type) {
        WorldManager.registerDimension(id, type, false);
    }

    @Overwrite
    public static void unregisterDimension(int id) {
        WorldManager.unregisterDimension(id);
    }

    @Overwrite
    public static boolean isDimensionRegistered(int dim) {
        return WorldManager.isDimensionRegistered(dim);
    }

    @Overwrite
    public static DimensionType getProviderType(int dim) {
        return WorldManager.getDimensionType(dim).orElse(null);
    }

    @Overwrite
    public static WorldProvider getProvider(int dim) {
        final Optional<WorldServer> optWorldServer = WorldManager.getWorldByDimensionId(dim);
        if (optWorldServer.isPresent()) {
            return optWorldServer.get().provider;
        } else {
            SpongeImpl.getLogger().error("Attempt made to get a provider for dimension id [{}] but it has no provider!");
            throw new RuntimeException();
        }
    }

    /**
     * Gets loaded dimension ids
     * @param check Check for leaked worlds
     * @return An array of loaded dimension ids
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

    @Overwrite
    public static Integer[] getIDs() {
        final int[] spongeDimIds = WorldManager.getLoadedWorldDimensionIds();
        Integer[] forgeDimIds = new Integer[spongeDimIds.length];
        for (int i = 0; i < spongeDimIds.length; i++) {
            forgeDimIds[i] = spongeDimIds[i];
        }
        return forgeDimIds;
    }

    @Overwrite
    public static void setWorld(int id, WorldServer world, MinecraftServer server) {
        if (world != null) {
            WorldManager.forceAddWorld(id, world);
            FMLLog.info("Loading dimension %d (%s) (%s)", id, world.getWorldInfo().getWorldName(), world.getMinecraftServer());
        } else {
            WorldManager.unloadWorld(WorldManager.getWorldByDimensionId(id).orElseThrow(() -> new RuntimeException("Attempt made to unload a "
                    + "world with dimension id [" + id + "].")), false);
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

        final DimensionType dimensionType = WorldManager.getDimensionType(dim).orElseThrow(() -> new RuntimeException("Attempt made to initialize "
                + "dimension who isn't registered!"));

        final WorldProvider provider = dimensionType.createDimension();
        // make sure to set the dimension id to avoid getting a null save folder
        provider.setDimension(dim);
        String worldFolder = provider.getSaveFolder();
        WorldProperties properties = WorldManager.getWorldProperties(worldFolder).orElse(null);
        if (properties == null) {
            final WorldArchetype.Builder builder = WorldArchetype.builder()
                    .dimension((org.spongepowered.api.world.DimensionType)(Object) dimensionType)
                    .keepsSpawnLoaded(dimensionType.shouldLoadSpawn());
            String modId = SpongeMod.instance.getModIdFromClass(provider.getClass());
            final WorldArchetype archetype = builder.build(modId + ":" + dimensionType.getName().toLowerCase(), dimensionType.getName());
            properties = WorldManager.createWorldProperties(worldFolder, archetype);
            ((IMixinWorldInfo) properties).setDimensionId(dim);
        }
        Optional<WorldServer> optWorld = WorldManager.loadWorld(properties);
        if (!optWorld.isPresent()) {
            SpongeImpl.getLogger().error("Could not load world [{}]!", properties.getWorldName());
        }
    }

    @Overwrite
    public static WorldServer getWorld(int id) {
        return WorldManager.getWorldByDimensionId(id).orElse(null);
    }

    @Overwrite
    public static WorldServer[] getWorlds() {
        final Collection<WorldServer> worlds = WorldManager.getWorlds();
        return worlds.toArray(new WorldServer[worlds.size()]);
    }

    @Overwrite
    public static Integer[] getStaticDimensionIDs() {
        final int[] spongeDimIds = WorldManager.getRegisteredDimensionIds();
        Integer[] forgeDimIds = new Integer[spongeDimIds.length];
        for (int i = 0; i < spongeDimIds.length; i++) {
            forgeDimIds[i] = spongeDimIds[i];
        }
        return forgeDimIds;
    }

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

    @Overwrite
    public static void unloadWorld(int id) {
        WorldManager.getWorldByDimensionId(id).ifPresent(WorldManager::queueWorldToUnload);
    }

    @Overwrite
    public static void unloadWorlds(Hashtable<Integer, long[]> worldTickTimes) {
        WorldManager.unloadQueuedWorlds();
    }

    @Overwrite
    public static int getNextFreeDimId() {
        return WorldManager.getNextFreeDimensionId();
    }

    @Overwrite
    public static NBTTagCompound saveDimensionDataMap() {
        return WorldManager.saveDimensionDataMap();
    }

    @Overwrite
    public static void loadDimensionDataMap(NBTTagCompound compoundTag) {
        WorldManager.loadDimensionDataMap(compoundTag);
    }

    @Overwrite
    public static File getCurrentSaveRootDirectory() {
        final Optional<Path> optCurrentSavesDir = WorldManager.getCurrentSavesDirectory();
        return optCurrentSavesDir.isPresent() ? optCurrentSavesDir.get().toFile() : null;
    }
}
