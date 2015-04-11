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
package org.spongepowered.mod.util;

import com.flowpowered.math.vector.Vector3i;
import com.google.gson.stream.JsonWriter;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.interfaces.IMixinWorldProvider;
import org.spongepowered.mod.mixin.plugin.CoreMixinPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;

public class SpongeHooks {

    public static int tickingDimension = 0;
    public static ChunkCoordIntPair tickingChunk = null;

    private static TObjectLongHashMap<CollisionWarning> recentWarnings = new TObjectLongHashMap<CollisionWarning>();

    public static void logInfo(String msg, Object... args) {
        MinecraftServer.getServer().logInfo(MessageFormat.format(msg, args));
    }

    public static void logWarning(String msg, Object... args) {
        MinecraftServer.getServer().logWarning(MessageFormat.format(msg, args));
    }

    public static void logSevere(String msg, Object... args) {
        MinecraftServer.getServer().logSevere(MessageFormat.format(msg, args));
    }

    public static void logStack(SpongeConfig<?> config) {
        if (config.getConfig().getLogging().logWithStackTraces()) {
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
    }

    public static void logEntityDeath(Entity entity) {
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entityDeathLogging()) {
            logInfo("[" + config.getConfigName() + "] [" + config.getConfigName() + "] Dim: {0} setDead(): {1}",
                    entity.worldObj.provider.getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logEntityDespawn(Entity entity, String reason) {
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entityDespawnLogging()) {
            logInfo("[" + config.getConfigName() + "] Dim: {0} Despawning ({1}): {2}", entity.worldObj.provider.getDimensionId(), reason, entity);
            logStack(config);
        }
    }

    public static void logEntitySpawn(Entity entity) {
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entitySpawnLogging()) {
            logInfo("[" + config.getConfigName() + "] Dim: {0} Spawning: {1}", entity.worldObj.provider.getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logChunkLoad(World world, Vector3i chunkPos) {
        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkLoadLogging()) {
            logInfo("[" + config.getConfigName() + "] Load Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logChunkUnload(World world, Vector3i chunkPos) {
        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkUnloadLogging()) {
            logInfo("[" + config.getConfigName() + "] Unload Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    @SuppressWarnings("unused")
    private static void logChunkLoadOverride(ChunkProviderServer provider, int x, int z) {
        SpongeConfig<?> config = getActiveConfig(provider.worldObj);
        logInfo("[" + config.getConfigName() + "]  Chunk Load Override: {0}, Dimension ID: {1}", provider.chunkLoadOverride,
                provider.worldObj.provider.getDimensionId());
    }

    public static boolean checkBoundingBoxSize(Entity entity, AxisAlignedBB aabb) {
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer) {
            return false; // only check living entities that are not players
        }

        int maxBoundingBoxSize = config.getConfig().getEntity().getMaxBoundingBoxSize();
        if (maxBoundingBoxSize <= 0) {
            return false;
        }
        int x = MathHelper.floor_double(aabb.minX);
        int x1 = MathHelper.floor_double(aabb.maxX + 1.0D);
        int y = MathHelper.floor_double(aabb.minY);
        int y1 = MathHelper.floor_double(aabb.maxY + 1.0D);
        int z = MathHelper.floor_double(aabb.minZ);
        int z1 = MathHelper.floor_double(aabb.maxZ + 1.0D);

        int size = Math.abs(x1 - x) * Math.abs(y1 - y) * Math.abs(z1 - z);
        if (size > maxBoundingBoxSize) {
            logWarning("Entity being removed for bounding box restrictions");
            logWarning("BB Size: {0} > {1} avg edge: {2}", size, maxBoundingBoxSize, aabb.getAverageEdgeLength());
            logWarning("Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
            logWarning("Calculated bounding box: {0}", aabb);
            logWarning("Entity bounding box: {0}", entity.getBoundingBox());
            logWarning("Entity: {0}", entity);
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            logWarning("Entity NBT: {0}", tag);
            logStack(config);
            entity.setDead();
            return true;
        }
        return false;
    }

    public static boolean checkEntitySpeed(Entity entity, double x, double y, double z) {
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        int maxSpeed = config.getConfig().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            double distance = x * x + z * z;
            if (distance > maxSpeed) {
                if (config.getConfig().getLogging().logEntitySpeedRemoval()) {
                    logInfo("[" + config.getConfigName() + "] Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase livingBase = (EntityLivingBase) entity;
                        logInfo("[" + config.getConfigName() + "] Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}",
                                entity.motionX, entity.motionY,
                                entity.motionZ,
                                livingBase.moveStrafing, livingBase.moveForward);
                    }

                    if (config.getConfig().getLogging().logWithStackTraces()) {
                        logInfo("[" + config.getConfigName() + "] Move offset: ({0}, {1}, {2})", x, y, z);
                        logInfo("[" + config.getConfigName() + "] Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
                        logInfo("[" + config.getConfigName() + "] Entity: {0}", entity);
                        NBTTagCompound tag = new NBTTagCompound();
                        entity.writeToNBT(tag);
                        logInfo("[" + config.getConfigName() + "] Entity NBT: {0}", tag);
                        logStack(config);
                    }
                }
                if (entity instanceof EntityPlayer) { // Skip killing players
                    entity.motionX = 0;
                    entity.motionY = 0;
                    entity.motionZ = 0;
                    return false;
                }
                // Remove the entity;
                entity.isDead = true;
                return false;
            }
        }
        return true;
    }

    // TODO - needs to be hooked
    @SuppressWarnings("rawtypes")
    public static void logEntitySize(Entity entity, List list) {
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (!config.getConfig().getLogging().logEntityCollisionChecks()) {
            return;
        }
        int collisionWarnSize = config.getConfig().getEntity().getMaxCollisionSize();

        if (list == null) {
            return;
        }

        if (collisionWarnSize > 0 && (MinecraftServer.getServer().getTickCounter() % 10) == 0 && list.size() >= collisionWarnSize) {
            SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.worldObj, entity);
            if (SpongeHooks.recentWarnings.contains(warning)) {
                long lastWarned = SpongeHooks.recentWarnings.get(warning);
                if ((MinecraftServer.getCurrentTimeMillis() - lastWarned) < 30000) {
                    return;
                }
            }
            SpongeHooks.recentWarnings.put(warning, System.currentTimeMillis());
            logWarning("Entity collision > {0, number} at: {1}", collisionWarnSize, entity);
        }
    }

    private static class CollisionWarning {

        public BlockPos blockPos;
        public int dimensionId;

        public CollisionWarning(World world, Entity entity) {
            this.dimensionId = world.provider.getDimensionId();
            this.blockPos = new BlockPos(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
        }

        @Override
        public boolean equals(Object otherObj) {
            if (!(otherObj instanceof CollisionWarning) || (otherObj == null)) {
                return false;
            }
            CollisionWarning other = (CollisionWarning) otherObj;
            return (other.dimensionId == this.dimensionId) && other.blockPos.equals(this.blockPos);
        }

        @Override
        public int hashCode() {
            return this.blockPos.hashCode() + this.dimensionId;
        }
    }

    @SuppressWarnings("rawtypes")
    public static void writeChunks(File file, boolean logAll) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            FileWriter fileWriter = new FileWriter(file);
            JsonWriter writer = new JsonWriter(fileWriter);
            writer.setIndent("  ");
            writer.beginArray();

            for (net.minecraft.world.WorldServer world : DimensionManager.getWorlds()) {
                writer.beginObject();
                writer.name("name").value(world.provider.getSaveFolder());
                writer.name("dimensionId").value(world.provider.getDimensionId());
                writer.name("players").value(world.playerEntities.size());
                writer.name("loadedChunks").value(world.theChunkProviderServer.loadedChunks.size());
                writer.name("activeChunks").value(world.activeChunkSet.size());
                writer.name("entities").value(world.loadedEntityList.size());
                writer.name("tiles").value(world.loadedTileEntityList.size());

                TObjectIntHashMap<ChunkCoordIntPair> chunkEntityCounts = new TObjectIntHashMap<ChunkCoordIntPair>();
                TObjectIntHashMap<Class> classEntityCounts = new TObjectIntHashMap<Class>();
                TObjectIntHashMap<Entity> entityCollisionCounts = new TObjectIntHashMap<Entity>();
                Set<BlockPos> collidingCoords = new HashSet<BlockPos>();
                for (int i = 0; i < world.loadedEntityList.size(); i++) {
                    Entity entity = (Entity) world.loadedEntityList.get(i);
                    ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair((int) entity.posX >> 4, (int) entity.posZ >> 4);
                    chunkEntityCounts.adjustOrPutValue(chunkCoords, 1, 1);
                    classEntityCounts.adjustOrPutValue(entity.getClass(), 1, 1);
                    if ((entity.getBoundingBox() != null) && logAll) {
                        BlockPos coords = new BlockPos((int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ));
                        if (!collidingCoords.contains(coords)) {
                            collidingCoords.add(coords);
                            int size = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, entity.getBoundingBox().expand(1, 1, 1)).size();
                            if (size < 5) {
                                continue;
                            }
                            entityCollisionCounts.put(entity, size);
                        }
                    }
                }

                TObjectIntHashMap<ChunkCoordIntPair> chunkTileCounts = new TObjectIntHashMap<ChunkCoordIntPair>();
                TObjectIntHashMap<Class> classTileCounts = new TObjectIntHashMap<Class>();
                writer.name("tiles").beginArray();
                for (int i = 0; i < world.loadedTileEntityList.size(); i++) {
                    TileEntity tile = (TileEntity) world.loadedTileEntityList.get(i);
                    if (logAll) {
                        writer.beginObject();
                        writer.name("type").value(tile.getClass().toString());
                        writer.name("x").value(tile.getPos().getX());
                        writer.name("y").value(tile.getPos().getY());
                        writer.name("z").value(tile.getPos().getZ());
                        writer.name("isInvalid").value(tile.isInvalid());
                        // writer.name("canUpdate").value(tile.canUpdate());
                        writer.name("block").value("" + tile.getBlockType());
                        writer.endObject();
                    }
                    ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4);
                    chunkTileCounts.adjustOrPutValue(chunkCoords, 1, 1);
                    classTileCounts.adjustOrPutValue(tile.getClass(), 1, 1);
                }
                writer.endArray();

                if (logAll) {
                    writeChunkCounts(writer, "topEntityColliders", entityCollisionCounts, 20);
                }

                writeChunkCounts(writer, "entitiesByClass", classEntityCounts);
                writeChunkCounts(writer, "entitiesByChunk", chunkEntityCounts);

                writeChunkCounts(writer, "tilesByClass", classTileCounts);
                writeChunkCounts(writer, "tilesByChunk", chunkTileCounts);

                writer.endObject(); // Dimension
            }
            writer.endArray(); // Dimensions
            writer.close();
            fileWriter.close();
        } catch (Throwable throwable) {
            MinecraftServer.getServer().logSevere("Could not save chunk info report to " + file);
        }
    }

    private static <T> void writeChunkCounts(JsonWriter writer, String name, final TObjectIntHashMap<T> map) throws IOException {
        writeChunkCounts(writer, name, map, 0);
    }

    private static <T> void writeChunkCounts(JsonWriter writer, String name, final TObjectIntHashMap<T> map, int max) throws IOException {
        List<T> sortedCoords = new ArrayList<T>(map.keySet());
        Collections.sort(sortedCoords, new Comparator<T>() {

            @Override
            public int compare(T s1, T s2) {
                return map.get(s2) - map.get(s1);
            }
        });

        int i = 0;
        writer.name(name).beginArray();
        for (T key : sortedCoords) {
            if ((max > 0) && (i++ > max)) {
                break;
            }
            if (map.get(key) < 5) {
                continue;
            }
            writer.beginObject();
            writer.name("key").value(key.toString());
            writer.name("count").value(map.get(key));
            writer.endObject();
        }
        writer.endArray();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void dumpHeap(File file, boolean live) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspotMBean, file.getPath(), live);
        } catch (Throwable t) {
            logSevere("Could not write heap to {0}", file);
        }
    }

    public static void enableThreadContentionMonitoring() {
        if (!CoreMixinPlugin.getGlobalConfig().getConfig().getDebug().isEnableThreadContentionMonitoring()) {
            return;
        }
        java.lang.management.ThreadMXBean mbean = java.lang.management.ManagementFactory.getThreadMXBean();
        mbean.setThreadContentionMonitoringEnabled(true);
    }

    public static SpongeConfig<?> getActiveConfig(World world) {
        SpongeConfig<?> config = ((IMixinWorld) world).getWorldConfig();
        if (config.getConfig().isConfigEnabled()) {
            return config;
        } else if (((IMixinWorldProvider) world.provider).getDimensionConfig() != null && ((IMixinWorldProvider) world.provider)
                .getDimensionConfig().getConfig().isConfigEnabled()) {
            return ((IMixinWorldProvider) world.provider).getDimensionConfig();
        } else {
            return CoreMixinPlugin.getGlobalConfig();
        }
    }

    public static void setBlockState(World world, Vector3i position, BlockState state) {
        setBlockState(world, position.getX(), position.getY(), position.getZ(), state);
    }

    public static void setBlockState(World world, int x, int y, int z, BlockState state) {
        setBlockState(world, new BlockPos(x, y, z), state);
    }

    public static void setBlockState(World world, BlockPos position, BlockState state) {
        if (state instanceof IBlockState) {
            // 0 is no notify flag. For now not going to notify nearby blocks of update.
            world.setBlockState(position, (IBlockState) state, 0);
        } else {
            // TODO: Need to figure out what is sensible for other BlockState implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }
}
