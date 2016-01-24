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
package org.spongepowered.mod.mixin.core.server.management;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.util.StaticMixinHelper;

@Mixin(ItemInWorldManager.class)
public abstract class MixinItemInWorldManager {

    @Shadow public EntityPlayerMP thisPlayerMP;

    @Inject(method = "activateBlockOrUseItem", at = @At(value = "RETURN", ordinal = 3))
    public void onActivateBlockOrUseItem(EntityPlayer player, World worldIn, ItemStack stack, BlockPos pos, EnumFacing side, float p_180236_6_,
            float p_180236_7_, float p_180236_8_, CallbackInfoReturnable<Boolean> cir) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        if (iblockstate.getProperties().containsKey(BlockDoor.HALF)) {
            if (iblockstate.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldIn, pos.up()));
            } else {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldIn, pos.down()));
            }
        }
    }

    @Inject(method = "removeBlock(Lnet/minecraft/util/BlockPos;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"
            + "onBlockDestroyedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
            shift = At.Shift.BEFORE))
    private void gottaCatchThemAll(BlockPos pos, boolean canHarvest, CallbackInfoReturnable<Boolean> cir) {
        StaticMixinHelper.blockDestroyPlayer = this.thisPlayerMP;
    }

    @Inject(method = "removeBlock(Lnet/minecraft/util/BlockPos;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"
            + "onBlockDestroyedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
            shift = At.Shift.AFTER))
    private void releaseCapture(BlockPos pos, boolean canHarvest, CallbackInfoReturnable<Boolean> cir) {
        StaticMixinHelper.blockDestroyPlayer = null;
    }

}
