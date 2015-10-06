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
package org.spongepowered.mod.mixin.core.world.gen.feature;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.world.gen.SpongePopulatorType;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.util.StaticMixinHelper;

import java.util.Random;

@Mixin(net.minecraft.world.gen.feature.WorldGenerator.class)
public abstract class MixinWorldGenerator implements Populator {

    private SpongePopulatorType populatorType;

    @Shadow private boolean doBlockNotify;
    @Shadow public abstract boolean generate(net.minecraft.world.World worldIn, Random rand, BlockPos position);

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstructed(boolean notifyBlock, CallbackInfo ci) {
        net.minecraft.world.gen.feature.WorldGenerator gen = ((net.minecraft.world.gen.feature.WorldGenerator)(Object) this);
        if (!gen.getClass().getName().contains("net.minecraft.") && SpongeMod.instance.getSpongeRegistry().populatorClassToTypeMappings.get(gen.getClass()) == null) {
            this.populatorType = new SpongePopulatorType(this.getClass().getSimpleName(),SpongeMod.instance.getModIdFromClass(gen.getClass()), gen.getClass());
            SpongeMod.instance.getSpongeRegistry().registerPopulatorType(this.populatorType);
        }
    }

    @Overwrite
    protected void setBlockAndNotifyAdequately(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state) {
        StaticMixinHelper.populator = this.populatorType;

        if (this.doBlockNotify) {
            worldIn.setBlockState(pos, state, 3);
        } else {
            worldIn.setBlockState(pos, state, 2);
        }

        StaticMixinHelper.populator = null;
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        // DOES NOTHING
    }

}
