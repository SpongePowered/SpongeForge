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
package org.spongepowered.mod.mixin.entity;

import java.util.ArrayDeque;
import java.util.UUID;

import javax.annotation.Nullable;

import com.flowpowered.math.vector.Vector3f;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.util.SpongeHooks;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;

@NonnullByDefault
@Mixin(net.minecraft.entity.Entity.class)
public abstract class MixinEntity implements Entity, IMixinEntity {

    private boolean teleporting;
    private net.minecraft.entity.Entity teleportVehicle;

    @Shadow private UUID entityUniqueID;
    @Shadow public net.minecraft.world.World worldObj;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public float width;
    @Shadow public float height;
    @Shadow public boolean isDead;
    @Shadow public boolean onGround;
    @Shadow public int fireResistance;
    @Shadow private int fire;
    @Shadow public net.minecraft.entity.Entity riddenByEntity;
    @Shadow public net.minecraft.entity.Entity ridingEntity;

    @Shadow
    public abstract void setPosition(double x, double y, double z);
    @Shadow(prefix = "shadow$")
    protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow
    public abstract void mountEntity(net.minecraft.entity.Entity entityIn);

    @Inject(method = "moveEntity(DDD)V", at = @At("HEAD"), cancellable = true)
    public void onMoveEntity(double x, double y, double z, CallbackInfo ci) {
        if (!this.worldObj.isRemote && !SpongeHooks.checkEntitySpeed(((net.minecraft.entity.Entity)(Object)this), x, y, z)) {
            ci.cancel();
        }
    }

    @Override
    public World getWorld() {
        return (World)this.worldObj;
    }

    @Override
    public Location getLocation() {
        return new Location((Extent)this.worldObj, new Vector3d(this.posX, this.posY, this.posZ));
    }

