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
package org.spongepowered.mod.mixin.core.event.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.mod.mixin.core.event.block.MixinEventBlock;
import org.spongepowered.mod.mixin.core.event.entity.MixinEventEntity;
import org.spongepowered.mod.util.StaticMixinHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.item.ItemEvent.class, remap = false)
public abstract class MixinEventDropItem extends MixinEventEntity implements DropItemEvent {

    protected EntitySnapshot entitySnapshot;
    protected ImmutableList<EntitySnapshot> entitySnapshots;
    protected List<Item> entities = new ArrayList<Item>();
    @Shadow public EntityItem entityItem;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityItem itemEntity, CallbackInfo ci) {
        this.entitySnapshot = ((Entity) itemEntity).createSnapshot();
        this.entities.add((Item) itemEntity);
        ImmutableList.of(this.entitySnapshot);
    }

    @Mixin(value = ItemTossEvent.class, remap = false)
    static abstract class Toss extends MixinEventDropItem implements DropItemEvent.Toss {

        @Shadow public EntityPlayer player;

        @Override
        public List<Item> getEntities() {
            return this.entities;
        }

        @Override
        public List<Item> filterEntityLocations(Predicate<Location<World>> predicate) {
            Iterator<Item> iterator = this.entities.iterator();
            while (iterator.hasNext()) {
                if (!predicate.test(((Item) iterator.next()).getLocation())) {
                    iterator.remove();
                }
            }
            return this.entities;
        }

        @Override
        public List<Item> filterEntities(Predicate<Entity> predicate) {
            Iterator<Item> iterator = this.entities.iterator();
            while (iterator.hasNext()) {
                if (!predicate.test((Item) iterator.next())) {
                    iterator.remove();
                }
            }
            return this.entities;
        }

        @Override
        public List<EntitySnapshot> getEntitySnapshots() {
            return this.entitySnapshots;
        }

        @Override
        public World getTargetWorld() {
            return (World) this.player.worldObj;
        }

        @Override
        public Cause getCause() {
            return Cause.of(this.player);
        }
    }

    @Mixin(value = HarvestDropsEvent.class, remap = false)
    static abstract class Harvest extends MixinEventBlock implements DropItemEvent.Pre, DropItemEvent.Harvest {

        private ImmutableList<ItemStackSnapshot> originalDrops;
        private List<ItemStackSnapshot> itemDrops = new ArrayList<ItemStackSnapshot>();
        private float originalDropChance;
        private BlockSnapshot blockSnapshot;
        private Entity entityCause;
        private BlockSnapshot blockCause;
        private World world;

        @Shadow public int fortuneLevel;
        @Shadow public List<net.minecraft.item.ItemStack> drops;
        @Shadow public boolean isSilkTouching;
        @Shadow public float dropChance;
        @Shadow public EntityPlayer harvester;

        @Inject(method = "<init>", at = @At("RETURN"))
        public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, int fortuneLevel, float dropChance,
                List<net.minecraft.item.ItemStack> drops, EntityPlayer harvester, boolean isSilkTouching,
                CallbackInfo ci) {

            ImmutableList.Builder<ItemStackSnapshot> builder = new ImmutableList.Builder<ItemStackSnapshot>();
            for (net.minecraft.item.ItemStack itemStack : drops) {
                builder.add(((ItemStack)itemStack).createSnapshot());
                itemDrops.add(((ItemStack)itemStack).createSnapshot());
            }

            this.originalDrops = builder.build();
            this.originalDropChance = dropChance;
            this.blockSnapshot = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            this.world = (World) world;
            Optional<Entity> currentTickEntity = ((IMixinWorld)world).getCurrentTickEntity();
            Optional<BlockSnapshot> currentTickBlock = ((IMixinWorld)world).getCurrentTickBlock();
            // TODO - remove below and replace Forge's event with ours
            // Check for player
            if (this.harvester != null) {
                this.entityCause = (Entity) harvester;
            } else if (currentTickEntity.isPresent()) {
                this.entityCause = currentTickEntity.get();
            } else if (StaticMixinHelper.processingPlayer != null) {
                this.entityCause = (Entity) StaticMixinHelper.processingPlayer;
            }
            // Check for block
            if (currentTickBlock.isPresent()) {
                this.blockCause = currentTickBlock.get();
            }
        }

        @Override
        public ImmutableList<ItemStackSnapshot> getOriginalDroppedItems() {
            return this.originalDrops;
        }

        @Override
        public List<ItemStackSnapshot> getDroppedItems() {
            return this.itemDrops;
        }

        @Override
        public float getDropChance() {
            return this.dropChance;
        }

        @Override
        public void setDropChance(float chance) {
            this.dropChance = chance;
        }

        @Override
        public Cause getCause() {
            if (this.entityCause != null && this.blockCause != null) {
                return Cause.of(this.entityCause, this.blockCause);
            } else if (this.entityCause != null || this.blockCause != null) {
                return Cause.of(this.entityCause != null ? this.entityCause : this.blockCause, this.world);
            } else {
                return Cause.of(this.world);
            }
        }

        @Override
        public float getOriginalDropChance() {
            return this.originalDropChance;
        }

        @Override
        public BlockSnapshot getTargetBlock() {
            return this.blockSnapshot;
        }

        @Override
        public void syncDataToSponge(net.minecraftforge.fml.common.eventhandler.Event forgeSyncEvent) {
            super.syncDataToSponge(forgeSyncEvent);

            HarvestDropsEvent forgeEvent = (HarvestDropsEvent) forgeSyncEvent;
            getDroppedItems().clear();
            for (net.minecraft.item.ItemStack itemstack : forgeEvent.drops) {
                getDroppedItems().add(((ItemStack) itemstack).createSnapshot());
            }
        }

        @Override
        public void syncDataToForge(org.spongepowered.api.event.Event spongeEvent) {
            super.syncDataToForge(spongeEvent);

            DropItemEvent.Harvest event = (DropItemEvent.Harvest) spongeEvent;
            this.drops.clear();
            for (ItemStackSnapshot itemSnapshot : event.getDroppedItems()) {
                this.drops.add((net.minecraft.item.ItemStack) itemSnapshot.createStack());
            }
        }
    }
}
