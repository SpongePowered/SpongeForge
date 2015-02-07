/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

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

import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.interfaces.IMixinWorldProvider;

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

import com.flowpowered.math.vector.Vector3i;
import com.google.gson.stream.JsonWriter;

public class SpongeHooks {

    public static int tickingDimension = 0;
    public static ChunkCoordIntPair tickingChunk = null;
    //public static Map<Class<? extends TileEntity>, TileEntityCache> tileEntityCache = new HashMap<Class<? extends TileEntity>, TileEntityCache>();

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

    public static void logStack(SpongeConfig config) {
        if (config.logWithStackTraces.getProperty().getBoolean()) {
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
    }

    public static void logEntityDeath(Entity entity) {
        SpongeConfig config = getActiveConfig(entity.worldObj);
        if (config.entityDeathLogging.getProperty().getBoolean()) {
            logInfo("Dim: {0} setDead(): {1}", entity.worldObj.provider.getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logEntityDespawn(Entity entity, String reason) {
        SpongeConfig config = getActiveConfig(entity.worldObj);
        if (config.entityDespawnLogging.getProperty().getBoolean()) {
            logInfo("Dim: {0} Despawning ({1}): {2}", entity.worldObj.provider.getDimensionId(), reason, entity);
            //logInfo("Chunk Is Active: {0}", entity.worldObj.inActiveChunk(entity));
            logStack(config);
        }
    }

    public static void logEntitySpawn(Entity entity) {
        SpongeConfig config = getActiveConfig(entity.worldObj);
        if (config.entitySpawnLogging.getProperty().getBoolean()) {
            logInfo("Dim: {0} Spawning: {1}", entity.worldObj.provider.getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logChunkLoad(World world, Vector3i chunkPos) {
        SpongeConfig config = getActiveConfig(world);
        if (config.chunkLoadLogging.getProperty().getBoolean()) {
            logInfo("Load Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(), chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logChunkUnload(World world, Vector3i chunkPos) {
        SpongeConfig config = getActiveConfig(world);
        if (config.chunkLoadLogging.getProperty().getBoolean()) {
            logInfo("Unload Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(), chunkPos.getZ());
            logStack(config);
        }
    }

    private static void logChunkLoadOverride(ChunkProviderServer provider, int x, int z) {
        logInfo(" Chunk Load Override: {0}, Dimension ID: {1}", provider.chunkLoadOverride, provider.worldObj.provider.getDimensionId());
    }

    public static boolean checkBoundingBoxSize(Entity entity, AxisAlignedBB aabb) {
        SpongeConfig config = getActiveConfig(entity.worldObj);
        if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer) return false; // only check living entities that are not players

        int logSize = config.largeBoundingBoxLogSize.getProperty().getInt();
        if (logSize <= 0 || !config.checkEntityBoundingBoxes.getProperty().getBoolean()) return false;
        int x = MathHelper.floor_double(aabb.minX);
        int x1 = MathHelper.floor_double(aabb.maxX + 1.0D);
        int y = MathHelper.floor_double(aabb.minY);
        int y1 = MathHelper.floor_double(aabb.maxY + 1.0D);
        int z = MathHelper.floor_double(aabb.minZ);
        int z1 = MathHelper.floor_double(aabb.maxZ + 1.0D);
        
        int size = Math.abs(x1-x) * Math.abs(y1-y) * Math.abs(z1-z);
        if (size > config.largeBoundingBoxLogSize.getProperty().getInt()) {
            logWarning("Entity being removed for bounding box restrictions");
            logWarning("BB Size: {0} > {1} avg edge: {2}", size, logSize, aabb.getAverageEdgeLength());
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
        SpongeConfig config = getActiveConfig(entity.worldObj);
        int maxSpeed = config.entityMaxSpeed.getProperty().getInt();
        if (maxSpeed > 0 && config.checkEntityMaxSpeeds.getProperty().getBoolean()) {
            double distance = x * x + z * z;
            if (distance > maxSpeed) {
                if (config.logEntitySpeedRemoval.getProperty().getBoolean()) {
                    logInfo("Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase livingBase = (EntityLivingBase)entity;
                        logInfo("Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}", entity.motionX, entity.motionY, entity.motionZ, livingBase.moveStrafing, livingBase.moveForward);
                    }

                    if (config.logWithStackTraces.getProperty().getBoolean()) {
                        logInfo("Move offset: ({0}, {1}, {2})", x, y, z);
                        logInfo("Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
                        logInfo("Entity: {0}", entity);
                        NBTTagCompound tag = new NBTTagCompound();
                        entity.writeToNBT(tag);
                        logInfo("Entity NBT: {0}", tag);
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
    public static void logEntitySize(Entity entity, List list) {
        SpongeConfig config = getActiveConfig(entity.worldObj);
        if (!config.logEntityCollisionChecks.getProperty().getBoolean()) return;
        int largeCountLogSize = config.largeCollisionLogSize.getProperty().getInt();
        /*if (largeCountLogSize > 0 && entity.worldObj.entitiesTicked > largeCountLogSize)
        {
            logWarning("Entity size > {0, number} at: {1}", largeCountLogSize, entity);
        }*/
        if (list == null) return;
        int largeCollisionLogSize = config.largeCollisionLogSize.getProperty().getInt();
        if (largeCollisionLogSize > 0 && (MinecraftServer.getServer().getTickCounter() % 10) == 0 && list.size() >= largeCollisionLogSize) {
            SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.worldObj, entity);
            if (recentWarnings.contains(warning)) {
                long lastWarned = recentWarnings.get(warning);
                if ((MinecraftServer.getCurrentTimeMillis() - lastWarned) < 30000) return;
            }
            recentWarnings.put(warning, System.currentTimeMillis());
            logWarning("Entity collision > {0, number} at: {1}", largeCollisionLogSize, entity);
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
            if (!(otherObj instanceof CollisionWarning) || (otherObj == null)) return false;
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
                        BlockPos coords = new BlockPos((int)Math.floor(entity.posX), (int)Math.floor(entity.posY), (int)Math.floor(entity.posZ));
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
                        //writer.name("canUpdate").value(tile.canUpdate());
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
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
        if (!SpongeMod.instance.getGlobalConfig().enableThreadContentionMonitoring.getProperty().getBoolean()) return;
        java.lang.management.ThreadMXBean mbean = java.lang.management.ManagementFactory.getThreadMXBean();
        mbean.setThreadContentionMonitoringEnabled(true);
    }

    public static SpongeConfig getActiveConfig(World world) {
        SpongeConfig config = ((IMixinWorld)world).getWorldConfig();
        if (config.configEnabled.getProperty().getBoolean()) {
            return config;
        } else if (((IMixinWorldProvider)world.provider).getDimensionConfig() != null && ((IMixinWorldProvider)world.provider).getDimensionConfig().configEnabled.getProperty().getBoolean()) {
            return ((IMixinWorldProvider)world.provider).getDimensionConfig();
        } else {
            return SpongeMod.instance.getGlobalConfig();
        }
    }
}
