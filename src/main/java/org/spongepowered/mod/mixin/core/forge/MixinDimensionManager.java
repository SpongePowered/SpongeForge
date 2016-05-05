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

import java.io.File;
import java.util.Hashtable;

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
    }

    @Overwrite
    public static DimensionType getProviderType(int dim) {
    }

    @Overwrite
    public static WorldProvider getProvider(int dim) {
    }

    @Overwrite
    public static Integer[] getIDs(boolean check) {
    }

    @Overwrite
    public static Integer[] getIDs() {
    }

    @Overwrite
    public static void setWorld(int id, WorldServer world, MinecraftServer server) {
    }

    @Overwrite
    public static void initDimension(int dim) {
    }

    @Overwrite
    public static WorldServer getWorld(int id) {
    }

    @Overwrite
    public static WorldServer[] getWorlds() {
    }

    @Overwrite
    public static Integer[] getStaticDimensionIDs() {
    }

    @Overwrite
    public static WorldProvider createProviderFor(int dim) {
    }

    @Overwrite
    public static void unloadWorld(int id) {
    }

    @Overwrite
    public static void unloadWorlds(Hashtable<Integer, long[]> worldTickTimes) {
    }

    @Overwrite
    public static int getNextFreeDimId() {
    }

    @Overwrite
    public static NBTTagCompound saveDimensionDataMap() {
    }

    @Overwrite
    public static void loadDimensionDataMap(NBTTagCompound compoundTag) {
    }

    @Overwrite
    public static File getCurrentSaveRootDirectory() {
        return SpongeImpl.getGame().getSavesDirectory().toFile();
    }
}
