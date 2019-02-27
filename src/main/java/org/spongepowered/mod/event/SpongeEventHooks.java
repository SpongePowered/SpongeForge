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

import net.minecraftforge.common.ForgeChunkManager.ForceChunkEvent;
import net.minecraftforge.common.ForgeChunkManager.UnforceChunkEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.util.SpongeHooks;

public class SpongeEventHooks {

    @SubscribeEvent
    public void onChunkWatchEvent(ChunkWatchEvent event) {
        IMixinEntity spongeEntity = (IMixinEntity) event.getPlayer();

        if (spongeEntity.isTeleporting()) {
            spongeEntity.getTeleportVehicle().getPassengers().add(event.getPlayer());
            spongeEntity.setTeleportVehicle(null);
            spongeEntity.setIsTeleporting(false);
        }
    }

    @SubscribeEvent
    public void onEntityDeathEvent(LivingDeathEvent event) {
        SpongeHooks.logEntityDeath(event.getEntity());
    }

    @SubscribeEvent
    public void onForceChunk(ForceChunkEvent event) {
        final net.minecraft.world.chunk.Chunk chunk = ((IMixinChunkProviderServer) event.getTicket().world.getChunkProvider())
                .getLoadedChunkWithoutMarkingActive(event.getLocation().x,  event.getLocation().z);
        if (chunk != null) {
            ((IMixinChunk) chunk).setPersistedChunk(true);
        }
    }

    @SubscribeEvent
    public void onUnforceChunk(UnforceChunkEvent event) {
        final net.minecraft.world.chunk.Chunk chunk = ((IMixinChunkProviderServer) event.getTicket().world.getChunkProvider())
                .getLoadedChunkWithoutMarkingActive(event.getLocation().x,  event.getLocation().z);
        if (chunk != null) {
            ((IMixinChunk) chunk).setPersistedChunk(false);
        }
    }
}
