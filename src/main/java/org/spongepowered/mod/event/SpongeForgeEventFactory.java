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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.DefaultBodyApplier;
import org.spongepowered.api.event.message.MessageEvent.DefaultHeaderApplier;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.UnwindingPhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinBlockSnapshot;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeForgeEventFactory {

    /**
     * Used by {@link SpongeModEventManager#extendedPost} to obtain
     * corresponding forge event class if available.
     * 
     * @param spongeEvent The sponge event to check against forge
     * @return The forge event class, if available
     */
    static Class<? extends net.minecraftforge.fml.common.eventhandler.Event> getForgeEventClass(Event spongeEvent) {
        final Class<? extends Event> clazz = spongeEvent.getClass();
        if (ChangeInventoryEvent.Pickup.Pre.class.isAssignableFrom(clazz)) {
            if (spongeEvent.getCause().root() instanceof Player) {
                return EntityItemPickupEvent.class;
            }
        }
        if (DestructEntityEvent.Death.class.isAssignableFrom(clazz)) {
            return LivingDeathEvent.class;
        }
        if (InteractBlockEvent.Primary.class.isAssignableFrom(clazz)) {
            return PlayerInteractEvent.class;
        }
        if (InteractBlockEvent.Secondary.class.isAssignableFrom(clazz)) {
            return PlayerInteractEvent.class;
        }
        if (InteractBlockEvent.class.isAssignableFrom(clazz)) {
            return PlayerInteractEvent.class;
        }
        if (InteractEntityEvent.Secondary.class.isAssignableFrom(clazz)) {
            InteractEntityEvent event = (InteractEntityEvent) spongeEvent;
            if (event.getInteractionPoint().isPresent()) {
                return PlayerInteractEvent.EntityInteractSpecific.class;
            } else {
                return PlayerInteractEvent.EntityInteract.class;
            }
        }
        if (NotifyNeighborBlockEvent.class.isAssignableFrom(clazz)) {
            return BlockEvent.NeighborNotifyEvent.class;
        }
        if (ChangeBlockEvent.Place.class.isAssignableFrom(clazz)) {
            return BlockEvent.PlaceEvent.class;
        }
        if (ExplosionEvent.Pre.class.isAssignableFrom(clazz)) {
            return net.minecraftforge.event.world.ExplosionEvent.Start.class;
        }
        if (ExplosionEvent.Detonate.class.isAssignableFrom(clazz)) {
            return net.minecraftforge.event.world.ExplosionEvent.Detonate.class;
        }
        if (DropItemEvent.Destruct.class.isAssignableFrom(clazz)) {
            return LivingDropsEvent.class;
        }
        if (DropItemEvent.Dispense.class.isAssignableFrom(clazz)) {
            return ItemTossEvent.class;
        }
        if (DropItemEvent.Custom.class.isAssignableFrom(clazz)) {
            return ItemTossEvent.class;
        }
        if (ClientConnectionEvent.Join.class.isAssignableFrom(clazz)) {
            return PlayerLoggedInEvent.class;
        }
        if (ClientConnectionEvent.Disconnect.class.isAssignableFrom(clazz)) {
            return PlayerLoggedOutEvent.class;
        }
        if (MoveEntityEvent.Teleport.class.isAssignableFrom(clazz)) {
            return EntityTravelToDimensionEvent.class;
        }
        if (SpawnEntityEvent.class.isAssignableFrom(clazz)) {
            return EntityJoinWorldEvent.class;
        }
        if (LoadWorldEvent.class.isAssignableFrom(clazz)) {
            return WorldEvent.Load.class;
        }
        if (UnloadWorldEvent.class.isAssignableFrom(clazz)) {
            return WorldEvent.Unload.class;
        }
        if (SaveWorldEvent.Post.class.isAssignableFrom(clazz)) {
            return WorldEvent.Save.class;
        }
        if (LoadChunkEvent.class.isAssignableFrom(clazz)) {
            return ChunkEvent.Load.class;
        }
        if (UnloadChunkEvent.class.isAssignableFrom(clazz)) {
            return ChunkEvent.Unload.class;
        }
        if (FishingEvent.Stop.class.isAssignableFrom(clazz)) {
            return ItemFishedEvent.class;
        }
        if (UseItemStackEvent.Start.class.isAssignableFrom(clazz)) {
            return LivingEntityUseItemEvent.Start.class;
        }
        if (UseItemStackEvent.Tick.class.isAssignableFrom(clazz)) {
            return LivingEntityUseItemEvent.Tick.class;
        }
        if (UseItemStackEvent.Stop.class.isAssignableFrom(clazz)) {
            return LivingEntityUseItemEvent.Stop.class;
        }
        if (UseItemStackEvent.Replace.class.isAssignableFrom(clazz)) {
            return LivingEntityUseItemEvent.Finish.class;
        }
        if (org.spongepowered.api.event.advancement.AdvancementEvent.Grant.class.isAssignableFrom(clazz)) {
            return AdvancementEvent.class;
        }
        return null;
    }

<<<<<<< HEAD
    // ====================================  FORGE TO SPONGE START ==================================== \\
    public static Event createSpongeEvent(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) { // TODO - maybe use the frame as a passed object, instead of having to create new frames.
        return propgateCancellation(createSpongeEventImpl(forgeEvent), forgeEvent);
=======
    private static Tristate getTristateFromResult(Result result) {
        if (result == Result.ALLOW) {
            return Tristate.TRUE;
        } else if (result == Result.DENY) {
            return Tristate.FALSE;
        }

        return Tristate.UNDEFINED;
>>>>>>> Refactor SpongeForgeEventFactory.
    }

    // ====================================  FORGE TO SPONGE START ==================================== \\
    // This section handles events initiated by Forge mods.
    // It is primarily responsible for firing a corresponding Sponge event to plugins.

    /**
     * This event creates or posts a corresponding sponge event from a forge mod.
     * 
     * @param eventData The forge event data
     * @return The sponge event created or posted
     */
    public static Event createOrPostSpongeEvent(ForgeEventData eventData) {
        final net.minecraftforge.fml.common.eventhandler.Event forgeEvent = eventData.getForgeEvent();
        if (forgeEvent instanceof BlockEvent.MultiPlaceEvent) {
            return createOrPostChangeBlockEventPlace(eventData);
        }
        if (forgeEvent instanceof BlockEvent.PlaceEvent) {
            return createOrPostChangeBlockEventPlace(eventData);
        }
        if (forgeEvent instanceof BlockEvent.BreakEvent) {
            return createOrPostChangeBlockEventPre(eventData);
        }
        if (forgeEvent instanceof ServerChatEvent) {
            return createOrPostMessageChannelEventChat(eventData);
        }
        if (forgeEvent instanceof PlayerSleepInBedEvent) {
            return createOrPostSleepingEventPre(eventData);
        }
        if (forgeEvent instanceof ChunkEvent.Load) {
            return createOrPostLoadChunkEvent(eventData);
        }
        if (forgeEvent instanceof ChunkEvent.Unload) {
            return createOrPostUnloadChunkEvent(eventData);
        }
        if (forgeEvent instanceof PlayerInteractEvent.RightClickBlock) {
            return createOrPostInteractBlockSecondaryEvent(eventData);
        }
        if (forgeEvent instanceof net.minecraftforge.event.world.ExplosionEvent.Detonate) {
            return createOrPostExplosionEventDetonate(eventData);
        }
        return null;
    }

    private static ExplosionEvent.Detonate createOrPostExplosionEventDetonate(ForgeEventData eventData) {
        final ExplosionEvent.Detonate spongeEvent = (ExplosionEvent.Detonate) eventData.getSpongeEvent();
        final net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Detonate) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final List<Location<World>> blockPositions = new ArrayList<>(forgeEvent.getAffectedBlocks().size());
            final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(forgeEvent.getAffectedEntities().size());
            for (BlockPos pos : forgeEvent.getAffectedBlocks()) {
                blockPositions.add(new Location<>((World) forgeEvent.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
            }
            for (Entity entity : forgeEvent.getAffectedEntities()) {
                entities.add((org.spongepowered.api.entity.Entity) entity);
            }

            return SpongeEventFactory.createExplosionEventDetonate(Sponge.getCauseStackManager().getCurrentCause(), blockPositions, entities, (org.spongepowered.api.world.explosion.Explosion) forgeEvent.getExplosion(), (World) forgeEvent.getWorld());
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        if (spongeEvent.isCancelled()) {
            forgeEvent.getAffectedBlocks().clear();
            forgeEvent.getAffectedEntities().clear();
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

    private static ChangeBlockEvent.Pre createOrPostChangeBlockEventPre(ForgeEventData eventData) {
        final ChangeBlockEvent.Pre spongeEvent = (ChangeBlockEvent.Pre) eventData.getSpongeEvent();
        final BlockEvent.BreakEvent forgeEvent = (BlockEvent.BreakEvent) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final net.minecraft.world.World world = forgeEvent.getWorld();
            if (world.isRemote) {
                return null;
            }

            final BlockPos pos = forgeEvent.getPos();
            /*final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final PhaseData data = phaseTracker.getCurrentPhaseData();

            User owner = data.context.getOwner().orElse(null);
            User notifier = data.context.getNotifier().orElse(null);
            EntityPlayer player = forgeEvent.getPlayer();

            if (SpongeImplHooks.isFakePlayer(player)) {
                frame.addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
            }

            if (owner != null) {
                frame.addContext(EventContextKeys.OWNER, owner);
            } else {
                frame.addContext(EventContextKeys.OWNER, (User) player);
            }
            if (notifier != null) {
                frame.addContext(EventContextKeys.NOTIFIER, notifier);
            }

            frame.addContext(EventContextKeys.PLAYER_BREAK, (World) world);*/

            return SpongeEventFactory.createChangeBlockEventPre(Sponge.getCauseStackManager().getCurrentCause(),
                ImmutableList.of(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ())));
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    private static ChangeBlockEvent.Place createOrPostChangeBlockEventPlace(ForgeEventData eventData) {
        final ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) eventData.getSpongeEvent();
        final BlockEvent.PlaceEvent forgeEvent = (BlockEvent.PlaceEvent) eventData.getForgeEvent();
        if (spongeEvent == null) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                final BlockPos pos = forgeEvent.getPos();
                final net.minecraft.world.World world = forgeEvent.getWorld();
                if (world.isRemote) {
                    return null;
                }
    
                final PhaseTracker phaseTracker = PhaseTracker.getInstance();
                final PhaseData data = phaseTracker.getCurrentPhaseData();
                BlockSnapshot originalSnapshot = ((IMixinBlockSnapshot) forgeEvent.getBlockSnapshot()).createSpongeBlockSnapshot();
                BlockSnapshot
                    finalSnapshot =
                    ((BlockState) forgeEvent.getPlacedBlock()).snapshotFor(new Location<>((World) world, VecHelper.toVector3d(pos)));
                ImmutableList<Transaction<BlockSnapshot>> blockSnapshots = new ImmutableList.Builder<Transaction<BlockSnapshot>>().add(
                    new Transaction<>(originalSnapshot, finalSnapshot)).build();
    
                User owner = data.context.getOwner().orElse(null);
                User notifier = data.context.getNotifier().orElse(null);
                EntityPlayer player = forgeEvent.getPlayer();
    
                if (SpongeImplHooks.isFakePlayer(player)) {
                    frame.addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
                }
    
                if (owner != null) {
                    frame.addContext(EventContextKeys.OWNER, owner);
                } else {
                    frame.addContext(EventContextKeys.OWNER, (User) player);
                }
                if (notifier != null) {
                    frame.addContext(EventContextKeys.NOTIFIER, notifier);
                }
    
                frame.addContext(EventContextKeys.PLAYER_PLACE, (World) world);
    
                return SpongeEventFactory.createChangeBlockEventPlace(frame.getCurrentCause(), blockSnapshots);
            }
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    private static ChangeBlockEvent.Place createOrPostChangeBlockEventPlaceMulti(ForgeEventData eventData) {
        final ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) eventData.getSpongeEvent();
        final BlockEvent.MultiPlaceEvent forgeEvent = (BlockEvent.MultiPlaceEvent) eventData.getForgeEvent();
        if (spongeEvent == null) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                final net.minecraft.world.World world = forgeEvent.getWorld();
                if (world.isRemote) {
                    return null;
                }
    
                ImmutableList.Builder<Transaction<BlockSnapshot>> builder = new ImmutableList.Builder<>();
                for (net.minecraftforge.common.util.BlockSnapshot blockSnapshot : forgeEvent.getReplacedBlockSnapshots()) {
                    final BlockPos snapshotPos = blockSnapshot.getPos();
                    BlockSnapshot originalSnapshot = ((IMixinBlockSnapshot) blockSnapshot).createSpongeBlockSnapshot();
                    BlockSnapshot finalSnapshot = ((World) world).createSnapshot(snapshotPos.getX(), snapshotPos.getY(), snapshotPos.getZ());
                    builder.add(new Transaction<>(originalSnapshot, finalSnapshot));
                }
    
                final PhaseTracker phaseTracker = PhaseTracker.getInstance();
                final PhaseData data = phaseTracker.getCurrentPhaseData();
                User owner = data.context.getOwner().orElse(null);
                User notifier = data.context.getNotifier().orElse(null);
                EntityPlayer player = forgeEvent.getPlayer();
    
                if (SpongeImplHooks.isFakePlayer(player)) {
                    frame.addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
                }
    
                if (owner != null) {
                    frame.addContext(EventContextKeys.OWNER, owner);
                } else {
                    frame.addContext(EventContextKeys.OWNER, (User) player);
                }
                if (notifier != null) {
                    frame.addContext(EventContextKeys.NOTIFIER, notifier);
                }
    
                return SpongeEventFactory.createChangeBlockEventPlace(frame.getCurrentCause(), builder.build());
            }
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    private static MessageChannelEvent.Chat createOrPostMessageChannelEventChat(ForgeEventData eventData) {
        final MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) eventData.getSpongeEvent();
        final ServerChatEvent forgeEvent = (ServerChatEvent) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final ITextComponent forgeComponent = forgeEvent.getComponent();
            final MessageFormatter formatter = new MessageFormatter();
            MessageChannel channel;
            Text[] chat = SpongeTexts.splitChatMessage((TextComponentTranslation) forgeComponent);
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
            Sponge.getCauseStackManager().pushCause(forgeEvent.getPlayer());
    
            return SpongeEventFactory.createMessageChannelEventChat(Sponge.getCauseStackManager().getCurrentCause(),
                originalChannel, Optional.ofNullable(channel), formatter, rawSpongeMessage, false);
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        final ITextComponent spongeComponent = SpongeTexts.toComponent(spongeEvent.getMessage());
        if (!spongeComponent.equals(forgeEvent.getComponent())) {
            forgeEvent.setComponent(spongeComponent);
        }

        return spongeEvent;
    }

    private static SleepingEvent.Pre createOrPostSleepingEventPre(ForgeEventData eventData) {
        final SleepingEvent.Pre spongeEvent = (SleepingEvent.Pre) eventData.getSpongeEvent();
        final PlayerSleepInBedEvent forgeEvent = (PlayerSleepInBedEvent) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final net.minecraft.world.World world = forgeEvent.getEntity().getEntityWorld();
            if (world.isRemote) {
                return null;
            }
    
            final BlockPos pos = forgeEvent.getPos();
            BlockSnapshot bedSnapshot = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            Sponge.getCauseStackManager().pushCause(forgeEvent.getEntity());
            return SpongeEventFactory.createSleepingEventPre(Sponge.getCauseStackManager().getCurrentCause(), bedSnapshot, (org.spongepowered.api.entity.Entity) forgeEvent.getEntity());
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    private static LoadChunkEvent createOrPostLoadChunkEvent(ForgeEventData eventData) {
        final LoadChunkEvent spongeEvent = (LoadChunkEvent) eventData.getSpongeEvent();
        final ChunkEvent.Load forgeEvent = (ChunkEvent.Load) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
            if (isMainThread) {
                Sponge.getCauseStackManager().pushCause(forgeEvent.getWorld());
            }
            final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
            return SpongeEventFactory.createLoadChunkEvent(cause, (Chunk) forgeEvent.getChunk());
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    private static UnloadChunkEvent createOrPostUnloadChunkEvent(ForgeEventData eventData) {
        final UnloadChunkEvent spongeEvent = (UnloadChunkEvent) eventData.getSpongeEvent();
        final ChunkEvent.Unload forgeEvent = (ChunkEvent.Unload) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
            if (isMainThread) {
                Sponge.getCauseStackManager().pushCause(forgeEvent.getWorld());
            }
            final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
            return SpongeEventFactory.createUnloadChunkEvent(cause, (Chunk) forgeEvent.getChunk());
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    private static InteractBlockEvent.Secondary createOrPostInteractBlockSecondaryEvent(ForgeEventData eventData) {
        final InteractBlockEvent.Secondary spongeEvent = (InteractBlockEvent.Secondary) eventData.getSpongeEvent();
        final PlayerInteractEvent.RightClickBlock forgeEvent = (PlayerInteractEvent.RightClickBlock) eventData.getForgeEvent();
        if (spongeEvent == null) {
            final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
            if (isMainThread) {
                Sponge.getCauseStackManager().pushCause(forgeEvent.getWorld());
            }
    
            final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
            final HandType hand = forgeEvent.getHand() == EnumHand.MAIN_HAND ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;
            final Tristate useBlockResult = getTristateFromResult(forgeEvent.getUseBlock());
            final Tristate useItemResult = getTristateFromResult(forgeEvent.getUseItem());
            final Vector3d interactionPoint = VecHelper.toVector3d(forgeEvent.getHitVec());
            final BlockPos pos = forgeEvent.getPos();
            final BlockSnapshot blockSnapshot = ((World) forgeEvent.getWorld()).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            final Direction direction = DirectionFacingProvider.getInstance().getKey(forgeEvent.getFace()).get();
            return SpongeEventFactory.createInteractBlockEventSecondaryMainHand(cause, useBlockResult, useBlockResult, useItemResult, useItemResult, hand, Optional.ofNullable(interactionPoint), blockSnapshot, direction);
        }

        ((SpongeModEventManager) Sponge.getEventManager()).postEvent(eventData);
        return spongeEvent;
    }

    // Special handler to process any events after ALL events have been posted to both Forge and Sponge
    public static void onPostEnd(ForgeEventData eventData) {
        if (eventData.getForgeEvent() instanceof ServerChatEvent) {
            final MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) eventData.getSpongeEvent();
            final ServerChatEvent forgeEvent = (ServerChatEvent) eventData.getForgeEvent();
            final EntityPlayerMP player = (EntityPlayerMP) forgeEvent.getPlayer();
            if (!spongeEvent.isCancelled()) {
                Text message = spongeEvent.getMessage();
                if (!spongeEvent.isMessageCancelled()) {
                    spongeEvent.getChannel().ifPresent(channel -> channel.send(forgeEvent.getPlayer(), message, ChatTypes.CHAT));
                }
            }

            // Chat spam suppression from MC
            int chatSpamThresholdCount = ((IMixinNetPlayHandler) player.connection).getChatSpamThresholdCount() + 20;
            ((IMixinNetPlayHandler) player.connection).setChatSpamThresholdCount(chatSpamThresholdCount);
            if (chatSpamThresholdCount > 200 && !SpongeImpl.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
                player.connection.disconnect(new TextComponentTranslation("disconnect.spam"));
            }
            // We set the forge component to null here to exit NetHandlerPlayServer#processChatMessage before it sends a message
            // This is done since we send our own message via the channel above
            forgeEvent.setComponent(null);
        }
    }
    // ====================================  FORGE TO SPONGE END ==================================== \\

    // ====================================  SPONGE TO FORGE START ==================================== \\
    // Used for firing Forge events after a Sponge event has been triggered
    static boolean createAndPostForgeEvent(SpongeEventData spongeEventData) {
        final Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz = spongeEventData.getForgeClass();
        if (EntityItemPickupEvent.class.isAssignableFrom(clazz)) {
            return createAndPostEntityItemPickupEvent(spongeEventData);
        } else if (PlayerInteractEvent.EntityInteract.class.isAssignableFrom(clazz)) {
            return createAndPostEntityInteractEvent(spongeEventData);
        } else if (BlockEvent.NeighborNotifyEvent.class.isAssignableFrom(clazz)) {
            return createAndPostNeighborNotifyEvent(spongeEventData);
        } else if (BlockEvent.PlaceEvent.class.isAssignableFrom(clazz)) {
            return createAndPostBlockPlaceEvent(spongeEventData);
        } else if (PlayerInteractEvent.class.isAssignableFrom(clazz)) {
            return createAndPostPlayerInteractEvent(spongeEventData);
        } else if (LivingDropsEvent.class.isAssignableFrom(clazz)) {
            if (spongeEventData.getSpongeEvent() instanceof DropItemEvent.Destruct) {
                final Object root = spongeEventData.getSpongeEvent().getSource();
                if (root instanceof Player) {
                    return createOrPostItemTossEvent(spongeEventData);
                }
            }
        } else if (ItemTossEvent.class.isAssignableFrom(clazz)) {
            return createAndPostLivingDropsEventEvent(spongeEventData);
        } else if (PlayerLoggedInEvent.class.isAssignableFrom(clazz)) {
            return createAndPostPlayerLoggedInEvent(spongeEventData);
        } else if (PlayerLoggedOutEvent.class.isAssignableFrom(clazz)) {
            return createAndPostPlayerLoggedOutEvent(spongeEventData);
        } else if (EntityTravelToDimensionEvent.class.isAssignableFrom(clazz)) {
            return createAndPostEntityTravelToDimensionEvent(spongeEventData);
        } else if (EntityJoinWorldEvent.class.isAssignableFrom(clazz)) {
            return createAndPostEntityJoinWorldEvent(spongeEventData);
        } else if (WorldEvent.Unload.class.isAssignableFrom(clazz)) {
            return createAndPostWorldUnloadEvent(spongeEventData);
        } else if (WorldEvent.Load.class.isAssignableFrom(clazz)) {
            return createAndPostWorldLoadEvent(spongeEventData);
        } else if (WorldEvent.Save.class.isAssignableFrom(clazz)) {
            return createAndPostWorldSaveEvent(spongeEventData);
        } else if (ChunkEvent.Load.class.isAssignableFrom(clazz)) {
            return createAndPostChunkLoadEvent(spongeEventData);
        } else if (ChunkEvent.Unload.class.isAssignableFrom(clazz)) {
            return createAndPostChunkUnloadEvent(spongeEventData);
        } else if (net.minecraftforge.event.world.ExplosionEvent.Start.class.isAssignableFrom(clazz)) {
            return createAndPostExplosionEventPre(spongeEventData);
        } else if (net.minecraftforge.event.world.ExplosionEvent.Detonate.class.isAssignableFrom(clazz)) {
            return createAndPostExplosionEventDetonate(spongeEventData);
        } else if (ItemFishedEvent.class.isAssignableFrom(clazz)) {
            return createAndPostItemFishedEvent(spongeEventData);
        } else if (LivingEntityUseItemEvent.class.isAssignableFrom(clazz)) {
            return createAndPostLivingUseItemEvent(spongeEventData);
        } else if (AdvancementEvent.class.isAssignableFrom(clazz)) {
            return createAndPostAdvancementGrantEvent(spongeEventData);
        }
        return false;
    }

    private static boolean createAndPostLivingUseItemEvent(SpongeEventData eventData) {
        final UseItemStackEvent spongeEvent = (UseItemStackEvent) eventData.getSpongeEvent();
        LivingEntityUseItemEvent forgeEvent = (LivingEntityUseItemEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final EntityLivingBase entity = spongeEvent.getCause().first(EntityLivingBase.class).orElse(null);
            if (entity == null) {
                return false;
            }
    
            final ItemStack stack = ItemStackUtil.toNative(spongeEvent.getItemStackInUse().createStack());
    
            if (spongeEvent instanceof UseItemStackEvent.Start) {
                forgeEvent = new LivingEntityUseItemEvent.Start(entity, stack, spongeEvent.getRemainingDuration());
            } else if (spongeEvent instanceof UseItemStackEvent.Tick) {
                forgeEvent = new LivingEntityUseItemEvent.Tick(entity, stack, spongeEvent.getRemainingDuration());
            } else if (spongeEvent instanceof UseItemStackEvent.Stop) {
                forgeEvent = new LivingEntityUseItemEvent.Stop(entity, stack, spongeEvent.getRemainingDuration());
            } else if (spongeEvent instanceof UseItemStackEvent.Replace) {
                forgeEvent = new LivingEntityUseItemEvent.Finish(entity, stack, spongeEvent.getRemainingDuration(), ItemStackUtil.toNative((
                        (UseItemStackEvent.Replace) spongeEvent).getItemStackResult().getFinal().createStack()));
            }
    
            if (forgeEvent == null) {
                return false;
            }
    
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        spongeEvent.setRemainingDuration(forgeEvent.getDuration());

        // Cancelling the Forge tick event stops all usage of the item, whereas
        // cancelling the Sponge tick event merely skips the logic for the current tick.
        if (forgeEvent.isCanceled()) {
            if (forgeEvent instanceof LivingEntityUseItemEvent.Tick) {
                spongeEvent.setRemainingDuration(-1);
            } else {
                ((Cancellable) spongeEvent).setCancelled(true);
            }
        }

        if (forgeEvent instanceof LivingEntityUseItemEvent.Finish) {
            ((UseItemStackEvent.Replace) spongeEvent).getItemStackResult().setCustom(ItemStackUtil.snapshotOf(((LivingEntityUseItemEvent.Finish) forgeEvent).getResultStack()));
        }

        return true;
    }

    private static boolean createAndPostAdvancementGrantEvent(SpongeEventData eventData) {
        final org.spongepowered.api.event.advancement.AdvancementEvent.Grant spongeEvent = (org.spongepowered.api.event.advancement.AdvancementEvent.Grant) eventData.getSpongeEvent();
        AdvancementEvent forgeEvent = (AdvancementEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new AdvancementEvent((EntityPlayer) spongeEvent.getTargetEntity(), (net.minecraft.advancements.Advancement) spongeEvent.getAdvancement());
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        return true;
    }


    // Bulk Event Handling
    private static boolean createOrPostItemTossEvent(SpongeEventData eventData) {
        final DropItemEvent.Destruct spongeEvent = (DropItemEvent.Destruct) eventData.getSpongeEvent();
        ItemTossEvent forgeEvent = (ItemTossEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final Cause cause = spongeEvent.getCause();
            final SpawnType spawnType = cause.getContext().get(EventContextKeys.SPAWN_TYPE).orElse(null);
            final EntityPlayerMP serverPlayer = (EntityPlayerMP) spongeEvent.getCause().root();
            if (spawnType == null || spawnType != SpawnTypes.DROPPED_ITEM || spongeEvent.getEntities().isEmpty()) {
                return false;
            }
    
            spongeEvent.filterEntities(e -> (e instanceof EntityItem) && !((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new ItemTossEvent(
                    (EntityItem) e, serverPlayer), true));
    
            createAndPostEntityJoinWorldEvent(eventData);
            //handleCustomStack((SpawnEntityEvent) event);
            return true;
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostPlayerInteractEvent(SpongeEventData eventData) {
        final InteractBlockEvent spongeEvent = (InteractBlockEvent) eventData.getSpongeEvent();
        PlayerInteractEvent forgeEvent = (PlayerInteractEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            Player player = spongeEvent.getCause().first(Player.class).orElse(null);
            // Forge doesn't support left-click AIR
            if (player == null || (spongeEvent instanceof InteractBlockEvent.Primary && spongeEvent.getTargetBlock() == BlockSnapshot.NONE)) {
                return false;
            }
    
            BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTargetBlock().getPosition());
            EnumFacing face = DirectionFacingProvider.getInstance().get(spongeEvent.getTargetSide()).orElse(null);
            Vec3d hitVec = null;
            final EntityPlayerMP entityPlayerMP = EntityUtil.toNative(player);
            if (spongeEvent.getInteractionPoint().isPresent()) {
                hitVec = VecHelper.toVec3d(spongeEvent.getInteractionPoint().get());
            }
            if (spongeEvent instanceof InteractBlockEvent.Primary) {
                forgeEvent = new PlayerInteractEvent.LeftClickBlock(entityPlayerMP, pos, face, hitVec);
            } else if (face != null && spongeEvent instanceof InteractBlockEvent.Secondary) {
                EnumHand hand = spongeEvent instanceof InteractBlockEvent.Secondary.MainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                forgeEvent = new PlayerInteractEvent.RightClickBlock(entityPlayerMP, hand, pos, face, hitVec);
            }

            if (forgeEvent == null) {
                return false;
            }
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        if (forgeEvent instanceof PlayerInteractEvent.RightClickBlock) {
            PlayerInteractEvent.RightClickBlock event = (PlayerInteractEvent.RightClickBlock) forgeEvent;
            // Mods have higher priority
            if (event.getUseItem() != Result.DEFAULT) {
                ((InteractBlockEvent.Secondary) spongeEvent).setUseItemResult(getTristateFromResult(event.getUseItem()));
            }
            if (event.getUseBlock() != Result.DEFAULT) {
                ((InteractBlockEvent.Secondary) spongeEvent).setUseBlockResult(getTristateFromResult(event.getUseBlock()));
            }
        }

        return true;
    }

    private static boolean createAndPostEntityItemPickupEvent(SpongeEventData eventData) {
        final ChangeInventoryEvent.Pickup.Pre spongeEvent = (ChangeInventoryEvent.Pickup.Pre) eventData.getSpongeEvent();
        EntityItemPickupEvent forgeEvent = (EntityItemPickupEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            // Sponge -> Forge
            if (spongeEvent.getTargetEntity() instanceof EntityItem) {
                final EntityItem entityItem = (EntityItem) spongeEvent.getTargetEntity();
                final Player player = spongeEvent.getCause().first(Player.class).orElse(null);
                if (player != null) {
                    forgeEvent = new EntityItemPickupEvent((EntityPlayer) player, entityItem);
                    eventData.setForgeEvent(forgeEvent);
                }
            }
            if (forgeEvent == null) {
                return false;
            }
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostLivingDropsEventEvent(SpongeEventData eventData) {
        final AffectEntityEvent spongeEvent = (AffectEntityEvent) eventData.getSpongeEvent();
        LivingDropsEvent forgeEvent = (LivingDropsEvent) eventData.getForgeEvent();
        final Cause cause = spongeEvent.getCause();
        //final SpawnType spawnType = cause.getContext().get(EventContextKeys.SPAWN_TYPE).orElse(null);
        final EntityLivingBase living = cause.first(EntityLivingBase.class).orElse(null);

        if (living != null && spongeEvent instanceof DropItemEvent.Destruct) {
            final DropItemEvent.Destruct destruct = (DropItemEvent.Destruct) spongeEvent;

            final net.minecraft.util.DamageSource damageSource = (net.minecraft.util.DamageSource) cause.first(DamageSource.class).orElse(null);

            if (damageSource != null) {

                final List<EntityItem> items = destruct.getEntities()
                  .stream()
                  .filter(e -> e instanceof EntityItem)
                  .map(e -> (EntityItem) e)
                  .collect(Collectors.toList());

                if (forgeEvent == null) {
                    if (living instanceof EntityPlayerMP) {
                        final EntityPlayerMP serverPlayer = (EntityPlayerMP) living;
    
                        forgeEvent = new PlayerDropsEvent(serverPlayer, damageSource, new ArrayList<>(items), ((IMixinEntityLivingBase)
                          serverPlayer).getRecentlyHit() > 0);
                    } else {
                        forgeEvent = new LivingDropsEvent(living, damageSource, new ArrayList<>(items), net.minecraftforge.common.ForgeHooks
                          .getLootingLevel(living, damageSource.getTrueSource(), damageSource),
                          ((IMixinEntityLivingBase) living).getRecentlyHit() > 0);
                    }
                    eventData.setForgeEvent(forgeEvent);
                }
                ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                if (forgeEvent.isCanceled()) {
                    spongeEvent.setCancelled(true);
                    return true;
                } else {
                    // Re-sync entity list from forge to sponge
                    spongeEvent.getEntities().removeAll(items);
                    spongeEvent.getEntities().addAll(forgeEvent.getDrops().stream().map(EntityUtil::fromNative).collect(Collectors.toList()));
                }
            }
        }

        return createAndPostEntityJoinWorldEvent(eventData);
    }

    private static boolean createAndPostEntityJoinWorldEvent(SpongeEventData eventData) {
        SpawnEntityEvent spongeEvent = (SpawnEntityEvent) eventData.getSpongeEvent();
        ListIterator<org.spongepowered.api.entity.Entity> iterator = spongeEvent.getEntities().listIterator();
        if (spongeEvent.getEntities().isEmpty()) {
            if (eventData.getForgeEvent() != null) {
                return true;
            }
            return false;
        }

        // used to avoid player item restores when set to dead
        boolean canCancelEvent = true;

        while (iterator.hasNext()) {
            org.spongepowered.api.entity.Entity entity = iterator.next();
            EntityJoinWorldEvent forgeEvent = new EntityJoinWorldEvent((Entity) entity,
                    (net.minecraft.world.World) entity.getLocation().getExtent());

            ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
            Entity mcEntity = (Entity) entity;
            if (mcEntity.isDead) {
                // Don't restore packet item if a mod wants it dead
                // Mods such as Flux-Networks kills the entity item to spawn a custom one
                canCancelEvent = false;
            }
            if (forgeEvent.isCanceled()) {
                iterator.remove();
            }
        }
        if (spongeEvent.getEntities().isEmpty() && canCancelEvent) {
            spongeEvent.setCancelled(true);
        }
        return true;
    }

    static void handlePrefireLogic(Event event) {
        if (event instanceof SpawnEntityEvent) {
            handleCustomStack((SpawnEntityEvent) event);
        }
    }

    // Copied from ForgeInternalHandler.onEntityJoinWorld, but with modifications
    @SuppressWarnings("deprecation")
    private static void handleCustomStack(SpawnEntityEvent event) {
        // Sponge start - iterate over entities
        ListIterator<org.spongepowered.api.entity.Entity> it = event.getEntities().listIterator();
        while (it.hasNext()) {
            Entity entity = (Entity) it.next(); //Sponge - use entity from event
            if (entity instanceof EntityItem) {
                final ItemStack stack =  EntityUtil.getItem(entity);
                final Item item = stack.getItem();

                if (item.hasCustomEntity(stack)) {
                    final Entity newEntity = item.createEntity(entity.getEntityWorld(), entity, stack); // Sponge - use world from entity
                    if (newEntity != null) {
                        entity.setDead();

                        final EntityJoinWorldEvent cancelledEvent = new EntityJoinWorldEvent(entity, entity.getEntityWorld());
                        cancelledEvent.setCanceled(true);
                        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(cancelledEvent, true);

                        if (!cancelledEvent.isCanceled()) {
                            SpongeImpl.getLogger()
                                    .error("A mod has un-cancelled the EntityJoinWorld event for the original EntityItem (from before Item#createEntity is called). This is almost certainly a terrible idea!");
                        }

                        it.set((org.spongepowered.api.entity.Entity) newEntity);
                        // Sponge end
                    }
                }
            }
        }
    }

    private static boolean createAndPostNeighborNotifyEvent(SpongeEventData eventData) {
        final NotifyNeighborBlockEvent spongeEvent = (NotifyNeighborBlockEvent) eventData.getSpongeEvent();
        NeighborNotifyEvent forgeEvent = (NeighborNotifyEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            LocatableBlock locatableBlock = spongeEvent.getCause().first(LocatableBlock.class).orElse(null);
            TileEntity tileEntitySource = spongeEvent.getCause().first(TileEntity.class).orElse(null);
            Location<World> sourceLocation;
            IBlockState state;
            if (locatableBlock != null) {
                sourceLocation = locatableBlock.getLocation();
                state = (IBlockState) locatableBlock.getBlockState();
            } else if (tileEntitySource != null) {
                sourceLocation = tileEntitySource.getLocation();
                state = (IBlockState) sourceLocation.getBlock();
            } else { // should never happen but just in case it does
                return false;
            }
    
            EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
            for (Map.Entry<Direction, BlockState> mapEntry : spongeEvent.getNeighbors().entrySet()) {
                if (mapEntry.getKey() != Direction.NONE) {
                    facings.add(DirectionFacingProvider.getInstance().get(mapEntry.getKey()).get());
                }
            }
    
            if (facings.isEmpty()) {
                return false;
            }
    
            BlockPos pos = VecHelper.toBlockPos(sourceLocation);
            net.minecraft.world.World world = (net.minecraft.world.World) sourceLocation.getExtent();
            // TODO - the boolean forced redstone bit needs to be set properly
            forgeEvent = new NeighborNotifyEvent(world, pos, state, facings, false);
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostBlockPlaceEvent(SpongeEventData eventData) {
        final ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) eventData.getSpongeEvent();
        BlockEvent.PlaceEvent forgeEvent = (BlockEvent.PlaceEvent) eventData.getForgeEvent();
        if (!(spongeEvent.getCause().root() instanceof Player)) {
            return false;
        }

        if (forgeEvent == null) {
            EntityPlayer player = (EntityPlayer) spongeEvent.getCause().root();
            net.minecraft.world.World world = player.world;
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final PhaseContext<?> currentContext = phaseTracker.getCurrentContext();
            PhaseContext<?> target = currentContext;
            if (currentContext instanceof UnwindingPhaseContext) {
                target = ((UnwindingPhaseContext) currentContext).getUnwindingContext();
            }
            PacketContext<?> context = target instanceof PacketContext<?> ? (PacketContext<?>) target : null;
            Packet<?> contextPacket = context != null ? context.getPacket(): null;
            if (contextPacket == null) {
                return false;
            }
    
            if (spongeEvent.getTransactions().size() == 1) {
                BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTransactions().get(0).getOriginal().getPosition());
                IBlockState state = (IBlockState) spongeEvent.getTransactions().get(0).getOriginal().getState();
                net.minecraftforge.common.util.BlockSnapshot blockSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
                IBlockState placedAgainst = Blocks.AIR.getDefaultState();
                EnumHand hand = EnumHand.MAIN_HAND;
                if (contextPacket instanceof CPacketPlayerTryUseItemOnBlock) {
                    CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) contextPacket;
                    EnumFacing facing = packet.getDirection();
                    placedAgainst = blockSnapshot.getWorld().getBlockState(blockSnapshot.getPos().offset(facing.getOpposite()));
                    hand = packet.getHand();
                }
    
                forgeEvent = new BlockEvent.PlaceEvent(blockSnapshot, placedAgainst, player, hand);
                eventData.setForgeEvent(forgeEvent);
            } else { // multi
                Iterator<Transaction<BlockSnapshot>> iterator = spongeEvent.getTransactions().iterator();
                List<net.minecraftforge.common.util.BlockSnapshot> blockSnapshots = new ArrayList<>();
    
                while (iterator.hasNext()) {
                    Transaction<BlockSnapshot> transaction = iterator.next();
                    Location<World> location = transaction.getOriginal().getLocation().get();
                    IBlockState state = (IBlockState) transaction.getOriginal().getState();
                    BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    net.minecraftforge.common.util.BlockSnapshot blockSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
                    blockSnapshots.add(blockSnapshot);
                }
    
                IBlockState placedAgainst = Blocks.AIR.getDefaultState();
                EnumHand hand = EnumHand.MAIN_HAND;
                if (contextPacket instanceof CPacketPlayerTryUseItemOnBlock) {
                    CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) contextPacket;
                    EnumFacing facing = packet.getDirection();
                    placedAgainst = blockSnapshots.get(0).getWorld().getBlockState(blockSnapshots.get(0).getPos().offset(facing.getOpposite()));
                    hand = packet.getHand();
                }
    
                forgeEvent = new BlockEvent.MultiPlaceEvent(blockSnapshots, placedAgainst, player, hand);
                eventData.setForgeEvent(forgeEvent);
            }
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostExplosionEventPre(SpongeEventData eventData) {
        final ExplosionEvent.Pre spongeEvent = (ExplosionEvent.Pre) eventData.getSpongeEvent();
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Start) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Start(
                    ((net.minecraft.world.World) spongeEvent.getTargetWorld()), ((Explosion) ((ExplosionEvent.Pre) spongeEvent).getExplosion()));
            eventData.setForgeEvent(forgeEvent);
        }
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static boolean createAndPostExplosionEventDetonate(SpongeEventData eventData) {
        final ExplosionEvent.Detonate spongeEvent = (ExplosionEvent.Detonate) eventData.getSpongeEvent();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Detonate) eventData.getForgeEvent();

        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        if (explosion == null) {
            return false;
        }

        if (forgeEvent == null) {
            forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Detonate(
                    (net.minecraft.world.World) spongeEvent.getTargetWorld(), explosion,
                    (List<Entity>) (List<?>) spongeEvent.getEntities());
            explosion.getAffectedBlockPositions().clear();
            for (Location<World> x : spongeEvent.getAffectedLocations()) {
                explosion.getAffectedBlockPositions().add(VecHelper.toBlockPos(x.getPosition()));
            }
        }

        if (!forgeEvent.getExplosion().damagesTerrain) {
            List<BlockPos> affectedBlocks = forgeEvent.getExplosion().getAffectedBlockPositions();
            affectedBlocks.clear();
        }
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        ((ExplosionEvent.Detonate) spongeEvent).getAffectedLocations().clear();
        for (BlockPos pos : forgeEvent.getAffectedBlocks()) {
            ((ExplosionEvent.Detonate) spongeEvent).getAffectedLocations().add(new Location<>(spongeEvent.getTargetWorld(), VecHelper.toVector3i(pos)));
        }
        return true;
    }

    private static boolean createAndPostEntityInteractEvent(SpongeEventData eventData) {
        final InteractEntityEvent.Secondary spongeEvent = (InteractEntityEvent.Secondary) eventData.getSpongeEvent();
        PlayerInteractEvent forgeEvent = (PlayerInteractEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            Optional<Player> player = spongeEvent.getCause().first(Player.class);
            if (!player.isPresent()) {
                return false;
            }
            final EntityPlayerMP entityPlayerMP = EntityUtil.toNative(player.get());
            final EnumHand hand = entityPlayerMP.getActiveHand();
    
            final EntityPlayer entityPlayer = (EntityPlayer) player.get();
            final Entity entity = (Entity) spongeEvent.getTargetEntity();
            final Vector3d hitVec = spongeEvent.getInteractionPoint().orElse(null);
    
            if (hitVec != null) {
                forgeEvent = new PlayerInteractEvent.EntityInteractSpecific(entityPlayer, hand, entity, VecHelper.toVec3d(hitVec));
            } else {
                forgeEvent = new PlayerInteractEvent.EntityInteract(entityPlayer, hand, entity);
            }
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostPlayerLoggedInEvent(SpongeEventData eventData) {
        final ClientConnectionEvent.Join spongeEvent = (ClientConnectionEvent.Join) eventData.getSpongeEvent();
        PlayerLoggedInEvent forgeEvent = (PlayerLoggedInEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new PlayerLoggedInEvent((EntityPlayer) spongeEvent.getTargetEntity());
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostPlayerLoggedOutEvent(SpongeEventData eventData) {
        final ClientConnectionEvent.Disconnect spongeEvent = (ClientConnectionEvent.Disconnect) eventData.getSpongeEvent();
        PlayerLoggedOutEvent forgeEvent = (PlayerLoggedOutEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new PlayerLoggedOutEvent((EntityPlayer) spongeEvent.getTargetEntity());
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    private static boolean createAndPostEntityTravelToDimensionEvent(SpongeEventData eventData) {
        final MoveEntityEvent.Teleport spongeEvent = (MoveEntityEvent.Teleport) eventData.getSpongeEvent();
        EntityTravelToDimensionEvent forgeEvent = (EntityTravelToDimensionEvent) eventData.getForgeEvent();
        org.spongepowered.api.entity.Entity entity = spongeEvent.getTargetEntity();
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }

        if (forgeEvent == null) {
            int fromDimensionId = ((net.minecraft.world.World) spongeEvent.getFromTransform().getExtent()).provider.getDimension();
            int toDimensionId = ((net.minecraft.world.World) spongeEvent.getToTransform().getExtent()).provider.getDimension();
            if (fromDimensionId == toDimensionId) {
                return false;
            }
    
            // Copied from net.minecraftforge.common.ForgeHooks.onTravelToDimension
            forgeEvent = new EntityTravelToDimensionEvent((EntityPlayerMP) entity, toDimensionId);
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        if (forgeEvent.isCanceled())
        {
            // Revert variable back to true as it would have been set to false
            if (entity instanceof EntityMinecartContainer)
            {
               ((EntityMinecartContainer) entity).dropContentsWhenDead = true;
            }
            spongeEvent.setCancelled(true);
        }

        return true;
    }

    private static boolean createAndPostWorldSaveEvent(SpongeEventData eventData) {
        final SaveWorldEvent spongeEvent = (SaveWorldEvent) eventData.getSpongeEvent();
        WorldEvent.Save forgeEvent = (WorldEvent.Save) eventData.getForgeEvent();
        // Since Forge only uses a single save handler, we need to make sure to pass the overworld's handler.
        // This makes sure that mods dont attempt to save/read their data from the wrong location.
        ((IMixinWorld) spongeEvent.getTargetWorld()).setCallingWorldEvent(true);
        if (forgeEvent == null) {
            forgeEvent = new WorldEvent.Save((net.minecraft.world.World) spongeEvent.getTargetWorld());
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        ((IMixinWorld) spongeEvent.getTargetWorld()).setCallingWorldEvent(false);
        return true;
    }

    private static boolean createAndPostWorldLoadEvent(SpongeEventData eventData) {
        final LoadWorldEvent spongeEvent = (LoadWorldEvent) eventData.getSpongeEvent();
        WorldEvent.Load forgeEvent = (WorldEvent.Load) eventData.getForgeEvent();
        // Since Forge only uses a single save handler, we need to make sure to pass the overworld's handler.
        // This makes sure that mods dont attempt to save/read their data from the wrong location.
        final net.minecraft.world.World minecraftWorld = (net.minecraft.world.World) spongeEvent.getTargetWorld();
        ((IMixinWorld) spongeEvent.getTargetWorld()).setCallingWorldEvent(true);
        ((IMixinChunkProviderServer) minecraftWorld.getChunkProvider()).setForceChunkRequests(true);
        if (forgeEvent == null) {
            forgeEvent = new WorldEvent.Load(minecraftWorld);
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        ((IMixinChunkProviderServer) minecraftWorld.getChunkProvider()).setForceChunkRequests(false);
        ((IMixinWorld) minecraftWorld).setCallingWorldEvent(false);
        return true;
    }

    private static boolean createAndPostWorldUnloadEvent(SpongeEventData eventData) {
        final UnloadWorldEvent spongeEvent = (UnloadWorldEvent) eventData.getSpongeEvent();
        WorldEvent.Unload forgeEvent = (WorldEvent.Unload) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new WorldEvent.Unload((net.minecraft.world.World) spongeEvent.getTargetWorld());
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        return true;
    }

    private static boolean createAndPostChunkLoadEvent(SpongeEventData eventData) {
        final LoadChunkEvent spongeEvent = (LoadChunkEvent) eventData.getSpongeEvent();
        ChunkEvent.Load forgeEvent = (ChunkEvent.Load) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk();
            forgeEvent = new ChunkEvent.Load(chunk);
            eventData.setForgeEvent(forgeEvent);
        }
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        return true;
    }

    private static boolean createAndPostChunkUnloadEvent(SpongeEventData eventData) {
        final UnloadChunkEvent spongeEvent = (UnloadChunkEvent) eventData.getSpongeEvent();
        ChunkEvent.Unload forgeEvent = (ChunkEvent.Unload) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk();
            forgeEvent = new ChunkEvent.Unload(chunk);
            eventData.setForgeEvent(forgeEvent);
        }
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        return true;
    }

    private static boolean createAndPostItemFishedEvent(SpongeEventData eventData) {
        final FishingEvent.Stop spongeEvent = (FishingEvent.Stop) eventData.getSpongeEvent();
        ItemFishedEvent forgeEvent = (ItemFishedEvent) eventData.getForgeEvent();
        final List<Transaction<ItemStackSnapshot>> potentialFishies = spongeEvent.getTransactions();
        if (potentialFishies.isEmpty()) {
            return false;
        }

        if (forgeEvent == null) {
            final List<ItemStack> itemstacks = spongeEvent.getTransactions()
              .stream()
              .filter(Transaction::isValid)
              .map(transaction -> transaction.getFinal().createStack())
              .map(ItemStackUtil::toNative)
              .collect(Collectors.toList());
    
            if (itemstacks.isEmpty()) {
                return false;
            }
    
            forgeEvent = new ItemFishedEvent(itemstacks, 0, (EntityFishHook) spongeEvent.getFishHook());
            eventData.setForgeEvent(forgeEvent);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(eventData);
        return true;
    }

    // Special handler to process any events after ALL events have been posted to both Forge and Sponge
    public static void onPostEnd(SpongeEventData eventData) {
        
    }
    // ====================================  SPONGE TO FORGE END ==================================== \\
}
