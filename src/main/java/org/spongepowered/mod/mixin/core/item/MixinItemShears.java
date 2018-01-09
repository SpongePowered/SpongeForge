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
package org.spongepowered.mod.mixin.core.item;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IShearable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mixin(value = ItemShears.class, remap = false)
public abstract class MixinItemShears extends Item {


    /**
     * @author gabizou - June 21st, 2016
     * @reason Rewrites the forge handling of this to properly handle
     * when sheared drops are captured by whatever current phase the
     * {@link PhaseTracker} is in.
     *
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    @Overwrite
    @Override
    public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity,
            EnumHand hand) {
        if (entity.world.isRemote) {
            return false;
        }
        if (entity instanceof IShearable) {
            IShearable target = (IShearable) entity;
            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            if (target.isShearable(itemstack, entity.world, pos)) {
                List<ItemStack> drops = target.onSheared(itemstack, entity.world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));
                // Sponge Start - Handle drops according to the current phase
                final PhaseTracker phaseTracker = PhaseTracker.getInstance();
                final PhaseData currentData = phaseTracker.getCurrentPhaseData();
                final IPhaseState<?> currentState = currentData.state;
                final PhaseContext<?> phaseContext = currentData.context;
                final Random random = EntityUtil.fromNative(entity).getRandom();
                final IMixinEntity mixinEntity = EntityUtil.toMixin(entity);
                final double posX = entity.posX;
                final double posY = entity.posY + 1.0F;
                final double posZ = entity.posZ;
                final Vector3d position = new Vector3d(posX, posY, posZ);
                // Now the real fun begins.
                for (ItemStack drop : drops) {
                    final ItemStack item;

                    if (!drop.isEmpty()) {
                        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                            // FIRST we want to throw the DropItemEvent.PRE
                            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(drop);
                            final List<ItemStackSnapshot> original = new ArrayList<>();
                            original.add(snapshot);
                            Sponge.getCauseStackManager().pushCause(entity);
                            final DropItemEvent.Pre
                                dropEvent =
                                SpongeEventFactory.createDropItemEventPre(Sponge.getCauseStackManager().getCurrentCause(),
                                    ImmutableList.of(snapshot), original);
                            if (dropEvent.isCancelled()) {
                                continue;
                            }

                            // SECOND throw the ConstructEntityEvent
                            Transform<World> suggested = new Transform<>(mixinEntity.getWorld(), position);
                            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                            ConstructEntityEvent.Pre event = SpongeEventFactory
                                .createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), EntityTypes.ITEM, suggested);
                            SpongeImpl.postEvent(event);
                            item = event.isCancelled() ? null : ItemStackUtil.fromSnapshotToNative(dropEvent.getDroppedItems().get(0));
                        }
                    } else {
                        continue;
                    }
                    if (item == null) {
                        continue;
                    }
                    if (!item.isEmpty()) {
                        if (!currentState.ignoresItemPreMerging() && SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops()) {
                            if (currentState.tracksEntitySpecificDrops()) {
                                final Multimap<UUID, ItemDropData> multimap = phaseContext.getCapturedEntityDropSupplier().get();
                                final Collection<ItemDropData> itemStacks = multimap.get(entity.getUniqueID());
                                SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(item)
                                        .motion(new Vector3d((random.nextFloat() - random.nextFloat()) * 0.1F, random.nextFloat() * 0.05F, (random.nextFloat() - random.nextFloat()) * 0.1F))
                                        .position(new Vector3d(posX, posY, posZ))
                                        .build());
                                continue;
                            }
                            final List<ItemDropData> itemStacks = phaseContext.getCapturedItemStackSupplier().get();
                            SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(item)
                                    .position(new Vector3d(posX, posY, posZ))
                                    .motion(new Vector3d((random.nextFloat() - random.nextFloat()) * 0.1F, random.nextFloat() * 0.05F, (random.nextFloat() - random.nextFloat()) * 0.1F))
                                    .build());
                            continue;
                        }
                        EntityItem entityitem = new EntityItem(entity.world, posX, posY, posZ, item);
                        entityitem.setDefaultPickupDelay();
                        entityitem.motionY += random.nextFloat() * 0.05F;
                        entityitem.motionX += (random.nextFloat() - random.nextFloat()) * 0.1F;
                        entityitem.motionZ += (random.nextFloat() - random.nextFloat()) * 0.1F;

                        // FIFTH - Capture the entity maybe?
                        if (currentState.doesCaptureEntityDrops()) {
                            if (currentState.tracksEntitySpecificDrops()) {
                                // We are capturing per entity drop
                                phaseContext.getCapturedEntityItemDropSupplier().get().put(entity.getUniqueID(), entityitem);
                            } else {
                                // We are adding to a general list - usually for EntityPhase.State.DEATH
                                phaseContext.getCapturedItemsSupplier().get().add(entityitem);
                            }
                            // Return the item, even if it wasn't spawned in the world.
                            continue;
                        }
                        // FINALLY - Spawn the entity in the world if all else didn't fail
                        entity.world.spawnEntity(entityitem);

                    }
                }

                // Sponge End
                itemstack.damageItem(1, entity);
            }
            return true;
        }
        return false;
    }

}
