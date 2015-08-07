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
package org.spongepowered.mod.mixin.core.event.player;

import org.spongepowered.mod.mixin.core.event.block.MixinEventBlock;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerHarvestBlockEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(HarvestDropsEvent.class)
public abstract class MixinEventPlayerHarvestBlock extends MixinEventBlock implements PlayerHarvestBlockEvent {

    // TODO: add support for fortuneLevel
    @Shadow public int fortuneLevel;
    @Shadow public List<net.minecraft.item.ItemStack> drops;
    @Shadow public boolean isSilkTouching;
    @Shadow public float dropChance;
    @Shadow public EntityPlayer harvester;

    @Override
    public void setDroppedItems(Collection<Item> items) {
        List<net.minecraft.item.ItemStack> droppedItems = new ArrayList<net.minecraft.item.ItemStack>();
        for (Item item : items) {
            droppedItems.add(((EntityItem) item).getEntityItem());
        }
        this.drops = droppedItems;
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
    public Collection<ItemStack> getDroppedItems() {
        List<ItemStack> droppedItems = new ArrayList<ItemStack>();
        for (net.minecraft.item.ItemStack itemstack : this.drops) {
            droppedItems.add((ItemStack) itemstack);
        }
        return droppedItems;
    }

    @Override
    public boolean isSilkTouch() {
        return this.isSilkTouching;
    }

    @Override
    public Player getEntity() {
        return (Player) this.harvester;
    }

    @Override
    public Player getUser() {
        return (Player) this.harvester;
    }

    @SuppressWarnings("unused")
    private static HarvestDropsEvent fromSpongeEvent(PlayerHarvestBlockEvent spongeEvent) {
        List<net.minecraft.item.ItemStack> droppedItems = new ArrayList<net.minecraft.item.ItemStack>();
        for (ItemStack itemstack : spongeEvent.getDroppedItems()) {
            droppedItems.add(((EntityItem) itemstack).getEntityItem());
        }

        HarvestDropsEvent event =
                new HarvestDropsEvent((net.minecraft.world.World) spongeEvent.getBlock().getExtent(), VecHelper.toBlockPos(spongeEvent.getBlock()
                        .getBlockPosition()), (net.minecraft.block.state.IBlockState) spongeEvent.getBlock().getBlock(), 0,
                        spongeEvent.getDropChance(), droppedItems, (EntityPlayer) spongeEvent.getEntity(), spongeEvent.isSilkTouch());
        ((IMixinEvent) event).setSpongeEvent(spongeEvent);
        return event;
    }
}
