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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.player.ISpongeUser;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.mixin.core.entity.living.MixinEntityLivingBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = EntityPlayer.class, priority = 1001)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements ISpongeUser, IMixinEntityPlayer {

    @Shadow public InventoryPlayer inventory;
    @Shadow public BlockPos bedLocation;
    @Shadow protected boolean sleeping;
    @Shadow private int sleepTimer;
    @Shadow private BlockPos spawnPos;
    @Shadow private boolean spawnForced;
    @Shadow(remap = false) protected HashMap<Integer, BlockPos> spawnChunkMap;
    @Shadow(remap = false) protected HashMap<Integer, Boolean> spawnForcedMap;

    @Shadow public abstract EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem);
    @Shadow public abstract void setSpawnPoint(BlockPos pos, boolean force);
    @Shadow(remap = false)
    public abstract void setSpawnChunk(BlockPos pos, boolean forced, int dimension);

    /**
     * @author blood
     * @reason Reroutes to our damage hook
     *
     * @param damageSource The damage source
     * @param damage The damage
     */
    @Overwrite
    protected void damageEntity(DamageSource damageSource, float damage) {
        this.damageEntityHook(damageSource, damage);
    }

    @Inject(method = "trySleep", at = @At(value = "RETURN", ordinal = 0))
    private void onSleepEvent(BlockPos bedLocation, CallbackInfoReturnable<EntityPlayer.SleepResult> cir) {
        if (cir.getReturnValue() == EntityPlayer.SleepResult.OK) {
            // A cut-down version of trySleep handling for OK status
            if (((EntityPlayer) (Object) this).isRiding()) {
                ((EntityPlayer) (Object) this).dismountRidingEntity();
            }
            this.setSize(0.2F, 0.2F);
            ((EntityPlayer) (Object) this).setPosition(bedLocation.getX() + 0.5F, bedLocation.getY() + 0.6875F,
                    bedLocation.getZ() + 0.5F);
            this.sleeping = true;
            this.sleepTimer = 0;
            this.bedLocation = bedLocation;
            ((EntityPlayer) (Object) this).motionX = ((EntityPlayer) (Object) this).motionZ = ((EntityPlayer) (Object) this).motionY = 0.0D;
            if (!this.world.isRemote) {
                this.world.updateAllPlayersSleepingFlag();
            }
        }
    }

    @Override
    public void setOverworldSpawnPoint(@Nullable BlockPos pos) {
        this.spawnPos = pos;
    }

    /**
     * @author JBYoshi - November 23rd, 2015
     * @reason implement SpongeAPI events.
     *
     * @param immediately Whether to be woken up immediately
     * @param updateWorldFlag Whether to update the world
     * @param setSpawn Whether the player has successfully set up spawn
     */
    @Overwrite
    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        IBlockState iblockstate = this.world.getBlockState(this.bedLocation);

        Transform<World> newLocation = null;
        if (this.bedLocation != null && iblockstate.getBlock().isBed(iblockstate, this.world, this.bedLocation, (Entity) (Object) this)) {
            iblockstate.getBlock().setBedOccupied(this.world, this.bedLocation, ((EntityPlayer) (Object) this), false);
            BlockPos blockpos = iblockstate.getBlock().getBedSpawnPosition(iblockstate, this.world, this.bedLocation, ((EntityPlayer) (Object) this));

            if (blockpos == null) {
                blockpos = ((EntityPlayer) (Object) this).bedLocation.up();
            }

            newLocation = this.getTransform().setPosition(new Vector3d(blockpos.getX() + 0.5F, blockpos.getY() + 0.1F, blockpos.getZ() + 0.5F));
        }

        SleepingEvent.Post post = null;
        if (!((IMixinWorld) this.world).isFake()) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                post = SpongeEventFactory.createSleepingEventPost(frame.getCurrentCause(),
                    this.getWorld().createSnapshot(VecHelper.toVector3i(this.bedLocation)), Optional.ofNullable(newLocation), this, setSpawn);
                Sponge.getEventManager().post(post);
                if (post.isCancelled()) {
                    return;
                }

                ForgeEventFactory.onPlayerWakeup(((EntityPlayer) (Object) this), immediately, updateWorldFlag, setSpawn);
                this.setSize(0.6F, 1.8F);
                if (post.getSpawnTransform().isPresent()) {
                    this.setLocationAndAngles(post.getSpawnTransform().get());
                }
            }
        } else {
            ForgeEventFactory.onPlayerWakeup(((EntityPlayer) (Object) this), immediately, updateWorldFlag, setSpawn);
            this.setSize(0.6F, 1.8F);
        }

        this.sleeping = false;

        if (!this.world.isRemote && updateWorldFlag) {
            this.world.updateAllPlayersSleepingFlag();
        }

        this.sleepTimer = immediately ? 0 : 100;

        if (setSpawn) {
            this.setSpawnPoint(this.bedLocation, false);
        }
        if (post != null) {
            Sponge.getGame().getEventManager().post(SpongeEventFactory.createSleepingEventFinish(post.getCause(),
                    this.getWorld().createSnapshot(VecHelper.toVector3i(this.bedLocation)), this));
        }
    }

    // TODO 1.9 Update - Zidane's deal with the next two methods.
    @Override
    public Map<UUID, RespawnLocation> getBedlocations() {
        Map<UUID, RespawnLocation> locations = Maps.newHashMap();

        return locations;
    }

    @Override
    public boolean setBedLocations(Map<UUID, RespawnLocation> locations) {
        // Clear all existing values
        this.spawnChunkMap.clear();
        this.spawnForcedMap.clear();
        setSpawnChunk(null, false, 0);
        // Add replacement values
        for (Entry<UUID, RespawnLocation> entry : locations.entrySet()) {
            // TODO ???
        }
        return true;
    }

    @Override
    public ImmutableMap<UUID, RespawnLocation> removeAllBeds() {
        ImmutableMap<UUID, RespawnLocation> locations = ImmutableMap.copyOf(getBedlocations());
        this.spawnChunkMap.clear();
        this.spawnForcedMap.clear();
        setSpawnChunk(null, false, 0);
        return locations;
    }
}
