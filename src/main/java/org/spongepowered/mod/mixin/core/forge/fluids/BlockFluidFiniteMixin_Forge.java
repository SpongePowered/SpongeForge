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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;

@Mixin(value = BlockFluidFinite.class)
public abstract class BlockFluidFiniteMixin_Forge extends BlockFluidBaseMixin_Forge {

    @Override
    public BiConsumer<CauseStackManager.StackFrame, WorldServerBridge> bridge$getTickFrameModifier() {
        return (frame, world) -> frame.addContext(EventContextKeys.LIQUID_FLOW, (org.spongepowered.api.world.World) world);
    }

    @Inject(
        method = "updateTick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkBeforeTick(final World world, final BlockPos pos, final IBlockState state, final Random rand, final CallbackInfo ci) {
        if (!((WorldBridge) world).bridge$isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos).isCancelled()) {
                ci.cancel();
            }
        }
    }

    /**
     * @author gabizou - June 4th, 2019 - 1.12.2
     * @reason If a finite liquid can't flow into the direction of
     * {@code BlockFluidBase#densityDir} (usually because if the liquid
     * could try to flow up to the world height), it will set itself to
     * air and return.
     * @param world The world
     * @param pos the target position being flowed into
     * @param amtToInput The quanta input remaining from myState
     * @param cir The callback
     * @param myState The current state
     * @param targetFlow The target position
     */
    @Inject(
        method = "tryToFlowVerticallyInto",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;getHeight()I"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraftforge/fluids/BlockFluidFinite;getQuantaValueBelow(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;I)I",
                remap = false
            )
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void setBlockToAirDueToWorldHeight(final World world, final BlockPos pos, final int amtToInput, final CallbackInfoReturnable<Integer> cir, final IBlockState myState, final BlockPos targetFlow) {
        if (!((WorldBridge) world).bridge$isFake() && ShouldFire.CHANGE_BLOCK_EVENT_BREAK) {
            if (SpongeCommonEventFactory.callChangeBlockEventModifyLiquidBreak(world, pos, myState, Blocks.AIR.getDefaultState()).isCancelled()) {
                cir.setReturnValue(0);
            }
        }
    }

    /**
     * Specifically injecting into the section where
     * the state is adding a new "full" block in the flow
     * direction, without the consequence of scheduling
     * another update or changing the block state of this
     * current block.
     *
     * The code snippet looks like this:
     * <pre>{@code
     *             if (amt > quantaPerBlock)
     *             {
     *                 // Start injection
     *                 CallbackInfoReturnable<Integer> cir = new Callbackblahlbahblha// injection callback
     *                 setNewStateWithMaximumQuantaWhileFlowing(world, pos, amtToInput, cir, myState, other, amt);
     *                 if (cir.isCancelled()) {
     *                     return cir.getIntValue();
     *                 }
     *                 // End injection
     *                 world.setBlockState(other, myState.withProperty(LEVEL, quantaPerBlock - 1));
     *                 world.scheduleUpdate(other, this, tickRate);
     *                 return amt - quantaPerBlock;
     *             }
     *             else if (amt > 0)
     *             {
     *                 // Start injection
     *                 CallbackInfoReturnable<Integer> cir = new Callbackblahlbahblha// injection callback
     *                 setNewStateWithMaximumQuantaWhileFlowing(world, pos, amtToInput, cir, myState, other, amt);
     *                 if (cir.isCancelled()) {
     *                     return cir.getIntValue();
     *                 }
     *                 // End injection
     *                 world.setBlockState(other, myState.withProperty(LEVEL, amt - 1));
     *                 world.scheduleUpdate(other, this, tickRate);
     *                 world.setBlockToAir(pos);
     *                 return 0;
     *             }
     *
     * }</pre>
     */
    @Inject(
        method = "tryToFlowVerticallyInto",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
            remap = true
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraftforge/fluids/BlockFluidFinite;getQuantaValueBelow(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;I)I",
                remap = false
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraftforge/fluids/BlockFluidFinite;getDensity(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
                remap = false
            )
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true,
        constraints = "FORGE(2821+)"
    )
    private void setNewStateWithMaximumQuantaWhileFlowing(final World world, final BlockPos pos, final int amtToInput, final CallbackInfoReturnable<Integer> cir, final IBlockState myState, final BlockPos other, final int newAmount) {
        if (((WorldBridge) world).bridge$isFake() || !ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            return;
        }
        if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, other).isCancelled()) {
            cir.setReturnValue(0);
        }
    }

    /*
     * And because this kerfuffle of swapping liquids of different densities,
     * we have to kinda throw the mix event here for both states.
     */

    /**
     * @author gabizou - June 4th, 2019 - 1.12.2
     * @reason Since there will be a swapping of liquids based on densities,
     * we can technically call it mixing and throw an appropriate event before
     * the scheduled updates are actually added.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
        method = "tryToFlowVerticallyInto",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                shift = At.Shift.BY,
                by = 2,
                slice = "groupA"
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                ordinal = 0,
                slice = "groupB"
            )
        },
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        slice = {
            @Slice(
                id = "groupB",
                from = @At(
                    value = "FIELD",
                    target = "Lnet/minecraftforge/fluids/BlockFluidFinite;density:I",
                    ordinal = 1,
                    remap = false
                ),
                to = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;tickRate(Lnet/minecraft/world/World;)I",
                    ordinal = 1
                )
            ),
            @Slice(
                id = "groupA",
                from = @At(
                    value = "FIELD",
                    target = "Lnet/minecraftforge/fluids/BlockFluidFinite;density:I",
                    ordinal = 0,
                    remap = false
                ),
                to = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;tickRate(Lnet/minecraft/world/World;)I",
                    ordinal = 0
                )
            )
        },
        constraints = "FORGE(2821+)"
    )
    private void onSetBlockForSwapping(final World world, final BlockPos myPos, final int amtToInput, final CallbackInfoReturnable<Integer> cir,
        final IBlockState myState, final BlockPos other, final int amt, final int density_other, final IBlockState otherState) {
        if (((WorldBridge) world).bridge$isFake() || !ShouldFire.CHANGE_BLOCK_EVENT_MODIFY) {
            return;
        }
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        final UUID worldId = ((org.spongepowered.api.world.World) world).getUniqueId();
        final Vector3i myPosition = new Vector3i(myPos.getX(), myPos.getY(), myPos.getZ());
        final SpongeBlockSnapshot mySnapshot = builder.blockState(myState)
            .worldId(worldId)
            .position(myPosition).build();
        final Vector3i otherPosition = new Vector3i(other.getX(), other.getY(), other.getZ());
        final SpongeBlockSnapshot otherSnapshot = builder.reset().blockState(otherState).worldId(worldId).position(otherPosition).build();
        final IBlockState myNewState = myState.withProperty(LEVEL, amtToInput - 1);
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        ((IPhaseState) currentContext.state).associateBlockChangeWithSnapshot(currentContext, myNewState, (BlockFluidFinite) (Object) this,
            otherState, otherSnapshot, otherState.getBlock());
        ((IPhaseState) currentContext.state).associateBlockChangeWithSnapshot(currentContext, otherState, otherState.getBlock(), myState, mySnapshot, (BlockFluidFinite) (Object) this);
        Object source = currentContext.getSource();
        boolean pushSource = false;
        if (source == null) {
            // If source is null the source is the block itself
            pushSource = true;
            source = mySnapshot;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (!pushSource) {
                frame.pushCause(source);
            }
            frame.addContext(EventContextKeys.LIQUID_MIX, (org.spongepowered.api.world.World) world);

            final SpongeBlockSnapshot otherReplacementSnapshot = builder.blockState(myNewState).build();
            final SpongeBlockSnapshot ourReplacementSnapshot = builder.reset().blockState(otherState).worldId(worldId).position(myPosition).build();
            final Transaction<BlockSnapshot> otherReplacement = new Transaction<>(otherSnapshot, otherReplacementSnapshot);
            final Transaction<BlockSnapshot> ourReplacement = new Transaction<>(mySnapshot, ourReplacementSnapshot);
            final ImmutableList<Transaction<BlockSnapshot>> transactions = ImmutableList.of(otherReplacement, ourReplacement);
            if (ShouldFire.CHANGE_BLOCK_EVENT_MODIFY) {
                final ChangeBlockEvent.Modify event = SpongeEventFactory.createChangeBlockEventModify(frame.getCurrentCause(), transactions);
                SpongeImpl.postEvent(event);

                if (event.isCancelled()) {
                    cir.setReturnValue(0);
                }
            }
            if (otherReplacement.getCustom().isPresent() || ourReplacement.getCustom().isPresent()) {
                // Basically, we gotta check if the states match, and if one of them doesn't, well,
                // gotta manually perform the changes.
                final IBlockState otherReplacementState = (IBlockState) otherReplacement.getFinal().getState();
                final IBlockState myReplacementState = (IBlockState) ourReplacement.getFinal().getState();
                world.setBlockState(other, otherReplacementState);
                world.setBlockState(myPos, myReplacementState);
                if (otherReplacementState == myNewState) {
                    world.scheduleUpdate(other, (BlockFluidFinite) (Object) this, this.tickRate);
                } else {
                    world.scheduleUpdate(other, otherReplacementState.getBlock(), otherReplacementState.getBlock().tickRate(world));
                }
                if (myReplacementState == otherState) {
                    world.scheduleUpdate(myPos, otherState.getBlock(), otherState.getBlock().tickRate(world));
                } else {
                    world.scheduleUpdate(myPos, myReplacementState.getBlock(), myReplacementState.getBlock().tickRate(world));
                }
                // And finally, tell the callback to cancel.
                cir.setReturnValue(0);
            }
        }
    }


}
