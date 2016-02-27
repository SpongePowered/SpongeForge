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
package org.spongepowered.mod.mixin.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.mixin.core.block.MixinBlock;

import java.util.ArrayList;

@NonnullByDefault
@Mixin(value = BlockLeaves.class, priority = 1001)
public abstract class MixinBlockLeaves extends MixinBlock {

    @Redirect(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;beginLeavesDecay(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V") )
    public void onBreakBlock(Block block, World worldIn, BlockPos pos) {
        IMixinWorld spongeWorld = (IMixinWorld) worldIn;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        final boolean isBlockAlready = causeTracker.getPhases().current() != TrackingPhases.BLOCK;
        if (isBlockAlready) {
            causeTracker.switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.BLOCK_DECAY, PhaseContext.start()
                    .add(NamedCause.of(TrackingHelper.CAPTURED_BLOCKS, new ArrayList<>()))
                    .add(NamedCause.source(worldIn.getBlockState(pos)))
                    .complete());
        }
        block.beginLeavesDecay(worldIn, pos);
        if (isBlockAlready) {
            causeTracker.completePhase();
        }
    }

}
