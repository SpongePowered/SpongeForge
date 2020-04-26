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
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = BlockRailBase.class)
public abstract class BlockRailBaseMixin_Forge {

    @Shadow public abstract BlockRailBase.EnumRailDirection getRailDirection(IBlockAccess world, BlockPos pos, IBlockState state,
        @Nullable EntityMinecart cart);

    // Used to transfer tracking information from minecarts to block positions
    @Inject(method = "onMinecartPass", at = @At(value = "HEAD"), remap = false)
    private void forge$onMinecartRailPass(final World world, final net.minecraft.entity.item.EntityMinecart cart, final BlockPos pos,
            final CallbackInfo ci) {
        if (!(cart instanceof OwnershipTrackedBridge)) {
            return;
        }
        final OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) cart;
        final Optional<User> notifier = ownerBridge.tracked$getNotifierReference();
        final Optional<User> owner = ownerBridge.tracked$getOwnerReference();
        if (owner.isPresent() || notifier.isPresent()) {
            final Chunk chunk = (Chunk) ((ActiveChunkReferantBridge) cart).bridge$getActiveChunk();
            final boolean useActiveChunk = chunk != null && chunk.x == pos.getX() >> 4 && chunk.z == pos.getZ() >> 4;
            final ChunkBridge spongeChunk = (ChunkBridge) (useActiveChunk ? chunk : world.getChunk(pos));
            final Block block = ((Chunk) spongeChunk).getBlockState(pos).getBlock();
            if (notifier.isPresent()) {
                spongeChunk.bridge$addTrackedBlockPosition(block, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
            } else {
                owner.ifPresent(
                    user -> spongeChunk.bridge$addTrackedBlockPosition(block, pos, user, PlayerTracker.Type.NOTIFIER));
            }
        }
    }

    /**
     * @author gabizou - August 10th, 2019 - 1.12.2
     * @reason Forge's rail patch makes a call to {@link World#getBlockState(BlockPos)}
     * for the same {@link IBlockState} that was being notified at the provided
     * {@link BlockPos} in the {@link BlockRailBase#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)}
     * method. The problem here is that there's a corner case that after several
     * updates from neighbor propogation, a final neighbor propogation from a piston
     * event pushing the original rail will have the original rail's block state
     * passed in, and unfortunately, cause a property exception since the new block
     * state is now {@link Blocks#AIR}. Because the remainder of the neighbor update
     * logic after the direction is calculated, there is already a {@link World#isAirBlock(BlockPos)}
     * check on the current target's position, eliminating the need to actually perform
     * any further modifications. The lookup in the end can be patched one of two
     * ways, either provide the current state, but sanity check the state for not
     * matching the block, or provide the passed in block state of the rail for the
     * "delayed" neighbor notification.
     *
     *
     */
    @Redirect(method = "neighborChanged",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockRailBase;getRailDirection(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;"
                    + "Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/item/EntityMinecart;)"
                    + "Lnet/minecraft/block/BlockRailBase$EnumRailDirection;")
    )
    @SuppressWarnings("RedundantCast")
    private BlockRailBase.EnumRailDirection forge$fixRailNeighbor(final BlockRailBase blockRailBase, final IBlockAccess world, final BlockPos pos, final IBlockState state,
        final EntityMinecart cart, final IBlockState original, final World worldIn, final BlockPos originalPos, final Block fromBlock, final BlockPos fromPos) {
        return getRailDirection(world, pos, state.getBlock() == (BlockRailBase) (Object) this ? state : original, cart);
    }
}
