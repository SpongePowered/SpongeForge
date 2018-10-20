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
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerInteractionManager;
import org.spongepowered.common.interfaces.world.IMixinWorld;
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
    public EnumActionResult processRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos
            pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // SpongeVanilla is using the method inside SpongeCommon. Make sure to keep the two methods consistent.
        if (this.gameType == GameType.SPECTATOR) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    // TODO - fire event
                    player.displayGUIChest(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                // TODO - fire event
                player.displayGUIChest((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;

        } // else { // Sponge - Remove unecessary else
        // Sponge Start - Create an interact block event before something happens.
        // Store reference of current player's itemstack in case it changes
        final ItemStack oldStack = stack.copy();
        final Vector3d hitVec = VecHelper.toVector3d(pos.add(hitX, hitY, hitZ));
        final BlockSnapshot currentSnapshot = ((org.spongepowered.api.world.World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.createInteractBlockEventSecondary(player, oldStack,
                hitVec, currentSnapshot, DirectionFacingProvider.getInstance().getKey(facing).get(), hand);

        // SpongeForge - start
        SpongeToForgeEventData eventData = ((SpongeModEventManager) Sponge.getEventManager()).extendedPost(event, false, false);
        // SpongeForge - end

        if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
            final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
            ((PacketContext<?>) peek.context).interactItemChanged(true);
        }

        SpongeCommonEventFactory.lastInteractItemOnBlockCancelled = event.isCancelled() || event.getUseItemResult() == Tristate.FALSE;

        if (event.isCancelled()) {
            final IBlockState state = (IBlockState) currentSnapshot.getState();

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

            } else if (!oldStack.isEmpty()) {
                // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                final Item item = oldStack.getItem();
                if (item instanceof ItemDoor || (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(Blocks.DOUBLE_PLANT))) {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.up(2)));
                }
            }

            // SpongeForge - start
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
            if (worldIn.getTileEntity(pos) != null && this.player.openContainer instanceof ContainerPlayer && (eventData == null || !eventData.getForgeEvent().isCanceled())) {
                this.player.closeScreen();
            }
            // SpongeForge - end

            SpongeCommonEventFactory.interactBlockRightClickEventCancelled = true;

            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);

            return ((PlayerInteractEvent) eventData.getForgeEvent()).getCancellationResult(); // SpongeForge - return event result
        }
        // Sponge end

        EnumActionResult result = EnumActionResult.PASS;

        if (event.getUseItemResult() != Tristate.FALSE) {
            result = stack.onItemUseFirst(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (result != EnumActionResult.PASS) {
                return result ;
            }
        }

        boolean bypass = true;
        final ItemStack[] itemStacks = {player.getHeldItemMainhand(), player.getHeldItemOffhand()};
        for (ItemStack s : itemStacks) {
            bypass = bypass && (s.isEmpty() || s.getItem().doesSneakBypassUse(s, worldIn, pos, player)); // SpongeForge - use Item#doesSneakBypassUse
        }

        if (!player.isSneaking() || bypass || event.getUseBlockResult() == Tristate.TRUE) {
            // Sponge start - Check event useBlockResult, and revert the client if it's FALSE.
            // also, store the result instead of returning immediately
            if (event.getUseBlockResult() != Tristate.FALSE) {
                IBlockState iblockstate = (IBlockState) currentSnapshot.getState();
                Container lastOpenContainer = player.openContainer;

                // Don't close client gui based on the result of Block#onBlockActivated
                // See https://github.com/SpongePowered/SpongeForge/commit/a684cccd0355d1387a30a7fee08d23fa308273c9
                if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                    result = EnumActionResult.SUCCESS;
                }
                // Mods such as StorageDrawers alter the stack on block activation
                // if itemstack changed, avoid restore
                if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
                    final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
                    ((PacketContext<?>) peek.context).interactItemChanged(true);
                }

                result = this.handleOpenEvent(lastOpenContainer, this.player, currentSnapshot, result);
            } else {
                // Need to send a block change to the client, because otherwise, they are not
                // going to be told about the block change.
                this.player.connection.sendPacket(new SPacketBlockChange(this.world, pos));
                result = TristateUtil.toActionResult(event.getUseItemResult());

                // SpongeForge - start
                // Same issue as above with OpenComputers
                // This handles the event not cancelled and block not activated
                // We only run this if the event was changed. If the event wasn't changed,
                // we need to keep the GUI open on the client for Forge compatibility.
                if (result != EnumActionResult.SUCCESS && worldIn.getTileEntity(pos) != null && hand == EnumHand.MAIN_HAND) {
                    this.player.closeScreen();
                }
                // SpongeForge - end
            }
        }

        if (stack.isEmpty()) {
            return EnumActionResult.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        } else if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();

            if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                return EnumActionResult.FAIL;
            }
        }
        // else if (this.isCreative()) { // Sponge - Rewrite this to handle an isCreative check after the result, since we have a copied stack at the top of this method.
        //    int j = stack.getMetadata();
        //    int i = stack.stackSize;
        //    EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, offsetX, offsetY, offsetZ);
        //    stack.setItemDamage(j);
        //    stack.stackSize = i;
        //    return enumactionresult;
        // } else {
        //    return stack.onItemUse(player, worldIn, pos, hand, facing, offsetX, offsetY, offsetZ);
        // }
        // } // Sponge - Remove unecessary else bracket
        // Sponge Start - complete the method with the micro change of resetting item damage and quantity from the copied stack.

        // SpongeForge - use the stored result
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

        if (!ItemStack.areItemStacksEqual(player.getHeldItem(hand), oldStack) || result != EnumActionResult.SUCCESS) {
            player.openContainer.detectAndSendChanges();
        }

        return result;
        // Sponge end
        // } // Sponge - Remove unecessary else bracket
    }


}
