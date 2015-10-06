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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldInfo;

import java.util.Random;

@NonnullByDefault
@Mixin(value = WorldServer.class, priority = 1001)
public abstract class MixinWorldServer extends MixinWorld {

    @Shadow public abstract void updateBlockTick(BlockPos p_175654_1_, Block p_175654_2_, int p_175654_3_, int p_175654_4_);
    @Shadow public abstract boolean fireBlockEvent(BlockEventData event);

    @Inject(method = "init", at = @At("RETURN"))
    public void onPostInit(CallbackInfoReturnable<net.minecraft.world.World> ci) {
        net.minecraft.world.World world = ci.getReturnValue();
        if (!((IMixinWorldInfo) world.getWorldInfo()).getIsMod()) {
            // Run the world generator modifiers in the init method
            // (the "init" method, not the "<init>" constructor)
            IMixinWorld mixinWorld = (IMixinWorld) world;
            mixinWorld.updateWorldGenerator();
        }
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlocks(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null) {
            block.randomTick(worldIn, pos, state, rand);
            return;
        }

        this.currentTickBlock = createSpongeBlockSnapshot(state, pos, 0);
        block.randomTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlockTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        this.currentTickBlock = createSpongeBlockSnapshot(state, pos, 0);
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
    }
 
    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;"
            + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        this.currentTickBlock = createSpongeBlockSnapshot(state, pos, 0);
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
    }

    // special handling for Pistons since they use their own event system
    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;fireBlockEvent(Lnet/minecraft/block/BlockEventData;)Z"))
    public boolean onFireBlockEvent(net.minecraft.world.WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        this.currentTickBlock = createSpongeBlockSnapshot(currentState, event.getPosition(), 3);
        boolean result = fireBlockEvent(event);
        this.handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
        return result;
    }
}
