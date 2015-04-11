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
package org.spongepowered.mod.mixin.core.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityFallingBlock.class)
public abstract class MixinEntityFallingBlock extends MixinEntity implements FallingBlock {

    @Shadow public float fallHurtAmount;
    @Shadow public int fallHurtMax;
    @Shadow public IBlockState fallTile;
    @Shadow public boolean shouldDropItem;
    @Shadow public boolean canSetAsBlock;

    public double getFallDamagePerBlock() {
        return this.fallHurtAmount;
    }

    public void setFallDamagePerBlock(double damage) {
        this.fallHurtAmount = (float) damage;
    }

    public double getMaxFallDamage() {
        return this.fallHurtMax;
    }

    public void setMaxFallDamage(double damage) {
        this.fallHurtMax = (int) damage;
    }

    public BlockState getBlockState() {
        return (BlockState) this.fallTile.getBlock().getBlockState();
    }

    public void setBlockState(BlockState blockState) {
        this.fallTile = (net.minecraft.block.state.IBlockState) blockState;
    }

    public boolean getCanPlaceAsBlock() {
        return this.canSetAsBlock;
    }

    public void setCanPlaceAsBlock(boolean placeable) {
        this.canSetAsBlock = placeable;
    }

    public boolean getCanDropAsItem() {
        return this.shouldDropItem;
    }

    public void setCanDropAsItem(boolean droppable) {
        this.shouldDropItem = droppable;
    }
}
