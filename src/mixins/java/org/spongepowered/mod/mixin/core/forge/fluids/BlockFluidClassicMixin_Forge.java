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
package org.spongepowered.mod.mixin.core.forge.fluids;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Random;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

@Mixin(value = BlockFluidClassic.class)
public abstract class BlockFluidClassicMixin_Forge extends BlockFluidBaseMixin_Forge {

    @Override
    public BiConsumer<CauseStackManager.StackFrame, WorldServerBridge> bridge$getTickFrameModifier() {
        return (frame, world) -> frame.addContext(EventContextKeys.LIQUID_FLOW, (org.spongepowered.api.world.World) world);
    }

    @Inject(method = "updateTick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUpdateTickCheckSpongePre(final World world, final BlockPos pos, final IBlockState state, final Random rand, final CallbackInfo ci) {
        if (!((WorldBridge) world).bridge$isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos).isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Nullable private Boolean isPreCancelled;

    /**
     * @author gabizou - June 4th, 2019 - 1.12.2
     * @reason In the event the injection above does cancel, we need to duck out of the rest of the method.
     * Otherwise, the above injection never went through, the {@link BlockFluidBase#canDisplace(IBlockAccess, BlockPos)}
     * ends up getting it's injection thrown anyways.
     *
     * @param world The world
     * @param pos the position being flown into
     * @param state The state
     * @param rand The rando
     * @param ci The callback info
     */
    @Inject(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/BlockFluidClassic;canDisplace(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Z",
            remap = false
        ),
        cancellable = true
    )
    private void onCheckDisplaceIfPreAlreadyCancelled(
        final World world, final BlockPos pos, final IBlockState state, final Random rand, final CallbackInfo ci) {
        if (this.isPreCancelled == Boolean.TRUE) {
            ci.cancel();
            // And reset the field so we don't leak...
            this.isPreCancelled = null;
        }
    }

    // Capture Fluids flowing into other blocks
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
        method = "flowIntoBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"
        ),
        constraints = "FORGE(2821+)"
    )
    private boolean afterCanFlowInto(
        final World targetWorld, final BlockPos targetPos, final IBlockState newLiquidState, final World world, final BlockPos pos, final int meta) {
        if (((WorldBridge) targetWorld).bridge$isFake() || !ShouldFire.CHANGE_BLOCK_EVENT_BREAK) { // Check if we even need to fire.
            return world.setBlockState(targetPos, newLiquidState, Constants.BlockFlags.DEFAULT);
        }

        final IBlockState existing = targetWorld.getBlockState(targetPos);
        // Do not call events when just flowing into air or same liquid
        //noinspection RedundantCast
        if (!existing.getBlock().isAir(existing, targetWorld, targetPos) && !(existing.getMaterial().isLiquid() || existing.getBlock() == (BlockFluidClassic) (Object) this)) {
            final ChangeBlockEvent.Break event = SpongeCommonEventFactory.callChangeBlockEventModifyLiquidBreak(world, pos, newLiquidState);

            final Transaction<BlockSnapshot> transaction = event.getTransactions().get(0);
            if (event.isCancelled() || !transaction.isValid()) {
                // We need to clear any drops here because this is called after {@link #displaceIfPossible} and since it was true, well
                // there's the likelyhood that there's a dropped item
                final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
                ((IPhaseState) currentContext.state).processCancelledTransaction(currentContext, transaction, transaction.getOriginal());
                return false;
            }

            // Transaction modified?
            if (transaction.getCustom().isPresent()) {
                return targetWorld.setBlockState(targetPos, (IBlockState) transaction.getFinal().getState(), Constants.BlockFlags.DEFAULT);
            }
        }

        return targetWorld.setBlockState(targetPos, newLiquidState, Constants.BlockFlags.DEFAULT);
    }

}
