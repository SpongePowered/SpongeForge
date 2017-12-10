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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack {

    @Shadow private net.minecraft.item.Item item;
    @Shadow public abstract net.minecraft.item.Item getItem();

    // Disable Forge PlaceEvent patch as we handle this in World setBlockState

    /**
     * @author blood - October 7th, 2015
     * @reason Rewrites the method to vanilla logic where we handle events.
     * The forge event is thrown within our system.
     *
     * @param playerIn The player using the item
     * @param worldIn The world the item is being used
     * @param pos The position of the block being interacted with
     * @param side The side of the block
     * @param hitX The hit position from 0 to 1 of the face of the block
     * @param hitY The hit position from 0 to 1 on the depth of the block
     * @param hitZ The hit position from 0 to 1 on the height of the block
     * @return True if the use was successful
     */
    @Overwrite
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing
            side, float hitX, float hitY, float hitZ) {
        final EnumActionResult result = this.getItem().onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);

        if (result == EnumActionResult.SUCCESS) {
            playerIn.addStat(StatList.getObjectUseStats(this.item));
        }

        return result;
    }


}
