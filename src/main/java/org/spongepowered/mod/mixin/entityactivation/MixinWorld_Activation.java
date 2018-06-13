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
package org.spongepowered.mod.mixin.entityactivation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.plugin.entityactivation.EntityActivationRange;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;

@NonnullByDefault
@Mixin(value = net.minecraft.world.World.class, priority = 999)
public abstract class MixinWorld_Activation implements IMixinWorld {


    @Shadow public abstract void updateEntity(Entity ent);

    /**
     * @author blood
     * @reason Activation range checks.
     *
     * @param entityIn
     * @param forceUpdate
     */
    @Overwrite
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate)
    {
        // Sponge start - area is handled in ActivationRange
        //int i = MathHelper.floor_double(entityIn.posX);
        //int j = MathHelper.floor_double(entityIn.posZ);
        //boolean isForced = getPersistentChunks().containsKey(new net.minecraft.util.math.ChunkPos(i >> 4, j >> 4));
        //int k = isForced ? 0 : 32;
        //boolean canUpdate = !forceUpdate || this.isAreaLoaded(i - k, 0, j - k, i + k, 0, j + k, true);
        boolean canUpdate = EntityActivationRange.checkIfActive(entityIn);
        // Allow forge mods to force an update
        if (!canUpdate) canUpdate = net.minecraftforge.event.ForgeEventFactory.canEntityUpdate(entityIn);

        if (!canUpdate) {
            entityIn.ticksExisted++;
            ((IModData_Activation) entityIn).inactiveTick();
            return;
        }
        // Sponge end
        entityIn.lastTickPosX = entityIn.posX;
        entityIn.lastTickPosY = entityIn.posY;
        entityIn.lastTickPosZ = entityIn.posZ;
        entityIn.prevRotationYaw = entityIn.rotationYaw;
        entityIn.prevRotationPitch = entityIn.rotationPitch;

        if (forceUpdate && entityIn.addedToChunk)
        {
            ++entityIn.ticksExisted;
            ++co.aikar.timings.TimingHistory.activatedEntityTicks; // Sponge

            if (entityIn.isRiding())
            {
                entityIn.updateRidden();
            }
            else
            {
                entityIn.onUpdate();
            }
        }

        //this.theProfiler.startSection("chunkCheck");

        if (Double.isNaN(entityIn.posX) || Double.isInfinite(entityIn.posX))
        {
            entityIn.posX = entityIn.lastTickPosX;
        }

        if (Double.isNaN(entityIn.posY) || Double.isInfinite(entityIn.posY))
        {
            entityIn.posY = entityIn.lastTickPosY;
        }

        if (Double.isNaN(entityIn.posZ) || Double.isInfinite(entityIn.posZ))
        {
            entityIn.posZ = entityIn.lastTickPosZ;
        }

        if (Double.isNaN(entityIn.rotationPitch) || Double.isInfinite(entityIn.rotationPitch))
        {
            entityIn.rotationPitch = entityIn.prevRotationPitch;
        }

        if (Double.isNaN(entityIn.rotationYaw) || Double.isInfinite(entityIn.rotationYaw))
        {
            entityIn.rotationYaw = entityIn.prevRotationYaw;
        }

        int l = MathHelper.floor(entityIn.posX / 16.0D);
        int i1 = MathHelper.floor(entityIn.posY / 16.0D);
        int j1 = MathHelper.floor(entityIn.posZ / 16.0D);

        if (!entityIn.addedToChunk || entityIn.chunkCoordX != l || entityIn.chunkCoordY != i1 || entityIn.chunkCoordZ != j1)
        {
            // Sponge start - use cached chunk
            final Chunk activeChunk = (Chunk) ((IMixinEntity) entityIn).getActiveChunk();
            if (activeChunk != null)
            {
                activeChunk.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
            }
            // Sponge end

            final IMixinChunk newChunk = (IMixinChunk) ((IMixinChunkProviderServer) entityIn.world.getChunkProvider()).getLoadedChunkWithoutMarkingActive(l, j1);
            final boolean isPositionDirty = entityIn.setPositionNonDirty();
            if (newChunk == null || (!isPositionDirty && newChunk.isQueuedForUnload() && !newChunk.isPersistedChunk())) {
                entityIn.addedToChunk = false;
            }
            else
            {
                ((net.minecraft.world.chunk.Chunk) newChunk).addEntity(entityIn);
            }
        }

        //this.theProfiler.endSection();

        if (forceUpdate && entityIn.addedToChunk)
        {
            for (Entity entity : entityIn.getPassengers())
            {
                if (!entity.isDead && entity.getRidingEntity() == entityIn)
                {
                    this.updateEntity(entity);
                }
                else
                {
                    entity.dismountRidingEntity();
                }
            }
        }
    }
}