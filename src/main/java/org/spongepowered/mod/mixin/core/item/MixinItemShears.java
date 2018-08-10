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
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    @SuppressWarnings({"unchecked", "rawtypes"})
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
                final PhaseData currentData = PhaseTracker.getInstance().getCurrentPhaseData();
                final IPhaseState<?> currentState = currentData.state;
                final PhaseContext<?> phaseContext = currentData.context;
                final Random random = EntityUtil.fromNative(entity).getRandom();
                final double posX = entity.posX;
                final double posY = entity.posY + 1.0F;
                final double posZ = entity.posZ;
                // Now the real fun begins.
                for (ItemStack drop : drops) {
                    final ItemStack item;
                    if (drop.isEmpty()) {
                        continue;
                    }
                    final List<ItemStackSnapshot> original = new ArrayList<>();
                    final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(drop);
                    original.add(snapshot);
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        item = EntityUtil.throwDropItemAndConstructEvent(EntityUtil.toMixin(entity), posX, posY, posZ, snapshot, original, frame);
                    }

                    if (item == null || item.isEmpty()) {
                        continue;
                    }
                    // Item pre-merging should go here. It is disabled for now due to development.

                    // Continue with creating the entity item.
                    EntityItem entityitem = new EntityItem(entity.world, posX, posY, posZ, item);
                    entityitem.setDefaultPickupDelay();
                    entityitem.motionY += random.nextFloat() * 0.05F;
                    entityitem.motionX += (random.nextFloat() - random.nextFloat()) * 0.1F;
                    entityitem.motionZ += (random.nextFloat() - random.nextFloat()) * 0.1F;

                    // FIFTH - Capture the entity maybe?
                    // this sould be passed into the state, instead of cluttering the code in this area.
                    if (((IPhaseState) currentState).spawnItemOrCapture(phaseContext, entity, entityitem)) {
                        continue;
                    }

                    // FINALLY - Spawn the entity in the world if all else didn't fail
                    entity.world.spawnEntity(entityitem);
                }

                // Sponge End
                itemstack.damageItem(1, entity);
            }
            return true;
        }
        return false;
    }

}
