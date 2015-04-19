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
package org.spongepowered.mod.mixin.core.entity;

import static org.spongepowered.api.data.DataQuery.of;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook.EnumFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.manipulators.entities.NameData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.data.manipulators.SpongeNameData;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.registry.SpongeGameRegistry;
import org.spongepowered.mod.util.SpongeHooks;
import org.spongepowered.mod.world.SpongeDimensionType;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.entity.Entity.class)
public abstract class MixinEntity implements Entity, IMixinEntity {

    // @formatter:off
    private EntityType entityType = ((SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry()).entityClassToTypeMappings.get(this.getClass());
    private boolean teleporting;
    private net.minecraft.entity.Entity teleportVehicle;
    private float origWidth;
    private float origHeight;

    @Shadow private UUID entityUniqueID;
    @Shadow public net.minecraft.world.World worldObj;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public float width;
    @Shadow public float height;
    @Shadow public float fallDistance;
    @Shadow public boolean isDead;
    @Shadow public boolean onGround;
    @Shadow public boolean inWater;
    @Shadow public int hurtResistantTime;
    @Shadow public int fireResistance;
    @Shadow public int fire;
    @Shadow public net.minecraft.entity.Entity riddenByEntity;
    @Shadow public net.minecraft.entity.Entity ridingEntity;
    @Shadow protected DataWatcher dataWatcher;
    @Shadow protected Random rand;

    @Shadow public abstract void setPosition(double x, double y, double z);
    @Shadow public abstract void mountEntity(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void setDead();
    @Shadow public abstract void setFlag(int flag, boolean data);
    @Shadow public abstract boolean getFlag(int flag);
    @Shadow public abstract int getAir();
    @Shadow public abstract void setAir(int air);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract String getCustomNameTag();
    @Shadow public abstract void setCustomNameTag(String name);
    @Shadow public abstract UUID getUniqueID();
    @Shadow protected abstract boolean getAlwaysRenderNameTag();
    @Shadow protected abstract void setAlwaysRenderNameTag(boolean visible);
    @Shadow(prefix = "shadow$")
    protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow(remap = false)
    public abstract NBTTagCompound getEntityData();

    // @formatter:on

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
    public void setLocation(Location location) {
        setLocation(location, true);
    }

    @Override
    public boolean setLocationSafely(Location location) {
        return setLocation(location, false);
    }

    @Override
    public void setLocationAndRotation(Location location, Vector3d rotation) {
        setLocation(location);
        setRotation(rotation);
    }

    @Override
    public boolean setLocationAndRotationSafely(Location location, Vector3d rotation) {
        boolean relocated = setLocation(location, false);
        setRotation(rotation);
        return relocated;
    }

    @Override
    public boolean setLocationAndRotationSafely(Location location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        return setLocationAndRotation(location, rotation, relativePositions, true);
    }

    public boolean setLocation(Location location, boolean forced) {
        if (isRemoved()) {
            return false;
        }

        Entity spongeEntity = this;
        net.minecraft.entity.Entity thisEntity = (net.minecraft.entity.Entity) spongeEntity;

        if (!forced) {
            // Validate
            TeleportHelper teleportHelper = SpongeMod.instance.getGame().getTeleportHelper();
            Optional<Location> safeLocation = teleportHelper.getSafeLocation(location);
            if (!safeLocation.isPresent()) {
                return false;
            } else {
                location = safeLocation.get();
            }
        }
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
        if (location.getExtent() instanceof World && ((World) location.getExtent()).getUniqueId() != ((World) this.worldObj).getUniqueId()) {
            nmsWorld = (net.minecraft.world.World) location.getExtent();
            if (thisEntity instanceof EntityPlayerMP) {
                // Close open containers
                if (((EntityPlayerMP) thisEntity).openContainer != ((EntityPlayerMP) thisEntity).inventoryContainer) {
                    ((EntityPlayerMP) thisEntity).closeContainer();
                }
            }
            teleportEntity(thisEntity, location, thisEntity.dimension, nmsWorld.provider.getDimensionId(), forced);
        } else {
            if (thisEntity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) thisEntity).playerNetServerHandler
                        .setPlayerLocation(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ(),
                                thisEntity.rotationYaw, thisEntity.rotationPitch);
            } else {
                setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
            }
        }

        // re-attach passengers
        net.minecraft.entity.Entity lastPassenger = thisEntity;
        while (!passengers.isEmpty()) {
            net.minecraft.entity.Entity passengerEntity = passengers.remove();
            if (nmsWorld != null) {
                teleportEntity(passengerEntity, location, passengerEntity.dimension, nmsWorld.provider.getDimensionId(), true);
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

    @Override
    public void setLocationAndRotation(Location location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        setLocationAndRotation(location, rotation, relativePositions, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setLocationAndRotation(Location location, Vector3d rotation, EnumSet<RelativePositions> relativePositions, boolean forced) {
        boolean relocated = true;

        if (relativePositions.isEmpty()) {
            //This is just a normal teleport that happens to set both.
            relocated = setLocation(location, forced);
            setRotation(rotation);
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
                        .getY(), location.getPosition().getZ(), (float) rotation.getX(), (float) rotation.getY(), relativeFlags);
            } else {
                Location resultant = getLocation();
                Vector3d resultantRotation = getRotation();

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
                relocated = setLocation(resultant, forced);
                setRotation(resultantRotation);
            }
        }

        return relocated;
    }

    @Override
    public boolean transferToWorld(String worldName, Vector3d position) {
        Optional<WorldProperties> props = SpongeMod.instance.getSpongeRegistry().getWorldProperties(worldName);
        if (props.isPresent()) {
            if (props.get().isEnabled()) {
                Optional<World> world = SpongeMod.instance.getGame().getServer().loadWorld(worldName);
                if (world.isPresent()) {
                    Location location = new Location(world.get(), position);
                    return setLocationSafely(location);
                }
            }
        }
        return false;
    }

    @Override
    public boolean transferToWorld(UUID uuid, Vector3d position) {
        return transferToWorld(SpongeMod.instance.getSpongeRegistry().getWorldFolder(uuid), position);
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationYaw, this.rotationPitch, 0);
    }

    @Override
    public void setRotation(Vector3d rotation) {
        shadow$setRotation((float) rotation.getX(), (float) rotation.getY());
    }

    public Vector3d getVelocity() {
        return new Vector3d(this.motionX, this.motionY, this.motionZ);
    }

    public void setVelocity(Vector3d velocity) {
        this.motionX = velocity.getX();
        this.motionY = velocity.getY();
        this.motionZ = velocity.getZ();
    }

    public Optional<Entity> getPassenger() {
        return Optional.fromNullable((Entity) this.riddenByEntity);
    }

    public Optional<Entity> getVehicle() {
        return Optional.fromNullable((Entity) this.ridingEntity);
    }

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

    public boolean setVehicle(@Nullable Entity entity) {
        mountEntity((net.minecraft.entity.Entity) entity);
        return true;
    }

    public float getBase() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

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

    public int getFireTicks() {
        return this.fire;
    }

    public void setFireTicks(int ticks) {
        this.fire = ticks;
    }

    public int getFireDelay() {
        return this.fireResistance;
    }

    // for sponge internal use only
    @SuppressWarnings("unchecked")
    private boolean teleportEntity(net.minecraft.entity.Entity entity, Location location, int currentDim, int targetDim, boolean forced) {
        MinecraftServer mcServer = MinecraftServer.getServer();
        final WorldServer fromWorld = mcServer.worldServerForDimension(currentDim);
        final WorldServer toWorld = mcServer.worldServerForDimension(targetDim);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerManager().removePlayer((EntityPlayerMP) entity);
            mcServer.getConfigurationManager().playerEntityList.remove(entity);
        } else {
            fromWorld.getEntityTracker().untrackEntity(entity);
        }

        entity.worldObj.removePlayerEntityDangerously(entity);
        entity.dimension = targetDim;
        entity.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);
        if (forced) {
            while (!toWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D) {
                entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
            }
        }

        toWorld.theChunkProviderServer.loadChunk((int) entity.posX >> 4, (int) entity.posZ >> 4);

        if (entity instanceof EntityPlayer) {
            EntityPlayerMP entityplayermp1 = (EntityPlayerMP) entity;
            boolean fmlClient = entityplayermp1.playerNetServerHandler.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
            // Support vanilla clients teleporting to custom dimensions
            if (!fmlClient) {
                if (((Dimension) toWorld.provider).getType().equals(DimensionTypes.NETHER)) {
                    targetDim = -1;
                } else if (((Dimension) toWorld.provider).getType().equals(DimensionTypes.END)) {
                    targetDim = 1;
                } else {
                    targetDim = 0;
                }
            } else { // send DimensionRegister message
                FMLEmbeddedChannel serverChannel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
                serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
                serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entityplayermp1);
                serverChannel.writeOutbound(new ForgeMessage.DimensionRegisterMessage(toWorld.provider.getDimensionId(),
                        ((SpongeDimensionType) ((Dimension) toWorld.provider).getType()).getDimensionTypeId()));
            }

            entityplayermp1.playerNetServerHandler.sendPacket(
                    new S07PacketRespawn(targetDim, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                            entityplayermp1.theItemInWorldManager.getGameType()));
            entity.setWorld(toWorld);
            entity.isDead = false;
            entityplayermp1.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ,
                    entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
            entityplayermp1.setSneaking(false);
            mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(entityplayermp1, toWorld);
            toWorld.getPlayerManager().addPlayer(entityplayermp1);
            toWorld.spawnEntityInWorld(entityplayermp1);
            mcServer.getConfigurationManager().playerEntityList.add(entityplayermp1);
            entityplayermp1.theItemInWorldManager.setWorld(toWorld);
            entityplayermp1.addSelfToInternalCraftingInventory();
            entityplayermp1.setHealth(entityplayermp1.getHealth());
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(entityplayermp1, currentDim, targetDim);
        } else {
            toWorld.spawnEntityInWorld(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        return true;
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
    public EntityType getType() {
        return this.entityType;
    }

    @Override
    public UUID getUniqueId() {
        return this.entityUniqueID;
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #writeToNbt}.
     *
     * <p> This makes it easier for other entity mixins to override writeToNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write
     *        to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.writeToNbt(this.getSpongeData());
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #readFromNbt}.
     *
     * <p> This makes it easier for other entity mixins to override readFromNbt
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read
     *        from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.readFromNbt(this.getSpongeData());
    }

    /**
     * Gets the SpongeData NBT tag, used for additional data not stored in the
     * vanilla tag.
     *
     * <p> Modifying this tag will affect the data stored. </p>
     *
     * @return The data tag
     */
    public final NBTTagCompound getSpongeData() {
        NBTTagCompound data = this.getEntityData();
        if (!data.hasKey("SpongeData", Constants.NBT.TAG_COMPOUND)) {
            data.setTag("SpongeData", new NBTTagCompound());
        }
        return data.getCompoundTag("SpongeData");
    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    public void readFromNbt(NBTTagCompound compound) {
    }

    /**
     * Write extra data (SpongeData) to the entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    public void writeToNbt(NBTTagCompound compound) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> Optional<T> getData(Class<T> dataClass) {
        if (NameData.class.isAssignableFrom((Class) dataClass)) {
            NameData nameData = new SpongeNameData();
            return (Optional<T>) (Optional) nameData.fill(this);
        }
        return Optional.absent();
    }

    @Override
    public <T extends DataManipulator<T>> Optional<T> getOrCreate(Class<T> manipulatorClass) {
        return null;
    }

    @Override
    public <T extends DataManipulator<T>> boolean remove(Class<T> manipulatorClass) {
        return false;
    }

    @Override
    public <T extends DataManipulator<T>> boolean isCompatible(Class<T> manipulatorClass) {
        return false;
    }

    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData) {

        return null;
    }

    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData, DataPriority priority) {
        return null;
    }

    @Override
    public Collection<? extends DataManipulator<?>> getManipulators() {
        return getUnsafeManipulators().build();
    }

    protected ImmutableList.Builder<DataManipulator<?>> getUnsafeManipulators() {

        return ImmutableList.builder();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return null;
    }

    @Override
    public Collection<? extends Property<?, ?>> getProperties() {
        return null;
    }

    @Override
    public boolean validateRawData(DataContainer container) {
        return false;
    }

    @Override
    public void setRawData(DataContainer container) throws InvalidDataException {

    }
    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("World"), this.getWorld().getUniqueId().toString());
        // TODO do stuff with more container information
        return container;
    }

    protected <T extends DataManipulator<T>> DataTransactionResult preTransaction(T manipulatorData, T oldData,
                                                                                  DataHolder mixinEntityTNTPrimed) {
        // TODO stuff
        return null;
    }
}
