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
package org.spongepowered.mod.mixin.core.entity.living.monster;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.monster.Enderman;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityEnderman.class)
@Implements(@Interface(iface = Enderman.class, prefix = "enderman$"))
public abstract class MixinEntityEnderman extends EntityMob {

    public MixinEntityEnderman(World worldIn) {
        super(worldIn);
    }

    public Optional<BlockState> enderman$getCarriedBlock() {
        return Optional.fromNullable((BlockState) Block.getStateById(this.dataWatcher.getWatchableObjectShort(16) & 65535));
    }

    public void enderman$setCarriedBlock(BlockState carriedBlock) {
        this.dataWatcher
                .updateObject(16, (short) (Block.getStateId(((IBlockState) carriedBlock).getBlock().getDefaultState()) & 65535));
    }

    public boolean enderman$isScreaming() {
        return this.dataWatcher.getWatchableObjectByte(18) > 0;
    }

    public void enderman$setScreaming(boolean screaming) {
        this.dataWatcher.updateObject(18, (byte) (screaming ? 1 : 0));
    }
}
