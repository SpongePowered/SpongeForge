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
package org.spongepowered.mod.mixin.core.entity;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook.EnumFlags;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.registry.SpongeGameRegistry;
import org.spongepowered.mod.util.SpongeHooks;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.entity.Entity.class)
public abstract class MixinEntity implements Entity, IMixinEntity {

    private EntityType entityType;
    private boolean teleporting;
    private net.minecraft.entity.Entity teleportVehicle;
    private float origWidth;
    private float origHeight;

    @Shadow
    private UUID entityUniqueID;

    @Shadow
    public net.minecraft.world.World worldObj;

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public float rotationYaw;

    @Shadow
    public float rotationPitch;

    @Shadow
    public float width;

    @Shadow
    public float height;

    @Shadow
    public boolean isDead;

    @Shadow
    public boolean onGround;

    @Shadow
    public int fireResistance;

    @Shadow
    private int fire;

    @Shadow
    public net.minecraft.entity.Entity riddenByEntity;

    @Shadow
    public net.minecraft.entity.Entity ridingEntity;

    @Shadow
    public abstract void setPosition(double x, double y, double z);

    @Shadow(prefix = "shadow$")
    protected abstract void shadow$setRotation(float yaw, float pitch);

    @Shadow
    public abstract void mountEntity(net.minecraft.entity.Entity entityIn);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, CallbackInfo ci) {
        this.entityType = ((SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry()).entityClassToTypeMappings.get(this.getClass());
    }

    @Inject(method = "setSize", at = @At("RETURN"))
    public void onSetSize(float width, float height, CallbackInfo ci) {
        if (this.origWidth == 0 || this.origHeight == 0) {
            this.origWidth = this.width;
            this.origHeight = this.height;
        }
    }

    @Inject(method = "moveEntity(DDD)V", at = @At("HEAD"), cancellable = true)
    public void onMoveEntity(double x, double y, double z, CallbackInfo ci) {
        if (!this.worldObj.isRemote && !SpongeHooks.checkEntitySpeed(((net.minecraft.entity.Entity) (Object) this), x, y, z)) {
            ci.cancel();
        }
    }

    @Override
    public World getWorld() {
        return (World) this.worldObj;
    }

    @Override
    public Location getLocation() {
        return new Location((Extent) this.worldObj, new Vector3d(this.posX, this.posY, this.posZ));
    }

    @Override
    public boolean setLocation(Location location) {
        if (isRemoved()) {
            return false;
        }

        Entity spongeEntity = this;
        net.minecraft.entity.Entity thisEntity = (net.minecraft.entity.Entity) spongeEntity;

        // detach passengers
        net.minecraft.entity.Entity passenger = thisEntity.riddenByEntity;
        ArrayDeque<net.minecraft.entity.Entity> passengers = new ArrayDeque<net.minecraft.entity.Entity>();
        while (passenger != null) {
            if (passenger instanceof EntityPlayerMP && !this.worldObj.isRemote) {
                ((EntityPlayerMP) passenger).mountEntity(null);
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
        if (location.getExtent() instanceof World && ((net.minecraft.world.World) location.getExtent() != this.worldObj)) {
            if (!(thisEntity instanceof EntityPlayer)) {
                nmsWorld = (net.minecraft.world.World) location.getExtent();
                teleportEntity(thisEntity, location, thisEntity.dimension, nmsWorld.provider.getDimensionId());
            }
        } else {
            setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
            if (thisEntity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) thisEntity).playerNetServerHandler
                        .setPlayerLocation(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ(),
                                thisEntity.rotationYaw, thisEntity.rotationPitch);
            }
        }

        // re-attach passengers
        net.minecraft.entity.Entity lastPassenger = thisEntity;
        while (!passengers.isEmpty()) {
            net.minecraft.entity.Entity passengerEntity = passengers.remove();
            if (nmsWorld != null) {
                teleportEntity(passengerEntity, location, passengerEntity.dimension, nmsWorld.provider.getDimensionId());
            }

            if (passengerEntity instanceof EntityPlayerMP && !this.worldObj.isRemote) {
                // The actual mount is handled in our event as mounting must be set after client fully loads.
                ((IMixinEntity) passengerEntity).setIsTeleporting(true);
                ((IMixinEntity) passengerEntity).setTeleportVehicle(lastPassenger);
            } else {
                passengerEntity.mountEntity(lastPassenger);
            }
            lastPassenger = passengerEntity;
        }

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean setLocationAndRotation(Location location, Vector3f rotation, EnumSet<RelativePositions> relativePositions) {
        if (relativePositions.isEmpty()) {
            //This is just a normal teleport that happens to set both.
            if (setLocation(location)) {
                setRotation(rotation);
                return true;
            }
            return false;
        } else {
            Entity spongeEntity = this;
            if (spongeEntity instanceof EntityPlayerMP) {
                //Players use different logic, as they support real relative movement.
                EnumSet relativeFlags = EnumSet.noneOf(EnumFlags.class);

                if (relativePositions.contains(RelativePositions.X)) {
                    relativeFlags.add(EnumFlags.X);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    relativeFlags.add(EnumFlags.Y);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    relativeFlags.add(EnumFlags.Z);
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    relativeFlags.add(EnumFlags.Y_ROT);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    relativeFlags.add(EnumFlags.X_ROT);
                }

                ((EntityPlayerMP) (Entity) this).playerNetServerHandler.setPlayerLocation(location.getPosition().getX(), location.getPosition()
                        .getY(),
                        location.getPosition().getZ(), rotation.getX(), rotation.getY(), relativeFlags);
                return true;
            } else {
                Location resultant = getLocation();
                Vector3f resultantRotation = getRotation();

                if (relativePositions.contains(RelativePositions.X)) {
                    resultant.add(location.getPosition().getX(), 0, 0);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    resultant.add(0, location.getPosition().getY(), 0);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    resultant.add(0, 0, location.getPosition().getZ());
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    resultantRotation.add(rotation.getX(), 0, 0);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    resultantRotation.add(0, rotation.getY(), 0);
                }

                //From here just a normal teleport is needed.
                if (setLocation(resultant)) {
                    setRotation(resultantRotation);
                    return true;
                }
                return false;
            }
        }
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
    public Vector3d getVelocity() {
        return new Vector3d(this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public void setVelocity(Vector3d velocity) {
        this.motionX = velocity.getX();
        this.motionY = velocity.getY();
        this.motionZ = velocity.getZ();
    }

    @Override
    public Optional<Entity> getPassenger() {
        return Optional.fromNullable((Entity) this.riddenByEntity);
    }

    @Override
    public Optional<Entity> getVehicle() {
        return Optional.fromNullable((Entity) this.ridingEntity);
    }

    @Override
    public Entity getBaseVehicle() {
        if (this.ridingEntity == null) {
            return this;
        }

        net.minecraft.entity.Entity baseVehicle = this.ridingEntity;
        while (baseVehicle.ridingEntity != null) {
            baseVehicle = baseVehicle.ridingEntity;
        }
        return (Entity) baseVehicle;
    }

    @Override
    public boolean setPassenger(@Nullable Entity entity) {
        net.minecraft.entity.Entity passenger = (net.minecraft.entity.Entity) entity;
        if (this.riddenByEntity == null) { // no existing passenger
            if (passenger == null) {
                return true;
            }

            Entity thisEntity = this;
            passenger.mountEntity((net.minecraft.entity.Entity) thisEntity);
        } else { // passenger already exists
            this.riddenByEntity.mountEntity(null); // eject current passenger

            if (passenger != null) {
                Entity thisEntity = this;
                passenger.mountEntity((net.minecraft.entity.Entity) thisEntity);
            }
        }

        return true;
    }

    @Override
    public boolean setVehicle(@Nullable Entity entity) {
        mountEntity((net.minecraft.entity.Entity) entity);
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
        if (this.origWidth == 0 || this.origHeight == 0) {
            this.origWidth = this.width;
            this.origHeight = this.height;
        }
        double scaleW = this.width / this.origWidth;
        double scaleH = this.height / this.origHeight;
        return (float) (scaleH + scaleW) / 2;
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
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerManager().removePlayer((EntityPlayerMP) entity);
            mcServer.getConfigurationManager().playerEntityList.remove(entity);
        } else {
            fromWorld.getEntityTracker().untrackEntity(entity);
        }

        entity.worldObj.removePlayerEntityDangerously(entity);
        entity.dimension = targetDim;
        entity.setWorld(toWorld);
        entity.isDead = false;
        entity.setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
        toWorld.theChunkProviderServer.loadChunk((int) entity.posX >> 4, (int) entity.posZ >> 4);
        while (!toWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D) {
            entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
        }

        if (entity instanceof EntityPlayer) {
            EntityPlayerMP entityplayermp1 = (EntityPlayerMP) entity;
            entityplayermp1.isDead = false;
            entityplayermp1.playerNetServerHandler.sendPacket(
                    new S07PacketRespawn(targetDim, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                            entityplayermp1.theItemInWorldManager.getGameType()));
            entityplayermp1.playerNetServerHandler.sendPacket(
                    new S1FPacketSetExperience(entityplayermp1.experience, entityplayermp1.experienceTotal, entityplayermp1.experienceLevel));
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
        return this.entityType;
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
