/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.mod.mixin.core.block;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemBlock;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.translation.SpongeTranslation;

@NonnullByDefault
@Mixin(Block.class)
public abstract class MixinBlockType implements BlockType {

    @Shadow
    private boolean needsRandomTick;

    @Shadow(prefix = "shadow$")
    public abstract IBlockState shadow$getDefaultState();

    @Shadow
    public abstract String getUnlocalizedName();

    @Shadow
    public abstract boolean isFullCube();

    @Shadow
    public abstract boolean getEnableStats();

    @Shadow
    public abstract int getLightValue();

    @Shadow
    public abstract IBlockState getStateFromMeta(int meta);

    @Override
    public String getId() {
        return Block.blockRegistry.getNameForObject(this).toString();
    }

    @Override
    public BlockState getDefaultState() {
        return (BlockState) shadow$getDefaultState();
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getUnlocalizedName() + ".name");
    }

    @Override
    @Deprecated
    public BlockState getStateFromDataValue(byte data) {
        return (BlockState) getStateFromMeta(data);
    }

    @Override
    public boolean isLiquid() {
        return BlockLiquid.class.isAssignableFrom(this.getClass());
    }

    @Override
    public boolean isSolidCube() {
        return isFullCube();
    }

    @Override
    public boolean isAffectedByGravity() {
        return BlockFalling.class.isAssignableFrom(this.getClass());
    }

    @Override
    public boolean areStatisticsEnabled() {
        return getEnableStats();
    }

    @Override
    public float getEmittedLight() {
        return 15F / getLightValue();
    }

    @Override
    @Overwrite
    public boolean getTickRandomly() {
        return this.needsRandomTick;
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
    }

    @Override
    public Optional<ItemBlock> getHeldItem() {
        return Optional.fromNullable((ItemBlock) Item.getItemFromBlock((Block) (Object) this));
    }

}
