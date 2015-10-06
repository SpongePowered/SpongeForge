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
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack {

    @Shadow private net.minecraft.item.Item item;
    @Shadow public abstract net.minecraft.item.Item getItem();

    // Disable Forge PlaceEvent patch as we handle this in World setBlockState
    @Overwrite
    public boolean onItemUse(EntityPlayer playerIn, net.minecraft.world.World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        boolean flag = this.getItem().onItemUse((net.minecraft.item.ItemStack)(Object)this, playerIn, worldIn, pos, side, hitX, hitY, hitZ);

        if (flag) {
            playerIn.triggerAchievement(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(this.item)]);
        }

        return flag;
    }
}
