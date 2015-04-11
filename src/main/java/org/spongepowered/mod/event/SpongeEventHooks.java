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
package org.spongepowered.mod.event;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.util.SpongeHooks;

public class SpongeEventHooks {

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onChunkWatchEvent(ChunkWatchEvent event) {
        IMixinEntity spongeEntity = (IMixinEntity) event.player;

        if (spongeEntity.isTeleporting()) {
            event.player.mountEntity(spongeEntity.getTeleportVehicle());
            spongeEntity.setTeleportVehicle(null);
            spongeEntity.setIsTeleporting(false);
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onEntityDeathEvent(LivingDeathEvent event) {
        SpongeHooks.logEntityDeath(event.entity);
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChunkPrePopulate(PopulateChunkEvent.Pre event) {
        World world = (World) event.world;
        int chunkStartX = event.chunkX;
        int chunkStartZ = event.chunkZ;

        Chunk chunk = (Chunk) event.chunkProvider.provideChunk(event.chunkX, event.chunkZ);
        if (chunk == null) {
            // When the chunk is null, there's a bug somewhere in the server
            // Better not pass this null value to all plugins, that will make
            // it look like the plugins are in error
            throw new NullPointerException("Failed to populate chunk at (" + event.chunkX + "," + event.chunkZ + ")");
        }
        for (Populator populator : ((IMixinWorld) world).getPopulators()) {
            populator.populate(chunk, event.rand);
        }
        for (Populator populator : world.getBiome(chunkStartX + 15, chunkStartZ + 15).getPopulators()) {
            populator.populate(chunk, event.rand);
        }
    }

}
