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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemDoublePlant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;

import java.util.Optional;

@Mixin(value = PlayerInteractionManager.class, priority = 1001)
public abstract class MixinPlayerInteractionManager {

    @Shadow public EntityPlayerMP thisPlayerMP;
    @Shadow public net.minecraft.world.World theWorld;
    @Shadow private WorldSettings.GameType gameType;

    @Shadow public abstract boolean isCreative();
    @Shadow public abstract boolean tryUseItem(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack);

    /**
     * @author blood - April 6th, 2016
     * @reason Activate the clicked on block, otherwise use the held item. Throw events.
     */
    @Overwrite
    public boolean activateBlockOrUseItem(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack, BlockPos pos, EnumFacing side, float offsetX,
            float offsetY, float offsetZ) {
        if (this.gameType == WorldSettings.GameType.SPECTATOR) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    player.displayGUIChest(ilockablecontainer);
                    return true;
                }
            } else if (tileentity instanceof IInventory) {
                player.displayGUIChest((IInventory) tileentity);
                return true;
            }

            return false;
        } else {
            BlockSnapshot currentSnapshot = ((World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            InteractBlockEvent.Secondary event = SpongeCommonEventFactory.callInteractBlockEventSecondary(Cause.of(NamedCause.source(player)),
                    Optional.of(new Vector3d(offsetX, offsetY, offsetZ)), currentSnapshot, DirectionFacingProvider.getInstance().getKey(side).get());

            if (event.isCancelled()) {
                final IBlockState state = worldIn.getBlockState(pos);

                if (state.getBlock() == Blocks.command_block) {
                    // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                    ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S2EPacketCloseWindow(0));

                } else if (state.getProperties().containsKey(BlockDoor.HALF)) {
                    // Stopping a door from opening while interacting the top part will allow the door to open, we need to update the
                    // client to resolve this
                    if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldIn, pos.up()));
                    } else {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldIn, pos.down()));
                    }

                } else if (stack != null) {
                    // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                    if (stack.getItem() instanceof ItemDoor || stack.getItem() instanceof ItemDoublePlant) {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldIn, pos.up(2)));
                    }
                }

                return false;
            }

            if (stack != null && stack.getItem().onItemUseFirst(stack, player, worldIn, pos, side, offsetX, offsetY, offsetZ)) {
                if (stack.stackSize <= 0) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(thisPlayerMP, stack);
                }
                return true;
            }

            IBlockState iblockstate = worldIn.getBlockState(pos);
            boolean useBlock = !player.isSneaking() || player.getHeldItem() == null;
            if (!useBlock) {
                useBlock = player.getHeldItem().getItem().doesSneakBypassUse(worldIn, pos, player);
            }
            boolean result = false;

            if (useBlock) {
                if (event.getUseBlockResult() != Tristate.FALSE) {
                    result = iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, side, offsetX, offsetY, offsetZ);
                } else {
                    thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(theWorld, pos));
                    result = event.getUseItemResult() != Tristate.TRUE;
                }
            }
            if (stack != null && !result && event.getUseItemResult() != Tristate.FALSE) {
                int meta = stack.getMetadata();
                int size = stack.stackSize;
                result = stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
                if (isCreative()) {
                    stack.setItemDamage(meta);
                    stack.stackSize = size;
                }
                if (stack.stackSize <= 0) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(thisPlayerMP, stack);
                }
            }

            // Since we cancel the second packet received while looking at a block with
            // item in hand, we need to make sure to make an attempt to run the 'tryUseItem'
            // method during the first packet.
            if (stack != null && !result && !event.isCancelled() && event.getUseItemResult() != Tristate.FALSE) {
                tryUseItem(player, worldIn, stack);
            }

            return result;
        }
    }
}
