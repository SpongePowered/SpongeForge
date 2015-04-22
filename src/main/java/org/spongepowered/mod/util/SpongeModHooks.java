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

import com.google.gson.stream.JsonWriter;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpongeModHooks {

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


}
