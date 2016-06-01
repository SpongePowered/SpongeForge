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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Optional;

/**
 * TODO This is Zidane's Mixin...DO NOT TOUCH IT, I'M FINISHING IT.
 */
@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {

    @Overwrite
    public static int[] getDimensions(DimensionType type) {
        return null;
    }

    @Overwrite
    public static void init() {
    }

    @Overwrite
    public static void registerDimension(int id, DimensionType type) {
    }

    @Overwrite
    public static void unregisterDimension(int id) {
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

    @Overwrite
    public static Integer[] getIDs(boolean check) {
        return getIDs();
    }

    @Overwrite
    public static Integer[] getIDs() {
        final int[] spongeDimIds = WorldManager.worldByDimensionId.keySet().toIntArray();
        Integer[] forgeDimIds = new Integer[spongeDimIds.length];
        for (int i = 0; i < spongeDimIds.length; i++) {
            forgeDimIds[i] = spongeDimIds[i];
        }
        return forgeDimIds;
    }

    @Overwrite
    public static void setWorld(int id, WorldServer world, MinecraftServer server) {
    }

    @Overwrite
    public static void initDimension(int dim) {
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
        // TODO - Zidane  - Confirm?
        final int[] spongeDimIds = WorldManager.dimensionTypeByDimensionId.keySet().toIntArray();
        Integer[] forgeDimIds = new Integer[spongeDimIds.length];
        for (int i = 0; i < spongeDimIds.length; i++) {
            forgeDimIds[i] = spongeDimIds[i];
        }
        return forgeDimIds;
    }

    @Overwrite
    public static WorldProvider createProviderFor(int dim) {
        final Optional<DimensionType> dimensionType = WorldManager.getDimensionType(dim);
        if (dimensionType.isPresent()) {
            try {
                final WorldProvider provider = dimensionType.get().createDimension();
                provider.setDimension(dim);
                return provider;
            } catch (Exception e) {
                SpongeImpl.getLogger().error("Failed to create provider for dimension id [{}]!", dim);
                throw new RuntimeException(e);
            }
        } else {
            SpongeImpl.getLogger().error("Attempt to create provider for dimension id [{}] failed because it has not been registered!");
            throw new RuntimeException();
        }
    }

    @Overwrite
    public static void unloadWorld(int id) {
        WorldManager.queueWorldToUnload(WorldManager.worldByDimensionId.get(id));
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
        return new NBTTagCompound();
    }

    @Overwrite
    public static void loadDimensionDataMap(NBTTagCompound compoundTag) {
        WorldManager.loadDimensionDataMap(compoundTag);
    }

    @Overwrite
    public static File getCurrentSaveRootDirectory() {
        return WorldManager.getCurrentSavesDirectory().get().toFile();
    }
}
