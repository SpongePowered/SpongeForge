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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinEntityFishHook;

@NonnullByDefault
@Mixin(ItemFishingRod.class)
public abstract class MixinItemFishingRod extends Item implements IMixinEntityFishHook {

    @Override
    @Overwrite
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (player.fishEntity != null) {
            // Damage and animation logic is moved inside handleHookRetraction, as the event
            // is fired there
            ((IMixinEntityFishHook) player.fishEntity).setFishingRodItemStack(itemStack);
            player.fishEntity.handleHookRetraction();
        } else {
            EntityFishHook fishHook = new EntityFishHook(world, player);
            if (!SpongeMod.instance.getGame().getEventManager()
                    .post(SpongeEventFactory.createPlayerCastFishingLineEvent(SpongeMod.instance.getGame(), (Player) player, (FishHook) fishHook))) {
                world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                world.spawnEntityInWorld(fishHook);

                player.swingItem();
                player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
            }

        }
        return itemStack;
    }

}