    @Override
    public boolean setLocation(Location location) {
        if (isRemoved()) {
            return false;
        }

        Entity spongeEntity = (Entity)this;
        net.minecraft.entity.Entity thisEntity = (net.minecraft.entity.Entity)spongeEntity;

        // dettach passengers
        net.minecraft.entity.Entity passenger = thisEntity.riddenByEntity;
        ArrayDeque<net.minecraft.entity.Entity> passengers = new ArrayDeque<net.minecraft.entity.Entity>();
        while (passenger != null) {
            if (passenger instanceof EntityPlayerMP && !this.worldObj.isRemote) {
                ((EntityPlayerMP)passenger).mountEntity(null);
            }
            net.minecraft.entity.Entity nextPassenger = null;
            if (passenger.riddenByEntity != null) {
                nextPassenger = passenger.riddenByEntity;
                this.riddenByEntity.mountEntity(null);
            }
            passengers.add(passenger);
            passenger = nextPassenger;
        }

        net.minecraft.world.World nmsWorld = null;
        if (location.getExtent() instanceof World && ((net.minecraft.world.World)location.getExtent() != this.worldObj)) {
            if (!(thisEntity instanceof EntityPlayer)) {
                nmsWorld = (net.minecraft.world.World)location.getExtent();
                teleportEntity(thisEntity, location, thisEntity.dimension, nmsWorld.provider.getDimensionId());
            }
        } else {
            setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
            if(thisEntity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) thisEntity).playerNetServerHandler.setPlayerLocation(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ(), thisEntity.rotationYaw, thisEntity.rotationPitch);
            }
        }

        // reattach passengers
        net.minecraft.entity.Entity lastPassenger = thisEntity;
        while (!passengers.isEmpty()) {
            net.minecraft.entity.Entity passengerEntity = passengers.remove();
            if (nmsWorld != null) {
                teleportEntity(passengerEntity, location, passengerEntity.dimension, nmsWorld.provider.getDimensionId());
            }

            if (passengerEntity instanceof EntityPlayerMP && !this.worldObj.isRemote) {
                // The actual mount is handled in our event as mounting must be set after client fully loads.
                ((IMixinEntity)passengerEntity).setIsTeleporting(true);
                ((IMixinEntity)passengerEntity).setTeleportVehicle(lastPassenger);
            } else {
                passengerEntity.mountEntity(lastPassenger);
            }
            lastPassenger = passengerEntity;
        }

        return true;
    }

    @Override
    public Vector3f getRotation() {
        return new Vector3f(this.rotationYaw, this.rotationPitch, 0);
    }

    @Override
    public void setRotation(Vector3f rotation) {
        shadow$setRotation(rotation.getX(), rotation.getY());
    }

    @Override
    public Optional<Entity> getPassenger() {
        return Optional.fromNullable((Entity)this.riddenByEntity);
    }

    @Override
    public Optional<Entity> getVehicle() {
        return Optional.fromNullable((Entity)this.ridingEntity);
    }

    @Override
    public Entity getBaseVehicle() {
        if (this.ridingEntity == null) {
          return (Entity)this;
        }

        net.minecraft.entity.Entity baseVehicle = this.ridingEntity;
        while (baseVehicle.ridingEntity != null) {
            baseVehicle = baseVehicle.ridingEntity;
        }
        return (Entity)baseVehicle;
    }

    @Override
    public boolean setPassenger(@Nullable Entity entity) {
        net.minecraft.entity.Entity passenger = (net.minecraft.entity.Entity)entity;
        if (this.riddenByEntity == null) { // no existing passenger
            if (passenger == null) {
                return true;
            }

            Entity thisEntity = (Entity)this;
            passenger.mountEntity((net.minecraft.entity.Entity)thisEntity);
        } else { // passenger already exists
            this.riddenByEntity.mountEntity(null); // eject current passenger

            if (passenger != null) {
                Entity thisEntity = (Entity)this;
                passenger.mountEntity((net.minecraft.entity.Entity)thisEntity);
            }
        }

        return true;
    }

    @Override
    public boolean setVehicle(@Nullable Entity entity) {
        mountEntity((net.minecraft.entity.Entity)entity);
        return true;
    }

    @Override
    public float getBase() {
        return this.width;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public float getScale() {
        // TODO
        return 0;
    }

    @Override
    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public boolean isRemoved() {
        return this.isDead;
    }

    @Override
    public boolean isLoaded() {
        // TODO - add flag for entities loaded/unloaded into world
        return !isRemoved();
    }

    @Override
    public void remove() {
        this.isDead = true;
    }

    @Override
    public int getFireTicks() {
        return this.fire;
    }

    @Override
    public void setFireTicks(int ticks) {
        this.fire = ticks;
    }

    @Override
    public int getFireDelay() {
        return this.fireResistance;
    }

    // for sponge internal use only
    @SuppressWarnings("unchecked")
    private void teleportEntity(net.minecraft.entity.Entity entity, Location location, int currentDim, int targetDim) {
        MinecraftServer mcServer = MinecraftServer.getServer();
        WorldServer fromWorld = mcServer.worldServerForDimension(currentDim);
        WorldServer toWorld = mcServer.worldServerForDimension(targetDim);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP)entity);
            fromWorld.getPlayerManager().removePlayer((EntityPlayerMP)entity);
            mcServer.getConfigurationManager().playerEntityList.remove((EntityPlayerMP)entity);
        } else {
            fromWorld.getEntityTracker().untrackEntity(entity);
        }

        entity.worldObj.removePlayerEntityDangerously(entity);
        entity.dimension = targetDim;
        entity.setWorld(toWorld);
        entity.isDead = false;
        entity.setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
        toWorld.theChunkProviderServer.loadChunk((int)entity.posX >> 4, (int)entity.posZ >> 4);
        while (!toWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D)
        {
            entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
        }

        if (entity instanceof EntityPlayer) {
            EntityPlayerMP entityplayermp1 = (EntityPlayerMP)entity;
            entityplayermp1.isDead = false;
            entityplayermp1.playerNetServerHandler.sendPacket(new S07PacketRespawn(targetDim, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), entityplayermp1.theItemInWorldManager.getGameType()));
            entityplayermp1.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(entityplayermp1.experience, entityplayermp1.experienceTotal, entityplayermp1.experienceLevel));
            entityplayermp1.setSneaking(false);
            mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(entityplayermp1, toWorld);
            toWorld.getPlayerManager().addPlayer(entityplayermp1);
            toWorld.spawnEntityInWorld(entityplayermp1);
            mcServer.getConfigurationManager().playerEntityList.add(entityplayermp1);
            entityplayermp1.theItemInWorldManager.setWorld(toWorld);
            entityplayermp1.addSelfToInternalCraftingInventory();
            entityplayermp1.setHealth(entityplayermp1.getHealth());
        } else {
            toWorld.spawnEntityInWorld(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
    }

    @Override
    public boolean isTeleporting() {
        return this.teleporting;
    }

    @Override
    public net.minecraft.entity.Entity getTeleportVehicle() {
        return this.teleportVehicle;
    }

    @Override
    public void setIsTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    @Override
    public void setTeleportVehicle(net.minecraft.entity.Entity vehicle) {
        this.teleportVehicle = vehicle;
    }

    @Override
    public boolean isPersistent() {
        // TODO
        return true;
    }

    @Override
    public void setPersistent(boolean persistent) {
        // TODO
    }

    @Override
    public <T> Optional<T> getData(Class<T> dataClass) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public EntityType getType() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public EntitySnapshot getSnapshot() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public UUID getUniqueId() {
        return this.entityUniqueID;
    }
}
