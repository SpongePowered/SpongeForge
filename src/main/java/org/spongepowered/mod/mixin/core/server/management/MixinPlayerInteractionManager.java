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
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerInteractionManager;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.TristateUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.event.SpongeToForgeEventData;
import org.spongepowered.mod.event.SpongeModEventManager;

@Mixin(value = PlayerInteractionManager.class, priority = 1001)
public abstract class MixinPlayerInteractionManager implements IMixinPlayerInteractionManager {

    @Shadow public EntityPlayerMP player;
    @Shadow public World world;
    @Shadow private GameType gameType;

    @Shadow public abstract boolean isCreative();
    @Shadow public abstract EnumActionResult processRightClick(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack, EnumHand hand);
    @Shadow(remap = false) public abstract double getBlockReachDistance();
    @Shadow(remap = false) public abstract void setBlockReachDistance(double distance);

    /**
     * @author gabizou - May 5th, 2016
     * @reason Rewrite the firing of interact block events with forge hooks
     * Note: This is a dirty merge of Aaron's SpongeCommon writeup of the interaction events and
     * Forge's additions. There's some overlay between the two events, specifically that there
     * is a SpongeEvent thrown before the ForgeEvent, and yet both are checked in various
     * if statements.
     */
    @Overwrite
    public EnumActionResult processRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (this.gameType == GameType.SPECTATOR) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    player.displayGUIChest(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                player.displayGUIChest((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;
        }
        // Store reference of current player's itemstack in case it changes
        ItemStack oldStack = stack.copy();
        InteractBlockEvent.Secondary event;
        BlockSnapshot currentSnapshot = ((org.spongepowered.api.world.World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final Vector3d hitVec = VecHelper.toVector3d(pos.add(hitX, hitY, hitZ));
        Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(oldStack));
        final boolean interactItemCancelled = SpongeCommonEventFactory.callInteractItemEventSecondary(player, oldStack, hand, hitVec, currentSnapshot).isCancelled();
        event = SpongeCommonEventFactory.createInteractBlockEventSecondary(player, oldStack, hitVec
                , currentSnapshot, DirectionFacingProvider.getInstance().getKey(facing).get(), hand);
        if (interactItemCancelled) {
            event.setUseItemResult(Tristate.FALSE);
        }
        SpongeToForgeEventData eventData = ((SpongeModEventManager) Sponge.getEventManager()).extendedPost(event, false, false);
        if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
            SpongeCommonEventFactory.playerInteractItemChanged = true;
        }
        SpongeCommonEventFactory.lastInteractItemOnBlockCancelled = event.getUseItemResult() == Tristate.UNDEFINED ? false : !event.getUseItemResult().asBoolean();

        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (event.isCancelled()) {
            final IBlockState state = worldIn.getBlockState(pos);

            if (state.getBlock() == Blocks.COMMAND_BLOCK) {
                // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                this.player.connection.sendPacket(new SPacketCloseWindow(0));

            } else if (state.getProperties().containsKey(BlockDoor.HALF)) {
                // Stopping a door from opening while interacting the top part will allow the door to open, we need to update the
                // client to resolve this
                if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.up()));
                } else {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.down()));
                }

            } else if (!stack.isEmpty()) {
                // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                if (stack.getItem() instanceof ItemDoor || (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock()
                    .equals(Blocks.DOUBLE_PLANT))) {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.up(2)));
                }
            }

            // Some mods such as OpenComputers open a GUI on client-side
            // To workaround this, we will always send a SPacketCloseWindow to client if interacting with a TE
            // However, we skip closing under two circumstances:
            //
            // * If an inventory has already been opened on the server (e.g. by a plugin),
            // since we don't want to undo that

            // * If the event was cancelled by a Forge mod. In this case, we adhere to Forge's normal
            // bheavior, which is to leave any GUIs open on the client. Some mods, like Quark, modify
            // Vanilla blocks (such as noteblocks) by opening a custom GUI on the client interaction event,
            // and then cancelling the interaction event on the server.
            //
            // In the second case, we have two conflicting goals. First, we want to ensure that Sponge protection
            // plugins are ablee to fully prevent interactions with a block. This means sending a close
            // window packet to the client when the event is cancelled, since we can't know what
            // client-side only GUIs (no Container) a mod may have opened.
            //
            // However, we don't want to break mods that rely on the fact that cancelling
            // a server-side interaction events leaves any client GUIs open.
            //
            // To resolve this issue, we only send a close window packet if the event was not cancelled
            // by a Forge event listener.
            if (tileEntity != null && this.player.openContainer instanceof ContainerPlayer && (eventData == null || !eventData.getForgeEvent().isCanceled())) {
                this.player.closeScreen();
            }
            SpongeCommonEventFactory.interactBlockEventCancelled = true;
            return EnumActionResult.FAIL;
        }

        net.minecraft.item.Item item = stack.isEmpty() ? null : stack.getItem();
        EnumActionResult ret = item == null || event.getUseItemResult() == Tristate.FALSE
                               ? EnumActionResult.PASS
                               : item.onItemUseFirst(player, worldIn, pos, facing, hitX, hitY, hitZ, hand);
        if (ret != EnumActionResult.PASS) {
            return ret;
        }

        boolean bypass = true;
        final ItemStack[] itemStacks = {player.getHeldItemMainhand(), player.getHeldItemOffhand()};
        for (ItemStack s : itemStacks) {
            bypass = bypass && (s.isEmpty() || s.getItem().doesSneakBypassUse(s, worldIn, pos, player));
        }

        EnumActionResult result = EnumActionResult.PASS;

        if (!player.isSneaking() || bypass || event.getUseBlockResult() == Tristate.TRUE) {
            // Check event useBlockResult, and revert the client if it's FALSE.
            // also, store the result instead of returning immediately
            if (event.getUseBlockResult() != Tristate.FALSE) {
                IBlockState iblockstate = worldIn.getBlockState(pos);
                Container lastOpenContainer = player.openContainer;

                if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                    result = EnumActionResult.SUCCESS;
                }
                // Mods such as StorageDrawers alter the stack on block activation
                // if itemstack changed, avoid restore
                if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
                    SpongeCommonEventFactory.playerInteractItemChanged = true;
                }

                result = this.handleOpenEvent(lastOpenContainer, this.player, currentSnapshot, result);
            } else {
                this.player.connection.sendPacket(new SPacketBlockChange(this.world, pos));
                result = TristateUtil.toActionResult(event.getUseItemResult());

                // Same issue as above with OpenComputers
                // This handles the event not cancelled and block not activated
                // We only run this if the event was changed. If the event wasn't changed,
                // we need to keep the GUI open on the client for Forge compatibility.
                if (result != EnumActionResult.SUCCESS && tileEntity != null && hand == EnumHand.MAIN_HAND) {
                    this.player.closeScreen();
                }
            }
        }

        // store result instead of returning
        if (stack.isEmpty()) {
            result = EnumActionResult.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            result = EnumActionResult.PASS;
        } else if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockCommandBlock && !player.canUseCommand(2, "")) {
            result = EnumActionResult.FAIL;
        } else {
            if ((result != EnumActionResult.SUCCESS && event.getUseItemResult() != Tristate.FALSE || result == EnumActionResult.SUCCESS && event.getUseItemResult() == Tristate.TRUE)) {
                int meta = stack.getMetadata();
                int size = stack.getCount();
                result = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                // nest isCreative check instead of calling the method twice.
                if (this.isCreative()) {
                    stack.setItemDamage(meta);
                    stack.setCount(size);
                }
            }
        }

        if (!ItemStack.areItemStacksEqual(player.getHeldItem(hand), oldStack) || result != EnumActionResult.SUCCESS) {
            player.openContainer.detectAndSendChanges();
        }
        return result;
        // Sponge end
    }
}
