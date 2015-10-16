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

import org.spongepowered.common.mixin.core.entity.living.MixinEntityLivingBase;

import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Overwrite;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Shadow public InventoryPlayer inventory;
    @Shadow public abstract EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem);

    // Restore methods to original as we handle PlayerTossEvent in DropItemEvent
    @Overwrite
    public EntityItem dropOneItem(boolean dropAll) {
        return this.dropItem(this.inventory.decrStackSize(this.inventory.currentItem, dropAll && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1), false, true);
    }

    @Overwrite
    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean unused) {
        return this.dropItem(itemStackIn, false, false);
    }
}
