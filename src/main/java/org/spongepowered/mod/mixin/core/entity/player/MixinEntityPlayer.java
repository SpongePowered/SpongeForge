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
package org.spongepowered.mod.mixin.core.entity.player;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.player.ISpongeUser;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.mixin.core.entity.living.MixinEntityLivingBase;
import org.spongepowered.common.registry.type.world.WorldPropertyRegistryModule;
import org.spongepowered.common.util.VecHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements ISpongeUser {

    @Shadow public InventoryPlayer inventory;
    @Shadow public BlockPos playerLocation;
    @Shadow protected boolean sleeping;
    @Shadow private int sleepTimer;
    @Shadow private BlockPos spawnChunk;
    @Shadow(remap = false) private HashMap<Integer, BlockPos> spawnChunkMap;
    @Shadow(remap = false) private HashMap<Integer, Boolean> spawnForcedMap;

    @Shadow public abstract EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem);
    @Shadow public abstract void setSpawnPoint(BlockPos pos, boolean force);
    @Shadow(remap = false)
    public abstract void setSpawnChunk(BlockPos pos, boolean forced, int dimension);

    @Overwrite
    @Override
    protected void damageEntity(DamageSource damageSource, float damage) {
        ((IMixinEntityLivingBase) this).damageEntityHook(damageSource, damage);
    }

    // Restore methods to original as we handle PlayerTossEvent in DropItemEvent
    @Overwrite
    public EntityItem dropOneItem(boolean dropAll) {
        return this.dropItem(this.inventory.decrStackSize(this.inventory.currentItem, dropAll && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1), false, true);
    }

    @Overwrite
    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean unused) {
        return this.dropItem(itemStackIn, false, false);
    }

    @Overwrite
    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        IBlockState iblockstate = this.worldObj.getBlockState(this.playerLocation);

        Transform<World> newLocation = null;
        if (this.playerLocation != null && iblockstate.getBlock().isBed(this.worldObj, this.playerLocation, (EntityPlayer) (Object) this)) {
            iblockstate.getBlock().setBedOccupied(this.worldObj, this.playerLocation, (EntityPlayer) (Object) this, false);
            BlockPos blockpos = iblockstate.getBlock().getBedSpawnPosition(this.worldObj, this.playerLocation, (EntityPlayer) (Object) this);

            if (blockpos == null) {
                blockpos = this.playerLocation.up();
            }

            newLocation = this.getTransform().setPosition(new Vector3d(blockpos.getX() + 0.5F, blockpos.getY() + 0.1F, blockpos.getZ() + 0.5F));
        }

        SleepingEvent.Post post = SpongeEventFactory.createSleepingEventPost(Sponge.getGame(), Cause.of(NamedCause.source(this)),
            this.getWorld().createSnapshot(VecHelper.toVector(this.playerLocation)), Optional.ofNullable(newLocation), this, setSpawn);
        Sponge.getEventManager().post(post);
        if (post.isCancelled()) {
            return;
        }

        net.minecraftforge.event.ForgeEventFactory.onPlayerWakeup((EntityPlayer) (Object) this, immediately, updateWorldFlag, setSpawn);
        this.setSize(0.6F, 1.8F);
        if (post.getSpawnTransform().isPresent()) {
            this.setTransform(post.getSpawnTransform().get());
        }

        this.sleeping = false;

        if (!this.worldObj.isRemote && updateWorldFlag)
        {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        this.sleepTimer = immediately ? 0 : 100;

        if (setSpawn) {
            this.setSpawnPoint(post.getSpawnTransform().isPresent() ? VecHelper.toBlockPos(post.getSpawnTransform().get().getPosition()) : this.playerLocation, false);
        }

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createSleepingEventFinish(Sponge.getGame(), post.getCause(), this.getWorld().createSnapshot(VecHelper.toVector(this.playerLocation)), this));
    }

    @Override
    public Map<UUID, Vector3d> getBedlocations() {
        Map<UUID, Vector3d> locations = Maps.newHashMap();
        if (this.spawnChunk != null) {
            locations.put(WorldPropertyRegistryModule.dimIdToUuid(0), VecHelper.toVector3d(this.spawnChunk));
        }
        for (Entry<Integer, BlockPos> entry : this.spawnChunkMap.entrySet()) {
            UUID uuid = WorldPropertyRegistryModule.dimIdToUuid(entry.getKey());
            if (uuid != null) {
                locations.put(uuid, VecHelper.toVector3d(entry.getValue()));
            }
        }
        return locations;
    }

    @Override
    public boolean setBedLocations(Map<UUID, Vector3d> locations) {
        // Clear all existing values
        this.spawnChunkMap.clear();
        this.spawnForcedMap.clear();
        setSpawnChunk(null, false, 0);
        // Add replacement values
        for (Entry<UUID, Vector3d> entry : locations.entrySet()) {
            int dim = WorldPropertyRegistryModule.uuidToDimId(entry.getKey());
            if (dim != Integer.MIN_VALUE) {
                // Note: No way to set 'force' parameter
                setSpawnChunk(VecHelper.toBlockPos(entry.getValue()), false, dim);
            }
        }
        return true;
    }

    @Override
    public ImmutableMap<UUID, Vector3d> removeAllBeds() {
        ImmutableMap<UUID, Vector3d> locations = ImmutableMap.copyOf(getBedlocations());
        this.spawnChunkMap.clear();
        this.spawnForcedMap.clear();
        setSpawnChunk(null, false, 0);
        return locations;
    }
}
