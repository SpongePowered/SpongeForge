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
package org.spongepowered.mod.event;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.DefaultBodyApplier;
import org.spongepowered.api.event.message.MessageEvent.DefaultHeaderApplier;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.bridge.util.ForgeBlockSnapshotBridge_Forge;
import org.spongepowered.mod.bridge.network.INetPlayHandlerBridge_Forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles events initiated by Forge mods.
 * It is primarily responsible for firing a corresponding Sponge event to plugins.
 */
@SuppressWarnings({"deprecation", "Duplicates"})
public class ForgeToSpongeEventFactory {

    private static final SpongeModEventManager eventManager = ((SpongeModEventManager) Sponge.getEventManager());
    private static final ImmutableMap<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, Class<? extends Event>>
    forgeToSpongeClassMap = new ImmutableMap.Builder<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, Class<? extends Event>>()
        .put(BlockEvent.BreakEvent.class, ChangeBlockEvent.Pre.class)
        .put(BlockEvent.MultiPlaceEvent.class, ChangeBlockEvent.Place.class)
        .put(BlockEvent.PlaceEvent.class, ChangeBlockEvent.Place.class)
        .put(net.minecraftforge.event.world.ExplosionEvent.Start.class, ExplosionEvent.Pre.class)
        .put(net.minecraftforge.event.world.ExplosionEvent.Detonate.class, ExplosionEvent.Detonate.class)
        .put(PlayerInteractEvent.LeftClickBlock.class, InteractBlockEvent.Primary.class)
        .put(PlayerInteractEvent.RightClickBlock.class, InteractBlockEvent.Secondary.class)
        .put(PlayerInteractEvent.RightClickItem.class, InteractItemEvent.Secondary.class)
        .put(PlayerSleepInBedEvent.class, SleepingEvent.Pre.class)
        .put(ServerChatEvent.class, MessageChannelEvent.Chat.class)
        .build();

    public static Class<? extends Event> getSpongeClass(net.minecraftforge.fml.common.eventhandler.Event event) {
        // Handle special cases
        if (event instanceof PlayerInteractEvent) {
            if (event instanceof PlayerInteractEvent.LeftClickBlock) {
                final PlayerInteractEvent.LeftClickBlock forgeEvent = (PlayerInteractEvent.LeftClickBlock) event;
                if (forgeEvent.getHand() == EnumHand.MAIN_HAND) {
                    return InteractBlockEvent.Primary.MainHand.class;
                }
                return InteractBlockEvent.Primary.OffHand.class;
            }
            if (event instanceof PlayerInteractEvent.RightClickBlock) {
                final PlayerInteractEvent.RightClickBlock forgeEvent = (PlayerInteractEvent.RightClickBlock) event;
                if (forgeEvent.getHand() == EnumHand.MAIN_HAND) {
                    return InteractBlockEvent.Secondary.MainHand.class;
                }
                return InteractBlockEvent.Secondary.OffHand.class;
            }
            if (event instanceof PlayerInteractEvent.RightClickItem) {
                final PlayerInteractEvent.RightClickItem forgeEvent = (PlayerInteractEvent.RightClickItem) event;
                if (forgeEvent.getHand() == EnumHand.MAIN_HAND) {
                    return InteractItemEvent.Secondary.MainHand.class;
                }
                return InteractItemEvent.Secondary.OffHand.class;
            }
        }
        return forgeToSpongeClassMap.get(event.getClass());
    }

    private static Tristate getTristateFromResult(Result result) {
        if (result == Result.ALLOW) {
            return Tristate.TRUE;
        } else if (result == Result.DENY) {
            return Tristate.FALSE;
        }

        return Tristate.UNDEFINED;
    }

