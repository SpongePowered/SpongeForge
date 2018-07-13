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
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.mixin.core.block.MixinBlock;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = BlockLog.class, priority = 1001)
public abstract class MixinBlockLog extends MixinBlock {

    @Redirect(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;beginLeavesDecay(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", remap = false))
    private void onSpongeBreakBlock(Block block, IBlockState state, net.minecraft.world.World worldIn, BlockPos pos) {
        if (!worldIn.isRemote) {
            if (SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) worldIn, pos).isCancelled()) {
                return;
            }
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final IPhaseState<?> currentState = phaseTracker.getCurrentState();
            final boolean isBlockAlready = currentState.getPhase() != TrackingPhases.BLOCK;
            final boolean isWorldGen = currentState.isWorldGeneration();
            try (final PhaseContext<?> decayContext = createDecayContext((BlockState) state, (World) worldIn, pos, isBlockAlready && !isWorldGen)) {
                if (decayContext != null) {
                    decayContext.buildAndSwitch();
                }
                block.beginLeavesDecay(state, worldIn, pos);
            }
        } else {
            block.beginLeavesDecay(state, worldIn, pos);
        }
    }

    @Nullable
    private PhaseContext<?> createDecayContext(BlockState state, World worldIn, BlockPos pos, boolean canCreate) {
        if (canCreate) {
            final LocatableBlock locatable = LocatableBlock.builder()
                .location(new Location<>(worldIn, pos.getX(), pos.getY(), pos.getZ()))
                .state(state)
                .build();
            return BlockPhase.State.BLOCK_DECAY.createPhaseContext()
                .source(locatable);
        }
        return null;
    }

}
