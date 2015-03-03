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
package org.spongepowered.mod.mixin.entityactivation;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.mixin.plugin.entityactivation.ActivationRange;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    @Shadow
    public Profiler theProfiler;

    @Shadow
    public abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);

    @Shadow
    public abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Inject(method = "updateEntities()V", at = @At(value = "INVOKE_STRING",
            target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = {"ldc=regular"}))
    private void onInvokeProfiler(CallbackInfo ci) {
        if (!((net.minecraft.world.World) (Object) this).isRemote) {
            ActivationRange.activateEntities(((net.minecraft.world.World) (Object) this));
        }
    }

    @Overwrite
    public void updateEntityWithOptionalForce(net.minecraft.entity.Entity entity, boolean forceUpdate) {
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posZ);
        boolean isForcedChunk = ((net.minecraft.world.World) (Object) this).getPersistentChunks().containsKey(new ChunkCoordIntPair(i >> 4, j >> 4));
        byte b0 = isForcedChunk ? (byte) 0 : 32;
        boolean canUpdate = !forceUpdate || isAreaLoaded(i - b0, 0, j - b0, i + b0, 0, j + b0, true);

        if (!canUpdate) {
            EntityEvent.CanUpdate event = new EntityEvent.CanUpdate(entity);
            MinecraftForge.EVENT_BUS.post(event);
            canUpdate = event.canUpdate;
        }

        if (!isForcedChunk && !canUpdate && !ActivationRange.checkIfActive(entity)) { // ignore if forge event forced update or entity is in forced chunk
            entity.ticksExisted++;
            ((IMixinEntity) entity).inactiveTick();
            return;
        }

        if (canUpdate) {
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.prevRotationPitch = entity.rotationPitch;

            if (forceUpdate && entity.addedToChunk) {
                ++entity.ticksExisted;

                if (entity.ridingEntity != null) {
                    entity.updateRidden();
                } else {
                    entity.onUpdate();
                }
            }

            this.theProfiler.startSection("chunkCheck");

            if (Double.isNaN(entity.posX) || Double.isInfinite(entity.posX)) {
                entity.posX = entity.lastTickPosX;
            }

            if (Double.isNaN(entity.posY) || Double.isInfinite(entity.posY)) {
                entity.posY = entity.lastTickPosY;
            }

            if (Double.isNaN(entity.posZ) || Double.isInfinite(entity.posZ)) {
                entity.posZ = entity.lastTickPosZ;
            }

            if (Double.isNaN(entity.rotationPitch) || Double.isInfinite(entity.rotationPitch)) {
                entity.rotationPitch = entity.prevRotationPitch;
            }

            if (Double.isNaN(entity.rotationYaw) || Double.isInfinite(entity.rotationYaw)) {
                entity.rotationYaw = entity.prevRotationYaw;
            }

            int k = MathHelper.floor_double(entity.posX / 16.0D);
            int l = MathHelper.floor_double(entity.posY / 16.0D);
            int i1 = MathHelper.floor_double(entity.posZ / 16.0D);

            if (!entity.addedToChunk || entity.chunkCoordX != k || entity.chunkCoordY != l || entity.chunkCoordZ != i1) {
                if (entity.addedToChunk && isChunkLoaded(entity.chunkCoordX, entity.chunkCoordZ, true)) {
                    ((net.minecraft.world.World) (Object) this).getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(
                            entity, entity.chunkCoordY);
                }

                if (this.isChunkLoaded(k, i1, true)) {
                    entity.addedToChunk = true;
                    ((net.minecraft.world.World) (Object) this).getChunkFromChunkCoords(k, i1).addEntity(entity);
                } else {
                    entity.addedToChunk = false;
                }
            }

            this.theProfiler.endSection();

            if (forceUpdate && entity.addedToChunk && entity.riddenByEntity != null) {
                if (!entity.riddenByEntity.isDead && entity.riddenByEntity.ridingEntity == entity) {
                    ((net.minecraft.world.World) (Object) this).updateEntity(entity.riddenByEntity);
                } else {
                    entity.riddenByEntity.ridingEntity = null;
                    entity.riddenByEntity = null;
                }
            }
        }
    }
}
