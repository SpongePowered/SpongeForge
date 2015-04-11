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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinWorldInfo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@NonnullByDefault
@Mixin(net.minecraft.world.storage.SaveHandler.class)
public abstract class MixinSaveHandler {

    @Shadow
    private File worldDirectory;

    @Shadow
    private long initializationTime;

    @Overwrite
    public void checkSessionLock() throws MinecraftException {
        try {
            File file1 = new File(this.worldDirectory, "session.lock");
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));

            try {
                if (datainputstream.readLong() != this.initializationTime) {
                    throw new MinecraftException("The save folder for world " + this.worldDirectory
                            + " is being accessed from another location, aborting");
                }
            } finally {
                datainputstream.close();
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            throw new MinecraftException("Failed to check session lock for world " + this.worldDirectory + ", aborting");
        }
    }

    @Overwrite
    public WorldInfo loadWorldInfo() {
        File file1 = new File(this.worldDirectory, "level.dat");
        File file2 = new File(this.worldDirectory, "level.dat_old");
        File spongeFile = new File(this.worldDirectory, "level_sponge.dat");
        File spongeOldFile = new File(this.worldDirectory, "level_sponge.dat_old");
        NBTTagCompound nbttagcompound;
        NBTTagCompound nbttagcompound1;

        WorldInfo worldInfo = null;

        if (!file1.exists() && file2.exists()) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().confirmBackupLevelDatUse((SaveHandler) (Object) this);
        }

        if (file1.exists() || file2.exists()) {
            try {
                nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1.exists() ? file1 : file2));
                nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                worldInfo = new WorldInfo(nbttagcompound1);
                // Forge and FML data are only loaded from main world
                if (this.worldDirectory.getParentFile() == null
                        || (FMLCommonHandler.instance().getSide() == Side.CLIENT && this.worldDirectory.getParentFile().equals(
                                FMLCommonHandler.instance().getSavesDirectory()))) {
                    net.minecraftforge.fml.common.FMLCommonHandler.instance().handleWorldDataLoad((SaveHandler) (Object) this, worldInfo,
                            nbttagcompound);
                }

                // check for sponge data
                if (spongeFile.exists() || spongeOldFile.exists()) {
                    nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(spongeFile.exists() ? spongeFile : spongeOldFile));
                    ((IMixinWorldInfo) worldInfo).setSpongeRootLevelNBT(nbttagcompound);
                    if (nbttagcompound.hasKey(SpongeMod.instance.getModId())) {
                        NBTTagCompound spongeNbt = nbttagcompound.getCompoundTag(SpongeMod.instance.getModId());
                        ((IMixinWorldInfo) worldInfo).readSpongeNbt(spongeNbt);
                    }
                }

                return worldInfo;
            } catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e) {
                throw e;
            } catch (Exception exception1) {
                exception1.printStackTrace();
            }
        }

        return null;
    }

    @Overwrite
    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
        NBTTagCompound nbttagcompound1 = worldInformation.cloneNBTCompound(tagCompound);
        NBTTagCompound nbttagcompound2 = new NBTTagCompound();
        nbttagcompound2.setTag("Data", nbttagcompound1);

        // Forge and FML data are only saved to main world
        if (this.worldDirectory.getParentFile() == null
                || (FMLCommonHandler.instance().getSide() == Side.CLIENT && this.worldDirectory.getParentFile().equals(
                        FMLCommonHandler.instance().getSavesDirectory()))) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().handleWorldDataSave((SaveHandler) (Object) this, worldInformation,
                    nbttagcompound2);
        }

        try {
            File file1 = new File(this.worldDirectory, "level.dat_new");
            File file2 = new File(this.worldDirectory, "level.dat_old");
            File file3 = new File(this.worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound2, new FileOutputStream(file1));

            if (file2.exists()) {
                file2.delete();
            }

            file3.renameTo(file2);

            if (file3.exists()) {
                file3.delete();
            }

            file1.renameTo(file3);

            if (file1.exists()) {
                file1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            File spongeFile1 = new File(this.worldDirectory, "level_sponge.dat_new");
            File spongeFile2 = new File(this.worldDirectory, "level_sponge.dat_old");
            File spongeFile3 = new File(this.worldDirectory, "level_sponge.dat");
            CompressedStreamTools.writeCompressed(((IMixinWorldInfo) worldInformation).getSpongeRootLevelNbt(), new FileOutputStream(spongeFile1));

            if (spongeFile2.exists()) {
                spongeFile2.delete();
            }

            spongeFile3.renameTo(spongeFile2);

            if (spongeFile3.exists()) {
                spongeFile3.delete();
            }

            spongeFile1.renameTo(spongeFile3);

            if (spongeFile1.exists()) {
                spongeFile1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Overwrite
    public void saveWorldInfo(WorldInfo worldInformation) {
        NBTTagCompound nbttagcompound = worldInformation.getNBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setTag("Data", nbttagcompound);

        // Forge and FML data are only saved to main world
        if (this.worldDirectory.getParentFile() == null
                || (FMLCommonHandler.instance().getSide() == Side.CLIENT && this.worldDirectory.getParentFile().equals(
                        FMLCommonHandler.instance().getSavesDirectory()))) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().handleWorldDataSave((SaveHandler) (Object) this, worldInformation,
                    nbttagcompound1);
        }

        try {
            File file1 = new File(this.worldDirectory, "level.dat_new");
            File file2 = new File(this.worldDirectory, "level.dat_old");
            File file3 = new File(this.worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file1));

            if (file2.exists()) {
                file2.delete();
            }

            file3.renameTo(file2);

            if (file3.exists()) {
                file3.delete();
            }

            file1.renameTo(file3);

            if (file1.exists()) {
                file1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            File spongeFile1 = new File(this.worldDirectory, "level_sponge.dat_new");
            File spongeFile2 = new File(this.worldDirectory, "level_sponge.dat_old");
            File spongeFile3 = new File(this.worldDirectory, "level_sponge.dat");
            CompressedStreamTools.writeCompressed(((IMixinWorldInfo) worldInformation).getSpongeRootLevelNbt(), new FileOutputStream(spongeFile1));

            if (spongeFile2.exists()) {
                spongeFile2.delete();
            }

            spongeFile3.renameTo(spongeFile2);

            if (spongeFile3.exists()) {
                spongeFile3.delete();
            }

            spongeFile1.renameTo(spongeFile3);

            if (spongeFile1.exists()) {
                spongeFile1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
