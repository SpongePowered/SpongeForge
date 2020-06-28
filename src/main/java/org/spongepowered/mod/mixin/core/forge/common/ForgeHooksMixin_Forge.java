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
package org.spongepowered.mod.mixin.core.forge.common;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.DefaultBodyApplier;
import org.spongepowered.api.event.message.MessageEvent.DefaultHeaderApplier;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.bridge.network.NetHandlerPlayServerBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.event.SpongeToForgeEventData;
import org.spongepowered.mod.bridge.event.EventBusBridge_Forge;
import org.spongepowered.mod.bridge.network.INetPlayHandlerBridge_Forge;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksMixin_Forge {

    /**
     * @author blood - July 15th, 2018
     * @reason Overwritten to inject our InteractItemEvent.
     * @param player The player
     */
    @Overwrite
    public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(
        final EntityPlayer player, final BlockPos pos, final EnumFacing face, final Vec3d hitVec)
    {
        final PlayerInteractEvent.LeftClickBlock evt = new PlayerInteractEvent.LeftClickBlock(player, pos, face, hitVec);
        if (player.world.isRemote) {
            ((EventBusBridge_Forge) MinecraftForge.EVENT_BUS).forgeBridge$post(evt, true);
            return evt;
        }

        final ItemStack stack = player.getHeldItemMainhand();
        final BlockSnapshot blockSnapshot = new Location<>((World) player.world, VecHelper.toVector3d(pos)).createSnapshot();
        final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance((EntityPlayerMP) player));
        final Vector3d vec = result == null ? null : VecHelper.toVector3d(result.hitVec);
        if (SpongeCommonEventFactory.callInteractItemEventPrimary(player, stack, EnumHand.MAIN_HAND, vec, blockSnapshot).isCancelled()) {
            ((EntityPlayerMPBridge) player).bridge$sendBlockChange(pos, player.world.getBlockState(pos));
            evt.setCanceled(true);
            return evt;
        }

        MinecraftForge.EVENT_BUS.post(evt);
        return evt;
    }

    @Inject(method = "onItemRightClick", at = @At(value = "HEAD"), cancellable = true)
    private static void forgeImpl$UpdatePacketDiff(final EntityPlayer player, final EnumHand hand, final CallbackInfoReturnable<EnumActionResult> cir) {
        if (!player.world.isRemote && player instanceof EntityPlayerMP && !SpongeImplHooks.isFakePlayer(player)) {
            final long packetDiff = System.currentTimeMillis() - ((NetHandlerPlayServerBridge) ((EntityPlayerMP) player).connection).bridge$getLastTryBlockPacketTimeStamp();
            // If the time between packets is small enough, use the last result.
            if (packetDiff < 100) {
                // Avoid firing a second event
                cir.setReturnValue(null);
            }
        }
    }

    /**
     * @author gabizou - September 27th, 2018
     * @reason Due to forge mods having the ability to still check for
     * mineability without a player instance, some tile entities will
     * check for these sort of things without bothering to throw events
     * or check world.setBlockState return values for capturing.
     *
     * See https://github.com/SpongePowered/SpongeForge/issues/1811
     *
     * @param stack The stack being used
     * @param access The world access
     * @param pos The position
     * @param sameStack The very same stack, required by mixins
     * @return True if the stack is empty, or the change block event pre was cancelled.
     */
    @Redirect(
        method = "canToolHarvestBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
            remap = true
        ),
        remap = false
    )
    private static boolean forgeImpl$UpdateItemStackWithPre(final ItemStack stack, final IBlockAccess access, final BlockPos pos, final ItemStack sameStack)  {
        if (stack.isEmpty()) {
            return true;
        }
        if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && SpongeImplHooks.isMainThread() && access instanceof WorldBridge && !((WorldBridge) access).bridge$isFake()) {
            // If the event is cancelled, return true because then the item was "empty" and therefor, the tool cannot harvest the block.
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
                return SpongeCommonEventFactory.callChangeBlockEventPre(((WorldServerBridge) access), pos).isCancelled();
            }
        }
        return false;
    }

    /**
     * @author gabizou - May 24th, 2018
     * @author blood - July 20th, 2018 - Don't fire event for player interacts
     * @reason Add procussive {@link ChangeBlockEvent.Pre} events to be thrown here
     * as mods can use this hook to check if a block can be broken. Specifically
     * however, this does not interact with {@link ForgeEventFactory#doPlayerHarvestCheck(EntityPlayer, IBlockState, boolean)}
     * since that can potentially be a byproduct of {@link EntityPlayer#canHarvestBlock(IBlockState)}.
     * This assures that Sponge will be able to at the very least interact with the event pre-emptively
     * if some hook elsewhere fails.
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static boolean canHarvestBlock(@Nonnull final Block block, @Nonnull final EntityPlayer player, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        if (state.getMaterial().isToolNotRequired())
        {
            return true;
        }

        final ItemStack stack = player.getHeldItemMainhand();
        final String tool = block.getHarvestTool(state);
        if (stack.isEmpty() || tool == null)
        {
            return player.canHarvestBlock(state);
        }

        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> phaseState = context.state;
        // Many mods use this hook with fake players so we need to avoid passing them when possible
        // and instead pass the true source which is usually a TileEntity
        final Object source = context.getSource() == null ? player : context.getSource();
        if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && !phaseState.isInteraction()) {
            // Sponge Start - Add the changeblockevent.pre check here before we bother with item stacks.
            if (world instanceof WorldBridge && !((WorldBridge) world).bridge$isFake() && SpongeImplHooks.isMainThread()) {
                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    // Might as well provide the active item in use.
                    frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(player.getActiveItemStack()));
                    if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos, source).isCancelled()) {
                        // Since a plugin cancelled it, go ahead and cancel it.
                        return false;
                    }
                }
            }
            // Sponge End
        }

        final int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        if (toolLevel < 0)
        {
            return player.canHarvestBlock(state);
        }

        return toolLevel >= block.getHarvestLevel(state);
    }

    /**
     * @author blood - July 14th, 2018
     * @reason Replaces forge's hook with our own message event in order to handle post properly.
     * @param net The player connection handler
     * @param raw The raw message
     * @param comp The text component
     */
    @Overwrite
    @Nullable
    public static ITextComponent onServerChatEvent(final NetHandlerPlayServer net, final String raw, final ITextComponent comp)
    {
        final MessageFormatter formatter = new MessageFormatter();
        final EntityPlayerMP player = net.player;
        final MessageChannel channel;
        final Text[] chat = SpongeTexts.splitChatMessage(comp);
        if (chat[1] == null) {
            // Move content from head part to body part
            chat[1] = chat[0] != null ? chat[0] : SpongeTexts.toText(comp);
            chat[0] = null;
        }
        if (chat[0] != null) {
            formatter.getHeader().add(new DefaultHeaderApplier(chat[0]));
        }
        formatter.getBody().add(new DefaultBodyApplier(chat[1]));

        final Text rawSpongeMessage = Text.of(raw);
        final MessageChannel originalChannel = channel = ((Player) player).getMessageChannel();
        final MessageChannelEvent.Chat spongeEvent = SpongeEventFactory.createMessageChannelEventChat(Sponge.getCauseStackManager().getCurrentCause(),
            originalChannel, Optional.ofNullable(channel), formatter, rawSpongeMessage, false);
        final SpongeToForgeEventData eventData = ((SpongeModEventManager) Sponge.getEventManager()).extendedPost(spongeEvent, true, false);
        final ITextComponent spongeComponent = SpongeTexts.toComponent(spongeEvent.getMessage());
        if (eventData.getForgeEvent() != null && eventData.getForgeEvent() instanceof ServerChatEvent) {
            final ServerChatEvent forgeEvent = (ServerChatEvent) eventData.getForgeEvent();
            if (!spongeComponent.equals(forgeEvent.getComponent())) {
                forgeEvent.setComponent(spongeComponent);
            }
        }

        if (!spongeEvent.isCancelled()) {
            final Text message = spongeEvent.getMessage();
            if (!spongeEvent.isMessageCancelled()) {
                spongeEvent.getChannel().ifPresent(spongeChannel -> spongeChannel.send(player, message, ChatTypes.CHAT));
            }
        }

        // Chat spam suppression from MC
        final int chatSpamThresholdCount = ((INetPlayHandlerBridge_Forge) player.connection).forgeBridge$getChatSpamThresholdCount() + 20;
        ((INetPlayHandlerBridge_Forge) player.connection).forgeBridge$setChatSpamThresholdCount(chatSpamThresholdCount);
        if (chatSpamThresholdCount > 200 && !SpongeImpl.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
            player.connection.disconnect(new TextComponentTranslation("disconnect.spam"));
        }

        return null;
    }
}