    /**
     * This event creates and posts a corresponding sponge event from a forge mod.
     *
     *
     * @param frame
     * @param eventData The forge event data
     * @return The sponge event created or posted
     */
    @SuppressWarnings("deprecation")
    static Event createAndPostSpongeEvent(CauseStackManager.StackFrame frame, ForgeToSpongeEventData eventData) {
        final net.minecraftforge.fml.common.eventhandler.Event forgeEvent = eventData.getForgeEvent();
        if (forgeEvent instanceof BlockEvent.MultiPlaceEvent) {
            return createAndPostChangeBlockEventPlaceMulti(frame, eventData);
        }
        if (forgeEvent instanceof BlockEvent.PlaceEvent) {
            return createAndPostChangeBlockEventPlace(frame, eventData);
        }
        if (forgeEvent instanceof BlockEvent.BreakEvent) {
            return createAndPostChangeBlockEventPre(frame, eventData);
        }
        if (forgeEvent instanceof ServerChatEvent) {
            return createAndPostMessageChannelEventChat(frame, eventData);
        }
        if (forgeEvent instanceof PlayerSleepInBedEvent) {
            return createAndPostSleepingEventPre(frame, eventData);
        }
        if (forgeEvent instanceof PlayerInteractEvent) {
            if (forgeEvent instanceof PlayerInteractEvent.LeftClickBlock) {
                return createAndPostInteractBlockPrimaryEvent(frame, eventData);
            }
            if (forgeEvent instanceof PlayerInteractEvent.RightClickBlock) {
                return createAndPostInteractBlockSecondaryEvent(frame, eventData);
            }
            if (forgeEvent instanceof PlayerInteractEvent.RightClickItem) {
                return createAndPostInteractItemSecondaryEvent(frame, eventData);
            }
        }
        if (forgeEvent instanceof net.minecraftforge.event.world.ExplosionEvent.Start) {
            return createAndPostExplosionEventPre(frame, eventData);
        }
        if (forgeEvent instanceof net.minecraftforge.event.world.ExplosionEvent.Detonate) {
            return createAndPostExplosionEventDetonate(frame, eventData);
        }
        return null;
    }

    private static ExplosionEvent.Pre createAndPostExplosionEventPre(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Start) eventData.getForgeEvent();

