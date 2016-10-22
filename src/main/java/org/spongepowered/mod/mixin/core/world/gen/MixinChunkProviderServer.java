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
package org.spongepowered.mod.mixin.core.world.gen;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Set;

@Mixin(value = ChunkProviderServer.class, priority = 999)
public abstract class MixinChunkProviderServer {

    @Shadow @Final public WorldServer worldObj;
    @Shadow @Final private Set<Long> droppedChunksSet;
    @Shadow @Final public IChunkLoader chunkLoader;
    @Shadow @Final public Long2ObjectMap<Chunk> id2ChunkMap;

    @Shadow public abstract void saveChunkExtraData(Chunk chunkIn);
    @Shadow public abstract void saveChunkData(Chunk chunkIn);

    /**
     * @author blood - October 20th, 2016
     * @reason Removes forge's check for persistent chunks as we cache this in the Chunk itself
     * 
     * @return true if unload queue was processed
     */
    @Overwrite
    public boolean unloadQueuedChunks()
    {
        if (!this.worldObj.disableLevelSaving)
        {
            if (!this.droppedChunksSet.isEmpty())
            {
                // Sponge start - disable persistent chunk check as it is cached within the Chunk
                /*
                for (ChunkPos forced : this.worldObj.getPersistentChunks().keySet())
                {
                    this.droppedChunksSet.remove(ChunkPos.chunkXZ2Int(forced.chunkXPos, forced.chunkZPos));
                }*/
                // Sponge end

                Iterator<Long> iterator = this.droppedChunksSet.iterator();

                for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove())
                {
                    Long olong = (Long)iterator.next();
                    Chunk chunk = (Chunk)this.id2ChunkMap.get(olong);

                    // Sponge - we need to check if the chunk is persisted in case chunk gc is disabled
                    if (chunk != null && chunk.unloaded)
                    {
                        chunk.onChunkUnload();
                        this.saveChunkData(chunk);
                        this.saveChunkExtraData(chunk);
                        this.id2ChunkMap.remove(olong);
                        ++i;
                        net.minecraftforge.common.ForgeChunkManager.putDormantChunk(ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
                        if (id2ChunkMap.size() == 0 && /*net.minecraftforge.common.ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 &&*/ !this.worldObj.provider.getDimensionType().shouldLoadSpawn()){
                            net.minecraftforge.common.DimensionManager.unloadWorld(this.worldObj.provider.getDimension());
                            break;
                        }
                    }
                }
            }

            this.chunkLoader.chunkTick();
        }

        return false;
    }
}