        ExplosionEvent.Pre spongeEvent = SpongeEventFactory.createExplosionEventPre(frame.getCurrentCause(), (Explosion) forgeEvent.getExplosion(), (World) forgeEvent.getWorld());
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);

        // TODO - the Forge event doesn't allowing setting the explosion, but the Sponge event does
        // For now, we have no choice but to ignore explosion replacement in the Sponge event.
        return spongeEvent;
    }

    private static ExplosionEvent.Detonate createAndPostExplosionEventDetonate(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        ExplosionEvent.Detonate spongeEvent;
        final net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Detonate) eventData.getForgeEvent();
        final List<Location<World>> blockPositions = new ArrayList<>(forgeEvent.getAffectedBlocks().size());
        final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(forgeEvent.getAffectedEntities().size());
        for (BlockPos pos : forgeEvent.getAffectedBlocks()) {
            blockPositions.add(new Location<>((World) forgeEvent.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        }
        for (Entity entity : forgeEvent.getAffectedEntities()) {
            entities.add((org.spongepowered.api.entity.Entity) entity);
        }

        spongeEvent = SpongeEventFactory.createExplosionEventDetonate(frame.getCurrentCause(), blockPositions, entities, (org.spongepowered.api.world.explosion.Explosion) forgeEvent.getExplosion(), (World) forgeEvent.getWorld());
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        if (spongeEvent.isCancelled()) {
            forgeEvent.getAffectedBlocks().clear();
            forgeEvent.getAffectedEntities().clear();
            return spongeEvent;
        }

        if (forgeEvent.getAffectedBlocks().size() != spongeEvent.getAffectedLocations().size()) {
            forgeEvent.getAffectedBlocks().clear();
            for (Location<World> location : spongeEvent.getAffectedLocations()) {
                forgeEvent.getAffectedBlocks().add(VecHelper.toBlockPos(location));
            }
        }
        if (forgeEvent.getAffectedEntities().size() != spongeEvent.getEntities().size()) {
            forgeEvent.getAffectedEntities().clear();
            for (org.spongepowered.api.entity.Entity entity : spongeEvent.getEntities()) {
                forgeEvent.getAffectedEntities().add((Entity) entity);
            }
        }

        return spongeEvent;
    }

    private static ChangeBlockEvent.Pre createAndPostChangeBlockEventPre(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final BlockEvent.BreakEvent forgeEvent = (BlockEvent.BreakEvent) eventData.getForgeEvent();
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        final BlockPos pos = forgeEvent.getPos();
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();

        EntityPlayer player = forgeEvent.getPlayer();
        User owner = currentContext.getOwner().orElse((User) player);
        User notifier = currentContext.getNotifier().orElse((User) player);

        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.addContext(EventContextKeys.OWNER, owner);
            frame.addContext(EventContextKeys.NOTIFIER, notifier);
        }

        frame.addContext(EventContextKeys.PLAYER_BREAK, (World) world);

        final ChangeBlockEvent.Pre spongeEvent = SpongeEventFactory.createChangeBlockEventPre(frame.getCurrentCause(),
            ImmutableList.of(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ())));
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    private static ChangeBlockEvent.Place createAndPostChangeBlockEventPlace(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final BlockEvent.PlaceEvent forgeEvent = (BlockEvent.PlaceEvent) eventData.getForgeEvent();
        final BlockPos pos = forgeEvent.getPos();
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        BlockSnapshot originalSnapshot = ((ForgeBlockSnapshotBridge_Forge) forgeEvent.getBlockSnapshot()).forgeBridge$toSpongeSnapshot();
        BlockSnapshot
            finalSnapshot =
            ((BlockState) forgeEvent.getPlacedBlock()).snapshotFor(new Location<>((World) world, VecHelper.toVector3d(pos)));
        ImmutableList<Transaction<BlockSnapshot>> blockSnapshots = new ImmutableList.Builder<Transaction<BlockSnapshot>>().add(
            new Transaction<>(originalSnapshot, finalSnapshot)).build();

        EntityPlayer player = forgeEvent.getPlayer();
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        User owner = context.getOwner().orElse((User) player);
        User notifier = context.getNotifier().orElse((User) player);

        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.addContext(EventContextKeys.OWNER, owner);
            frame.addContext(EventContextKeys.NOTIFIER, notifier);
        }

        frame.addContext(EventContextKeys.PLAYER_PLACE, (World) world);
        final ChangeBlockEvent.Place spongeEvent = SpongeEventFactory.createChangeBlockEventPlace(frame.getCurrentCause(), blockSnapshots);
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    private static ChangeBlockEvent.Place createAndPostChangeBlockEventPlaceMulti(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final BlockEvent.MultiPlaceEvent forgeEvent = (BlockEvent.MultiPlaceEvent) eventData.getForgeEvent();
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        ImmutableList.Builder<Transaction<BlockSnapshot>> builder = new ImmutableList.Builder<>();
        for (net.minecraftforge.common.util.BlockSnapshot blockSnapshot : forgeEvent.getReplacedBlockSnapshots()) {
            final BlockPos snapshotPos = blockSnapshot.getPos();
            BlockSnapshot originalSnapshot = ((ForgeBlockSnapshotBridge_Forge) blockSnapshot).forgeBridge$toSpongeSnapshot();
            BlockSnapshot finalSnapshot = ((World) world).createSnapshot(snapshotPos.getX(), snapshotPos.getY(), snapshotPos.getZ());
            builder.add(new Transaction<>(originalSnapshot, finalSnapshot));
        }

        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        EntityPlayer player = forgeEvent.getPlayer();
        User owner = currentContext.getOwner().orElse((User) player);
        User notifier = currentContext.getNotifier().orElse((User) player);

        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.addContext(EventContextKeys.OWNER, owner);
            frame.addContext(EventContextKeys.NOTIFIER, notifier);
        }

        frame.addContext(EventContextKeys.PLAYER_PLACE, (World) world);

        final ChangeBlockEvent.Place spongeEvent = SpongeEventFactory.createChangeBlockEventPlace(frame.getCurrentCause(), builder.build());
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    @SuppressWarnings("ConstantConditions")
    private static MessageChannelEvent.Chat createAndPostMessageChannelEventChat(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final ServerChatEvent forgeEvent = (ServerChatEvent) eventData.getForgeEvent();
        final ITextComponent forgeComponent = forgeEvent.getComponent();
        final MessageFormatter formatter = new MessageFormatter();
        MessageChannel channel;
        Text[] chat = SpongeTexts.splitChatMessage(forgeComponent);
        if (chat[1] == null) {
            // Move content from head part to body part
            chat[1] = chat[0] != null ? chat[0] : SpongeTexts.toText(forgeComponent);
            chat[0] = null;
        }
        if (chat[0] != null) {
            formatter.getHeader().add(new DefaultHeaderApplier(chat[0]));
        }
        formatter.getBody().add(new DefaultBodyApplier(chat[1]));

        Text rawSpongeMessage = Text.of(forgeEvent.getMessage());
        MessageChannel originalChannel = channel = ((Player) forgeEvent.getPlayer()).getMessageChannel();
        frame.pushCause(forgeEvent.getPlayer());
        final MessageChannelEvent.Chat spongeEvent = SpongeEventFactory.createMessageChannelEventChat(frame.getCurrentCause(),
            originalChannel, Optional.ofNullable(channel), formatter, rawSpongeMessage, false);
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        final ITextComponent spongeComponent = SpongeTexts.toComponent(spongeEvent.getMessage());
        if (!spongeComponent.equals(forgeEvent.getComponent())) {
            forgeEvent.setComponent(spongeComponent);
        }

        return spongeEvent;
    }

    private static SleepingEvent.Pre createAndPostSleepingEventPre(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final PlayerSleepInBedEvent forgeEvent = (PlayerSleepInBedEvent) eventData.getForgeEvent();
        final net.minecraft.world.World world = forgeEvent.getEntity().getEntityWorld();
        if (world.isRemote) {
            return null;
        }

        final BlockPos pos = forgeEvent.getPos();
        BlockSnapshot bedSnapshot = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        frame.pushCause(forgeEvent.getEntity());
        final SleepingEvent.Pre spongeEvent = SpongeEventFactory.createSleepingEventPre(frame.getCurrentCause(), bedSnapshot, (org.spongepowered.api.entity.Entity) forgeEvent.getEntity());
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    private static LoadChunkEvent createAndPostLoadChunkEvent(CauseStackManager.StackFrame frame, ForgeToSpongeEventData eventData) {
        final ChunkEvent.Load forgeEvent = (ChunkEvent.Load) eventData.getForgeEvent();
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        if (isMainThread) {
            frame.pushCause(forgeEvent.getWorld());
        }
        final Cause cause = isMainThread ? frame.getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
        final LoadChunkEvent spongeEvent = SpongeEventFactory.createLoadChunkEvent(cause, (Chunk) forgeEvent.getChunk());
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    private static UnloadChunkEvent createAndPostUnloadChunkEvent(CauseStackManager.StackFrame frame, ForgeToSpongeEventData eventData) {
        final ChunkEvent.Unload forgeEvent = (ChunkEvent.Unload) eventData.getForgeEvent();
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        if (isMainThread) {
            frame.pushCause(forgeEvent.getWorld());
        }
        final Cause cause = isMainThread ? frame.getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
        final UnloadChunkEvent spongeEvent = SpongeEventFactory.createUnloadChunkEvent(cause, (Chunk) forgeEvent.getChunk());
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    private static InteractItemEvent.Secondary createAndPostInteractItemSecondaryEvent(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final PlayerInteractEvent.RightClickItem forgeEvent = (PlayerInteractEvent.RightClickItem) eventData.getForgeEvent();
        final ItemStack heldItem = forgeEvent.getItemStack();
        final BlockPos pos = forgeEvent.getPos();
        final EnumFacing face = forgeEvent.getFace();
        final Direction direction = face == null ? Direction.NONE : DirectionFacingProvider.getInstance().getKey(face).get();
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final EntityPlayerMP player = (EntityPlayerMP) forgeEvent.getEntityPlayer();
        final User owner = currentContext.getOwner().orElse((User) player);
        final User notifier = currentContext.getNotifier().orElse((User) player);

        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.OWNER, owner);
            frame.addContext(EventContextKeys.NOTIFIER, notifier);
        }

        BlockSnapshot currentSnapshot;
        final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance(player));
        if (result != null) {
            if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
                frame.addContext(EventContextKeys.ENTITY_HIT, ((org.spongepowered.api.entity.Entity) result.entityHit));
            } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                currentSnapshot = ((org.spongepowered.api.world.World) player.world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
                frame.addContext(EventContextKeys.BLOCK_HIT, currentSnapshot);
            }
        }
        final Vector3d hitVec = result == null ? null : VecHelper.toVector3d(result.hitVec);
        InteractItemEvent.Secondary spongeEvent;
        if (forgeEvent.getHand() == EnumHand.MAIN_HAND) {
            spongeEvent = SpongeEventFactory.createInteractItemEventSecondaryMainHand(frame.getCurrentCause(),
                    HandTypes.MAIN_HAND, Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(heldItem));
        } else {
            spongeEvent = SpongeEventFactory.createInteractItemEventSecondaryOffHand(frame.getCurrentCause(),
                    HandTypes.OFF_HAND, Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(heldItem));
        }
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        if (spongeEvent.isCancelled()) {
            // Multiple slots may have been changed on the client. Right
            // clicking armor is one example - the client changes it
            // without the server telling it to.
            player.sendAllContents(player.openContainer, player.openContainer.getInventory());
        }
        return spongeEvent;
    }

    private static InteractBlockEvent.Primary createAndPostInteractBlockPrimaryEvent(CauseStackManager.StackFrame frame,
        ForgeToSpongeEventData eventData) {
        final PlayerInteractEvent.LeftClickBlock forgeEvent = (PlayerInteractEvent.LeftClickBlock) eventData.getForgeEvent();
        final BlockPos pos = forgeEvent.getPos();
        final BlockSnapshot blockSnapshot = ((World) forgeEvent.getWorld()).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final EnumFacing face = forgeEvent.getFace();
        final Direction direction = face == null ? Direction.NONE : DirectionFacingProvider.getInstance().getKey(face).get();
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final EntityPlayerMP player = (EntityPlayerMP) forgeEvent.getEntityPlayer();
        final User owner = currentContext.getOwner().orElse((User) player);
        final User notifier = currentContext.getNotifier().orElse((User) player);

        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.OWNER, owner);
            frame.addContext(EventContextKeys.NOTIFIER, notifier);
        }

        InteractBlockEvent.Primary spongeEvent;
        if (forgeEvent.getHand() == EnumHand.MAIN_HAND) {
            spongeEvent = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(frame.getCurrentCause(), HandTypes.MAIN_HAND,
                    Optional.ofNullable(VecHelper.toVector3d(forgeEvent.getHitVec())), blockSnapshot, direction);
        } else {
            spongeEvent = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(frame.getCurrentCause(), HandTypes.OFF_HAND,
                    Optional.ofNullable(VecHelper.toVector3d(forgeEvent.getHitVec())), blockSnapshot, direction);
        }
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    private static InteractBlockEvent.Secondary createAndPostInteractBlockSecondaryEvent(
        CauseStackManager.StackFrame frame, ForgeToSpongeEventData eventData) {
        final PlayerInteractEvent.RightClickBlock forgeEvent = (PlayerInteractEvent.RightClickBlock) eventData.getForgeEvent();
        final HandType hand = forgeEvent.getHand() == EnumHand.MAIN_HAND ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;
        final Tristate useBlockResult = getTristateFromResult(forgeEvent.getUseBlock());
        final Tristate useItemResult = getTristateFromResult(forgeEvent.getUseItem());
        final Vector3d interactionPoint = VecHelper.toVector3d(forgeEvent.getHitVec());
        final BlockPos pos = forgeEvent.getPos();
        final BlockSnapshot blockSnapshot = ((World) forgeEvent.getWorld()).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final Direction direction = DirectionFacingProvider.getInstance().getKey(forgeEvent.getFace()).get();
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final EntityPlayerMP player = (EntityPlayerMP) forgeEvent.getEntityPlayer();
        final User owner = currentContext.getOwner().orElse((User) player);
        final User notifier = currentContext.getNotifier().orElse((User) player);

        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.OWNER, owner);
            frame.addContext(EventContextKeys.NOTIFIER, notifier);
        }

        InteractBlockEvent.Secondary spongeEvent = null;
        if (hand == HandTypes.MAIN_HAND) {
            spongeEvent = SpongeEventFactory.createInteractBlockEventSecondaryMainHand(frame.getCurrentCause(), useBlockResult, useBlockResult, useItemResult, useItemResult, hand, Optional.ofNullable(interactionPoint), blockSnapshot, direction);
        } else {
            spongeEvent = SpongeEventFactory.createInteractBlockEventSecondaryOffHand(frame.getCurrentCause(), useBlockResult, useBlockResult, useItemResult, useItemResult, hand, Optional.ofNullable(interactionPoint), blockSnapshot, direction);
        }
        eventData.setSpongeEvent(spongeEvent);
        eventManager.postEvent(eventData);
        return spongeEvent;
    }

    // Special handler to process any events after ALL events have been posted to both Forge and Sponge
    static void onPostEnd(ForgeToSpongeEventData eventData) {
        if (eventData.getForgeEvent() instanceof ServerChatEvent) {
            final MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) eventData.getSpongeEvent();
            final ServerChatEvent forgeEvent = (ServerChatEvent) eventData.getForgeEvent();
            final EntityPlayerMP player = forgeEvent.getPlayer();
            if (!spongeEvent.isCancelled()) {
                Text message = spongeEvent.getMessage();
                if (!spongeEvent.isMessageCancelled()) {
                    spongeEvent.getChannel().ifPresent(channel -> channel.send(forgeEvent.getPlayer(), message, ChatTypes.CHAT));
                }
            }

            // Chat spam suppression from MC
            if (!SpongeImplHooks.isFakePlayer(player)) {
                // We don't want to check fake players.
                int chatSpamThresholdCount = ((INetPlayHandlerBridge_Forge) player.connection).forgeBridge$getChatSpamThresholdCount() + 20;
                ((INetPlayHandlerBridge_Forge) player.connection).forgeBridge$setChatSpamThresholdCount(chatSpamThresholdCount);
                if (chatSpamThresholdCount > 200 && !SpongeImpl.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
                    player.connection.disconnect(new TextComponentTranslation("disconnect.spam"));
                }
            }
            // We set the forge component to null here to exit NetHandlerPlayServer#processChatMessage before it sends a message
            // This is done since we send our own message via the channel above
            forgeEvent.setComponent(null);
        } else if (eventData.getForgeEvent() instanceof PlayerInteractEvent.RightClickItem && !eventData.getForgeEvent().isCanceled()) {
            final PlayerInteractEvent.RightClickItem forgeEvent = (PlayerInteractEvent.RightClickItem) eventData.getForgeEvent();
            final BlockPos pos = forgeEvent.getPos();
            final EnumFacing face = forgeEvent.getFace();
            final ItemStack heldItem = forgeEvent.getItemStack();
            final Vector3d hitVec = VecHelper.toVector3d(new BlockPos(forgeEvent.getEntityPlayer()));
            final BlockSnapshot blockSnapshot = ((World) forgeEvent.getWorld()).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            final Direction direction = face == null ? Direction.NONE : DirectionFacingProvider.getInstance().getKey(face).get();
            SpongeImpl.postEvent(SpongeCommonEventFactory.createInteractBlockEventSecondary(forgeEvent.getEntityPlayer(), heldItem, hitVec, blockSnapshot, direction, forgeEvent.getHand()));
        }
    }
}
