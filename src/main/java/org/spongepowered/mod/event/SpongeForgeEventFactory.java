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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.entity.item.TargetItemEvent;
import org.spongepowered.api.event.entity.living.TargetLivingEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
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
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
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
import org.spongepowered.common.interfaces.IMixinInitCause;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinBlockSnapshot;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public class SpongeForgeEventFactory {

    // Order matters
    public static Class<? extends net.minecraftforge.fml.common.eventhandler.Event> getForgeEventClass(Event spongeEvent) {
        final Class<? extends Event> clazz = spongeEvent.getClass();
        if (ChangeInventoryEvent.Pickup.Pre.class.isAssignableFrom(clazz)) {
            return EntityItemPickupEvent.class;
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
        if (ClientConnectionEvent.Join.class.isAssignableFrom(clazz)) {
            return PlayerLoggedInEvent.class;
        }
        if (ClientConnectionEvent.Disconnect.class.isAssignableFrom(clazz)) {
            return PlayerLoggedOutEvent.class;
        }
        if (RespawnPlayerEvent.class.isAssignableFrom(clazz)) {
            return PlayerRespawnEvent.class;
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
        return null;
    }

    public static EventBus getForgeEventBus(Class<?> clazz) {
        if (OreGenEvent.class.isAssignableFrom(clazz)) {
            return MinecraftForge.ORE_GEN_BUS;
        } else if (WorldTypeEvent.class.isAssignableFrom(clazz)
                || BiomeEvent.class.isAssignableFrom(clazz)
                || DecorateBiomeEvent.class.isAssignableFrom(clazz)
                || InitMapGenEvent.class.isAssignableFrom(clazz)
                || InitNoiseGensEvent.class.isAssignableFrom(clazz)
                || PopulateChunkEvent.class.isAssignableFrom(clazz)
                || SaplingGrowTreeEvent.class.isAssignableFrom(clazz)) {
            return MinecraftForge.TERRAIN_GEN_BUS;
        }

        return MinecraftForge.EVENT_BUS;
    }

    // ====================================  FORGE TO SPONGE START ==================================== \\
    public static Event createSpongeEvent(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        if (forgeEvent instanceof BlockEvent.MultiPlaceEvent) {
            return createChangeBlockEventPlace((BlockEvent.MultiPlaceEvent) forgeEvent);
        }
        if (forgeEvent instanceof BlockEvent.PlaceEvent) {
            return createChangeBlockEventPlace((BlockEvent.PlaceEvent) forgeEvent);
        }
        if (forgeEvent instanceof BlockEvent.BreakEvent) {
            return createChangeBlockEventPre((BlockEvent.BreakEvent) forgeEvent);
        }
        if (forgeEvent instanceof ServerChatEvent) {
            return createMessageChannelEventChat((ServerChatEvent) forgeEvent);
        }
        if (forgeEvent instanceof PlayerSleepInBedEvent) {
            return createSleepingEventPre((PlayerSleepInBedEvent) forgeEvent);
        }
        if (forgeEvent instanceof ChunkEvent.Load) {
            return createLoadChunkEvent((ChunkEvent.Load) forgeEvent);
        }
        if (forgeEvent instanceof ChunkEvent.Unload) {
            return createUnloadChunkEvent((ChunkEvent.Unload) forgeEvent);
        }
        return null;
    }

    public static ChangeBlockEvent.Pre createChangeBlockEventPre(BlockEvent.BreakEvent forgeEvent) {
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        final BlockPos pos = forgeEvent.getPos();
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData data = phaseTracker.getCurrentPhaseData();

        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        EntityPlayer player = forgeEvent.getPlayer();

        if (SpongeImplHooks.isFakePlayer(player)) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
        } else {
            Sponge.getCauseStackManager().pushCause(player);
        }

        if (owner != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(owner);
            }
        } else {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, (User) player);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(player);
            }
        }
        if (notifier != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
        }

        Sponge.getCauseStackManager().addContext(EventContextKeys.PLAYER_BREAK, (World) world);

        return SpongeEventFactory.createChangeBlockEventPre(Sponge.getCauseStackManager().getCurrentCause(),
            ImmutableList.of(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ())));
    }

    public static ChangeBlockEvent.Break createChangeBlockEventBreak(BlockEvent.BreakEvent forgeEvent) {
        final BlockPos pos = forgeEvent.getPos();
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData data = phaseTracker.getCurrentPhaseData();
        BlockSnapshot originalSnapshot = ((World) forgeEvent.getWorld()).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        BlockSnapshot finalSnapshot = BlockTypes.AIR.getDefaultState().snapshotFor(new Location<>((World) world, VecHelper.toVector3d(pos)));
        ImmutableList<Transaction<BlockSnapshot>> blockSnapshots = new ImmutableList.Builder<Transaction<BlockSnapshot>>().add(
                new Transaction<>(originalSnapshot, finalSnapshot)).build();

        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        EntityPlayer player = forgeEvent.getPlayer();

        if (SpongeImplHooks.isFakePlayer(player)) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
        } else if (Sponge.getCauseStackManager().getCurrentCause() == null) {
            Sponge.getCauseStackManager().pushCause(player);
        }

        if (owner != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(owner);
            }
        } else {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, (User) player);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(player);
            }
        }
        if (notifier != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
        }

        Sponge.getCauseStackManager().addContext(EventContextKeys.PLAYER_BREAK, (World) world);

        return SpongeEventFactory.createChangeBlockEventBreak(Sponge.getCauseStackManager().getCurrentCause(), blockSnapshots);
    }

    public static ChangeBlockEvent.Place createChangeBlockEventPlace(BlockEvent.PlaceEvent forgeEvent) {
        final BlockPos pos = forgeEvent.getPos();
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData data = phaseTracker.getCurrentPhaseData();
        BlockSnapshot originalSnapshot = ((IMixinBlockSnapshot) forgeEvent.getBlockSnapshot()).createSpongeBlockSnapshot();
        BlockSnapshot finalSnapshot = ((BlockState) forgeEvent.getPlacedBlock()).snapshotFor(new Location<>((World) world, VecHelper.toVector3d(pos)));
        ImmutableList<Transaction<BlockSnapshot>> blockSnapshots = new ImmutableList.Builder<Transaction<BlockSnapshot>>().add(
                new Transaction<>(originalSnapshot, finalSnapshot)).build();

        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        EntityPlayer player = forgeEvent.getPlayer();

        if (SpongeImplHooks.isFakePlayer(player)) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
        } else if (Sponge.getCauseStackManager().getCurrentCause() == null) {
            Sponge.getCauseStackManager().pushCause(player);
        }

        if (owner != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(owner);
            }
        } else {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, (User) player);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(player);
            }
        }
        if (notifier != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
        }

        Sponge.getCauseStackManager().addContext(EventContextKeys.PLAYER_PLACE, (World) world);

        return SpongeEventFactory.createChangeBlockEventPlace(Sponge.getCauseStackManager().getCurrentCause(), blockSnapshots);
    }

    public static ChangeBlockEvent.Place createChangeBlockEventPlace(BlockEvent.MultiPlaceEvent forgeEvent) {
        final net.minecraft.world.World world = forgeEvent.getWorld();
        if (world.isRemote) {
            return null;
        }

        ImmutableList.Builder<Transaction<BlockSnapshot>> builder = new ImmutableList.Builder<Transaction<BlockSnapshot>>();
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
            Sponge.getCauseStackManager().addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
        } else if (Sponge.getCauseStackManager().getCurrentCause() == null) {
            Sponge.getCauseStackManager().pushCause(player);
        }

        if (owner != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(owner);
            }
        } else {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, (User) player);
            if (Sponge.getCauseStackManager().getCurrentCause() == null) {
                Sponge.getCauseStackManager().pushCause(player);
            }
        }
        if (notifier != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
        }

        return SpongeEventFactory.createChangeBlockEventPlace(Sponge.getCauseStackManager().getCurrentCause(), builder.build());
    }

    public static MessageChannelEvent.Chat createMessageChannelEventChat(ServerChatEvent forgeEvent) {
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

    public static SleepingEvent.Pre createSleepingEventPre(PlayerSleepInBedEvent forgeEvent) {
        final net.minecraft.world.World world = forgeEvent.getEntity().getEntityWorld();
        if (world.isRemote) {
            return null;
        }

        final BlockPos pos = forgeEvent.getPos();
        BlockSnapshot bedSnapshot = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        Sponge.getCauseStackManager().pushCause(forgeEvent.getEntity());
        return SpongeEventFactory.createSleepingEventPre(Sponge.getCauseStackManager().getCurrentCause(), bedSnapshot, (org.spongepowered.api.entity.Entity) forgeEvent.getEntity());
    }

    public static LoadChunkEvent createLoadChunkEvent(ChunkEvent.Load forgeEvent) {
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        if (isMainThread) {
            Sponge.getCauseStackManager().pushCause(forgeEvent.getWorld());
        }
        final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
        return SpongeEventFactory.createLoadChunkEvent(cause, (Chunk) forgeEvent.getChunk());
    }

    public static UnloadChunkEvent createUnloadChunkEvent(ChunkEvent.Unload forgeEvent) {
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        if (isMainThread) {
            Sponge.getCauseStackManager().pushCause(forgeEvent.getWorld());
        }
        final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), forgeEvent.getWorld());
        return SpongeEventFactory.createUnloadChunkEvent(cause, (Chunk) forgeEvent.getChunk());
    }
    // ====================================  FORGE TO SPONGE END ==================================== \\

    // ====================================  SPONGE TO FORGE START ==================================== \\
    // Used for firing Forge events after a Sponge event has been triggered
    public static Event callForgeEvent(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz) {
        if (EntityItemPickupEvent.class.isAssignableFrom(clazz)) {
            return callEntityItemPickupEvent(spongeEvent);
        } else if (PlayerInteractEvent.EntityInteractSpecific.class.isAssignableFrom(clazz)) {
            return callEntityInteractEvent(spongeEvent);
        } else if (PlayerInteractEvent.EntityInteract.class.isAssignableFrom(clazz)) {
            return callEntityInteractEvent(spongeEvent);
        } else if (BlockEvent.NeighborNotifyEvent.class.isAssignableFrom(clazz)) {
            return callNeighborNotifyEvent(spongeEvent);
        } else if (BlockEvent.PlaceEvent.class.isAssignableFrom(clazz)) {
            return callBlockPlaceEvent(spongeEvent);
        } else if (PlayerInteractEvent.class.isAssignableFrom(clazz)) {
            return createPlayerInteractEvent(spongeEvent);
        } else if (LivingDropsEvent.class.isAssignableFrom(clazz)) {
            return callLivingDropsEvent(spongeEvent);
        } else if (ItemTossEvent.class.isAssignableFrom(clazz)) {
            return callItemTossEvent(spongeEvent);
        } else if (PlayerLoggedInEvent.class.isAssignableFrom(clazz)) {
            return callPlayerLoggedInEvent(spongeEvent);
        } else if (PlayerLoggedOutEvent.class.isAssignableFrom(clazz)) {
            return callPlayerLoggedOutEvent(spongeEvent);
        } else if (PlayerRespawnEvent.class.isAssignableFrom(clazz)) {
            return callPlayerRespawnEvent(spongeEvent);
        } else if (EntityTravelToDimensionEvent.class.isAssignableFrom(clazz)) {
            return callEntityTravelToDimensionEvent(spongeEvent);
        } else if (EntityJoinWorldEvent.class.isAssignableFrom(clazz)) {
            return callEntityJoinWorldEvent(spongeEvent);
        } else if (WorldEvent.Unload.class.isAssignableFrom(clazz)) {
            return callWorldUnloadEvent(spongeEvent);
        } else if (WorldEvent.Load.class.isAssignableFrom(clazz)) {
            return callWorldLoadEvent(spongeEvent);
        } else if (WorldEvent.Save.class.isAssignableFrom(clazz)) {
            return callWorldSaveEvent(spongeEvent);
        } else if (ChunkEvent.Load.class.isAssignableFrom(clazz)) {
            return callChunkLoadEvent(spongeEvent);
        } else if (ChunkEvent.Unload.class.isAssignableFrom(clazz)) {
            return callChunkUnloadEvent(spongeEvent);
        } else if (net.minecraftforge.event.world.ExplosionEvent.Start.class.isAssignableFrom(clazz)) {
            return callExplosionEventPre(spongeEvent);
        } else if (net.minecraftforge.event.world.ExplosionEvent.Detonate.class.isAssignableFrom(clazz)) {
            return callExplosionEventDetonate(spongeEvent);
        }
        return spongeEvent;
    }

    private static LivingDropsEvent createLivingDropItemEvent(Event event) {
        DropItemEvent.Destruct spongeEvent = (DropItemEvent.Destruct) event;
        Optional<EntityLivingBase> spawnCause = spongeEvent.getCause().first(EntityLivingBase.class);
        if (!spawnCause.isPresent()) {
            return null;
        }
        Optional<DamageSource> source = spongeEvent.getCause().first(DamageSource.class);
        if (!source.isPresent()) {
            return null;
        }

        List<EntityItem> items = new ArrayList<>();
        for (org.spongepowered.api.entity.Entity entity : spongeEvent.getEntities()) {
            if (entity instanceof EntityItem) {
                items.add((EntityItem) entity);
            }
        }
        LivingDropsEvent forgeEvent = new LivingDropsEvent(spawnCause.get(), (net.minecraft.util.DamageSource) source.get(), items, 0, false);
        return forgeEvent;
    }

    // Block events
    public static BlockEvent createBlockEvent(Event event) {
        ChangeBlockEvent spongeEvent = (ChangeBlockEvent) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockEvent forgeEvent = new BlockEvent(world, pos, world.getBlockState(pos));
        return forgeEvent;
    }

    public static BlockEvent.BreakEvent createBlockBreakEvent(Event event) {
        ChangeBlockEvent.Break spongeEvent = (ChangeBlockEvent.Break) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockState state = (IBlockState) location.getBlock();
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        BlockEvent.BreakEvent forgeEvent = new BlockEvent.BreakEvent(world, pos, state, (EntityPlayer) player.get());
        return forgeEvent;
    }

    @SuppressWarnings("deprecation")
    public static BlockEvent.PlaceEvent createBlockPlaceEvent(Event event) {
        ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockSnapshot replacementBlock = spongeEvent.getTransactions().get(0).getFinal();
        IBlockState state = (IBlockState) replacementBlock.getState();
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraftforge.common.util.BlockSnapshot forgeSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
        BlockEvent.PlaceEvent forgeEvent =
                new BlockEvent.PlaceEvent(forgeSnapshot, world.getBlockState(pos),
                        (EntityPlayer) player.get(), ((EntityPlayer) player.get()).getActiveHand());
        return forgeEvent;
    }

    // Entity events
    public static EntityEvent createEntityEvent(Event event) {
        TargetEntityEvent spongeEvent = (TargetEntityEvent) event;
        EntityEvent forgeEvent =
                new EntityEvent((Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static EntityEvent.EntityConstructing createEntityConstructingEvent(Event event) {
        ConstructEntityEvent.Post spongeEvent = (ConstructEntityEvent.Post) event;
        EntityEvent.EntityConstructing forgeEvent =
                new EntityEvent.EntityConstructing((Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static AttackEntityEvent createAttackEntityEvent(Event event) {
        InteractEntityEvent.Primary spongeEvent = (InteractEntityEvent.Primary) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        AttackEntityEvent forgeEvent = new AttackEntityEvent((EntityPlayer) player.get(), (Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    // Living events
    public static LivingEvent createLivingEvent(Event event) {
        TargetLivingEvent spongeEvent = (TargetLivingEvent) event;
        LivingEvent forgeEvent =
                new LivingEvent((EntityLivingBase) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static LivingDeathEvent createLivingDeathEvent(Event event) {
        DestructEntityEvent.Death spongeEvent = (DestructEntityEvent.Death) event;
        Optional<DamageSource> source = spongeEvent.getCause().first(DamageSource.class);
        if (!source.isPresent()) {
            return null;
        }

        LivingDeathEvent forgeEvent =
                new LivingDeathEvent((EntityLivingBase) spongeEvent.getTargetEntity(), (net.minecraft.util.DamageSource) source.get());
        return forgeEvent;
    }

    // Player events
    public static PlayerSleepInBedEvent createPlayerSleepInBedEvent(Event event) {
        SleepingEvent.Pre spongeEvent = (SleepingEvent.Pre) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }
        Location<World> location = spongeEvent.getBed().getLocation().get();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return new PlayerSleepInBedEvent((EntityPlayer) player.get(), pos);
    }

    public static LivingEntityUseItemEvent.Start createPlayerUseItemStartEvent(Event event) {
        UseItemStackEvent.Start spongeEvent = (UseItemStackEvent.Start) event;
        Optional<Living> living = spongeEvent.getCause().first(Living.class);
        if (!living.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().createStack();
        LivingEntityUseItemEvent.Start forgeEvent =
                new LivingEntityUseItemEvent.Start((EntityLivingBase) living.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static LivingEntityUseItemEvent.Tick createPlayerUseItemTickEvent(Event event) {
        UseItemStackEvent.Tick spongeEvent = (UseItemStackEvent.Tick) event;
        Optional<Living> living = spongeEvent.getCause().first(Living.class);
        if (!living.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().createStack();
        LivingEntityUseItemEvent.Tick forgeEvent = new LivingEntityUseItemEvent.Tick((EntityLivingBase) living.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static LivingEntityUseItemEvent.Stop createPlayerUseItemStopEvent(Event event) {
        UseItemStackEvent.Stop spongeEvent = (UseItemStackEvent.Stop) event;
        Optional<Living> living = spongeEvent.getCause().first(Living.class);
        if (!living.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().createStack();
        LivingEntityUseItemEvent.Stop forgeEvent = new LivingEntityUseItemEvent.Stop((EntityLivingBase) living.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static LivingEntityUseItemEvent.Finish createPlayerUseItemFinishEvent(Event event) {
        UseItemStackEvent.Finish spongeEvent = (UseItemStackEvent.Finish) event;
        Optional<Living> living = spongeEvent.getCause().first(Living.class);
        if (!living.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().createStack();
        LivingEntityUseItemEvent.Finish forgeEvent =
                new LivingEntityUseItemEvent.Finish((EntityLivingBase) living.get(), itemstack, spongeEvent.getRemainingDuration(), itemstack); // TODO Forge allows changing the itemstack mid tick...
        return forgeEvent;
    }

    // Item events
    public static ItemEvent createItemEvent(Event event) {
        TargetItemEvent spongeEvent = (TargetItemEvent) event;
        ItemEvent forgeEvent =
                new ItemEvent((EntityItem) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static ItemTossEvent createItemTossEvent(Event event) {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            DropItemEvent.Dispense spongeEvent = (DropItemEvent.Dispense) event;
            final List<org.spongepowered.api.entity.Entity> entities = spongeEvent.getEntities();
            if (!entities.isEmpty()) {
                ItemTossEvent forgeEvent = new ItemTossEvent((EntityItem) entities.get(0), (EntityPlayer) player.get());
                return forgeEvent;
            }
        }
        return null;
    }

    // World events
    public static WorldEvent createWorldEvent(Event event) {
        TargetWorldEvent spongeEvent = (TargetWorldEvent) event;
        WorldEvent forgeEvent =
                new WorldEvent((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static ChunkEvent createChunkEvent(Event event) {
        TargetChunkEvent spongeEvent = (TargetChunkEvent) event;
        ChunkEvent forgeEvent =
                new ChunkEvent(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static ChunkEvent.Load createChunkLoadEvent(Event event) {
        LoadChunkEvent spongeEvent = (LoadChunkEvent) event;
        ChunkEvent.Load forgeEvent =
                new ChunkEvent.Load(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static ChunkEvent.Unload createChunkUnloadEvent(Event event) {
        UnloadChunkEvent spongeEvent = (UnloadChunkEvent) event;
        ChunkEvent.Unload forgeEvent =
                new ChunkEvent.Unload(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    // Explosion events
    public static net.minecraftforge.event.world.ExplosionEvent createExplosionEvent(Event event) {
        ExplosionEvent spongeEvent = (ExplosionEvent) event;
        Optional<World> world = spongeEvent.getCause().first(World.class);
        if (!world.isPresent()) {
            return null;
        }

        net.minecraft.world.World forgeWorld = (net.minecraft.world.World) world.get();
        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent forgeEvent = new net.minecraftforge.event.world.ExplosionEvent(forgeWorld, explosion);
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ExplosionEvent.Start createExplosionStartEvent(Event event) {
        ExplosionEvent.Pre spongeEvent = (ExplosionEvent.Pre) event;
        Optional<World> world = spongeEvent.getCause().first(World.class);
        if (!world.isPresent()) {
            return null;
        }

        net.minecraft.world.World forgeWorld = (net.minecraft.world.World) world.get();
        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent =
                new net.minecraftforge.event.world.ExplosionEvent.Start(forgeWorld, explosion);
        return forgeEvent;
    }

    @SuppressWarnings("unchecked")
    public static net.minecraftforge.event.world.ExplosionEvent.Detonate createExplosionDetonateEvent(Event event) {
        ExplosionEvent.Detonate spongeEvent = (ExplosionEvent.Detonate) event;

        net.minecraft.world.World forgeWorld = (net.minecraft.world.World) spongeEvent.getTargetWorld();
        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent =
                new net.minecraftforge.event.world.ExplosionEvent.Detonate(forgeWorld, explosion,
                        (List<Entity>) (Object) spongeEvent.getEntities());
        return forgeEvent;
    }

    // Server events
    private static ServerChatEvent createServerChatEvent(Event event) {
        MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        Text spongeText = spongeEvent.getOriginalMessage();
        ITextComponent component = SpongeTexts.toComponent(spongeText);
        if (!(component instanceof TextComponentTranslation)) {
            component = new TextComponentTranslation("%s", component);
        }

        // Using toPlain here is fine, since the raw message from the client
        // can't have formatting.
        ServerChatEvent forgeEvent =
                new ServerChatEvent((EntityPlayerMP) player.get(), spongeEvent.getOriginalMessage().toPlain(),
                        (TextComponentTranslation) component);
        ((IMixinInitCause) forgeEvent).initCause(spongeEvent.getCause());

        return forgeEvent;
    }

    // ====================================  SPONGE TO FORGE END ==================================== \\

    // Special handling before Forge events post
    public static void onForgePost(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        if (forgeEvent instanceof net.minecraftforge.event.world.ExplosionEvent.Detonate) {
            net.minecraftforge.event.world.ExplosionEvent.Detonate explosionEvent =
                    (net.minecraftforge.event.world.ExplosionEvent.Detonate) forgeEvent;
            if (!explosionEvent.getExplosion().damagesTerrain) {
                List<BlockPos> affectedBlocks = explosionEvent.getExplosion().getAffectedBlockPositions();
                affectedBlocks.clear();
            }
        }
    }

    // Bulk Event Handling
    private static InteractBlockEvent createPlayerInteractEvent(Event event) {
        InteractBlockEvent spongeEvent = (InteractBlockEvent) event;
        Player player = spongeEvent.getCause().first(Player.class).orElse(null);
        // Forge doesn't support left-click AIR
        if (player == null || (spongeEvent instanceof InteractBlockEvent.Primary && spongeEvent.getTargetBlock() == BlockSnapshot.NONE)) {
            return spongeEvent;
        }

        BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTargetBlock().getPosition());
        EnumFacing face = DirectionFacingProvider.getInstance().get(spongeEvent.getTargetSide()).orElse(null);
        Vec3d hitVec = null;
        final EntityPlayerMP entityPlayerMP = EntityUtil.toNative(player);
        if (spongeEvent.getInteractionPoint().isPresent()) {
            hitVec = VecHelper.toVec3d(spongeEvent.getInteractionPoint().get());
        }
        if (spongeEvent instanceof InteractBlockEvent.Primary) {
            PlayerInteractEvent.LeftClickBlock forgeEvent = new PlayerInteractEvent.LeftClickBlock(entityPlayerMP, pos, face, hitVec);
            ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
            if (forgeEvent.isCanceled()) {
                spongeEvent.setCancelled(true);
            }
        } else if (face != null && spongeEvent instanceof InteractBlockEvent.Secondary) {
            EnumHand hand = spongeEvent instanceof InteractBlockEvent.Secondary.MainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
            PlayerInteractEvent.RightClickBlock forgeEvent = new PlayerInteractEvent.RightClickBlock(entityPlayerMP, hand, pos, face, hitVec);
            ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
            if (forgeEvent.isCanceled()) {
                spongeEvent.setCancelled(true);
            }

            // Mods have higher priority
            if (forgeEvent.getUseItem() != Result.DEFAULT) {
                ((InteractBlockEvent.Secondary) spongeEvent).setUseItemResult(getTristateFromResult(forgeEvent.getUseItem()));
            }
            if (forgeEvent.getUseBlock() != Result.DEFAULT) {
                ((InteractBlockEvent.Secondary) spongeEvent).setUseBlockResult(getTristateFromResult(forgeEvent.getUseBlock()));
            }
        }

        return spongeEvent;
    }

    private static Tristate getTristateFromResult(Result result) {
        if (result == Result.ALLOW) {
            return Tristate.TRUE;
        } else if (result == Result.DENY) {
            return Tristate.FALSE;
        }

        return Tristate.UNDEFINED;
    }

    public static ChangeInventoryEvent.Pickup.Pre callEntityItemPickupEvent(Event event) {
        ChangeInventoryEvent.Pickup.Pre spongeEvent = (ChangeInventoryEvent.Pickup.Pre) event;
        EntityItem entityItem = (EntityItem) spongeEvent.getTargetEntity();
        EntityItemPickupEvent forgeEvent =
                new EntityItemPickupEvent((EntityPlayer) spongeEvent.getCause().first(Player.class).get(), entityItem);
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        }
        return spongeEvent;
    }

    // unused
    public static DestructEntityEvent.Death callLivingDeathEvent(Event event) {
        DestructEntityEvent.Death spongeEvent = (DestructEntityEvent.Death) event;
        if (!spongeEvent.getCause().first(DamageSource.class).isPresent()) {
            return spongeEvent;
        }

        EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) spongeEvent.getTargetEntity();
        net.minecraft.util.DamageSource damageSource = (net.minecraft.util.DamageSource) spongeEvent.getCause().first(DamageSource.class).get();
        LivingDeathEvent forgeEvent = new LivingDeathEvent(entity, damageSource);

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);

        return spongeEvent;
    }

    @SuppressWarnings("unchecked")
    public static DropItemEvent.Destruct callLivingDropsEvent(Event event) {
        DropItemEvent.Destruct spongeEvent = (DropItemEvent.Destruct) event;
        final Cause cause = spongeEvent.getCause();
        Object source = cause.root();
        Optional<DamageSource> damageSource = cause.first(DamageSource.class);
        final Optional<Entity> spawnEntity = cause.first(Entity.class);
        final Optional<SpawnType> spawnType = cause.getContext().get(EventContextKeys.SPAWN_TYPE);
        if (!spawnType.isPresent() || !damageSource.isPresent() || !spawnEntity.isPresent()) {
            // Mods expect EntityJoinWorldEvent to trigger
            callEntityJoinWorldEvent(spongeEvent);
            return spongeEvent;
        }
        Entity entity = spawnEntity.get();
        if (entity == null || !(entity instanceof EntityLivingBase)) {
            return spongeEvent;
        }

        LivingDropsEvent forgeEvent = null;
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            forgeEvent = new PlayerDropsEvent(player, (net.minecraft.util.DamageSource) damageSource.get(), (List<EntityItem>)(List<?>)spongeEvent.getEntities(),
                            ((IMixinEntityLivingBase) entity).getRecentlyHit() > 0);
        } else {
            forgeEvent = new LivingDropsEvent((EntityLivingBase) entity, (net.minecraft.util.DamageSource) damageSource.get(), (List<EntityItem>)(List<?>)spongeEvent.getEntities(), 0,
                            ((IMixinEntityLivingBase) entity).getRecentlyHit() > 0);
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        } else {
            callEntityJoinWorldEvent(spongeEvent);
        }

        return spongeEvent;
    }

    public static DropItemEvent.Dispense callItemTossEvent(Event event) {
        DropItemEvent.Dispense spongeEvent = (DropItemEvent.Dispense) event;
        if (spongeEvent.getEntities().size() <= 0) {
            return spongeEvent;
        }
        final Cause cause = spongeEvent.getCause();
        Object source = cause.root();
        Optional<DamageSource> damageSource = cause.first(DamageSource.class);
        final Optional<Entity> spawnEntity = cause.first(Entity.class);
        final Optional<SpawnType> spawnType = cause.getContext().get(EventContextKeys.SPAWN_TYPE);
        if (!spawnType.isPresent() || !damageSource.isPresent() || !spawnEntity.isPresent()) {
            // Mods expect EntityJoinWorldEvent to trigger
            callEntityJoinWorldEvent(spongeEvent);
            return spongeEvent;
        }
        Entity entity = spawnEntity.get();
        EntityItem item = (EntityItem) spongeEvent.getEntities().get(0);
        if (entity == null || item == null || item.getItem() == null || !(entity instanceof Player)) {
            return spongeEvent;
        }

        ItemTossEvent forgeEvent = new ItemTossEvent(item, (EntityPlayerMP) entity);
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        if (forgeEvent.isCanceled()) {
            if (item.isDead) {
                // Don't restore packet item if a mod wants it dead
                // Mods such as Flux-Networks kills the entity item to spawn a custom one
                return spongeEvent;
            }
            spongeEvent.setCancelled(true);
        } else {
            // Forge treats EntityJoinWorldEvent separately from Toss so we need to call it here
            callEntityJoinWorldEvent(spongeEvent);
        }

        return spongeEvent;
    }

    public static SpawnEntityEvent callEntityJoinWorldEvent(Event event) {
        SpawnEntityEvent spongeEvent = (SpawnEntityEvent) event;
        ListIterator<org.spongepowered.api.entity.Entity> iterator = spongeEvent.getEntities().listIterator();
        if (spongeEvent.getEntities().size() == 0) {
            return spongeEvent;
        }

        // used to avoid player item restores when set to dead
        boolean canCancelEvent = true;

        while (iterator.hasNext()) {
            org.spongepowered.api.entity.Entity entity = iterator.next();
            EntityJoinWorldEvent forgeEvent = new EntityJoinWorldEvent((Entity) entity,
                    (net.minecraft.world.World) entity.getLocation().getExtent());

            boolean prev = StaticMixinForgeHelper.preventInternalForgeEntityListener;
            StaticMixinForgeHelper.preventInternalForgeEntityListener = true;
            ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
            StaticMixinForgeHelper.preventInternalForgeEntityListener = prev;
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
        if (spongeEvent.getEntities().size() == 0 && canCancelEvent) {
            spongeEvent.setCancelled(true);
        }
        return spongeEvent;
    }

    public static void handlePrefireLogic(Event event) {
        if (event instanceof SpawnEntityEvent) {
            handleCustomStack((SpawnEntityEvent) event);
        }
    }

    // Copied from ForgeInternalHandler.onEntityJoinWorld, but with modifications
    private static void handleCustomStack(SpawnEntityEvent event) {
        // Sponge start - iterate over entities
        ListIterator<org.spongepowered.api.entity.Entity> it = event.getEntities().listIterator();
        while (it.hasNext()) {
            Entity entity = (Entity) it.next(); //Sponge - use entity from event
            if (entity.getClass().equals(EntityItem.class)) {
                ItemStack stack = ((EntityItem) entity).getItem();

                if (stack.isEmpty()) {
                    //entity.setDead();
                    //event.setCanceled(true);
                    continue; // Sponge - continue instead of return
                }

                Item item = stack.getItem();
                if (item == null) {
                    // Sponge - technically, this check is in the wrong place, as it normally runs in Forge's listener (i.e. after 'beforeModifications' listener)
                    // However, it's really only a sanity check for something that should never happen, so it's fine
                    FMLLog.warning("Attempted to add a EntityItem to the world with a invalid item at " +
                                    "(%2.2f,  %2.2f, %2.2f), this is most likely a config issue between you and the server. Please double check your configs",
                            entity.posX, entity.posY, entity.posZ);
                    entity.setDead();
                    event.setCancelled(true); // Sponge - use our setCancelled method
                    continue; // Sponge - continue instead of return
                }

                if (item.hasCustomEntity(stack)) {
                    Entity newEntity = item.createEntity(entity.getEntityWorld(), entity, stack); // Sponge - use world from entity
                    if (newEntity != null) {
                        entity.setDead();
                        //event.setCanceled(true); Sponge - don't cancel the event
                        // Sponge start - fire cancelled event, and return new event.
                        // We don't need to set 'StaticMixinForgeHelper.preventInternalForgeEntityListener' to 'true',
                        // since Forge's listener only handled uncancelled events

                        EntityJoinWorldEvent cancelledEvent = new EntityJoinWorldEvent(entity, entity.getEntityWorld());
                        cancelledEvent.setCanceled(true);
                        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(cancelledEvent, true);

                        if (!cancelledEvent.isCanceled()) {
                            SpongeImpl.getLogger()
                                    .error("A mod has un-cancelled the EntityJoinWorld event for the original EntityItem (from before Item#createEntity is called). This is almost certainly a terrible idea!");
                        }

                        it.set((org.spongepowered.api.entity.Entity) newEntity);
                        continue;
                        // Sponge end
                    }
                }
            }
        }
    }

    public static NotifyNeighborBlockEvent callNeighborNotifyEvent(Event event) {
        NotifyNeighborBlockEvent spongeEvent = (NotifyNeighborBlockEvent) event;
        LocatableBlock locatableBlock = spongeEvent.getCause().first(LocatableBlock.class).orElse(null);
        TileEntity tileEntitySource = spongeEvent.getCause().first(TileEntity.class).orElse(null);
        Location<World> sourceLocation = null;
        IBlockState state = null;
        if (locatableBlock != null) {
            Location<World> location = locatableBlock.getLocation();
            sourceLocation = location;
            state = (IBlockState) locatableBlock.getBlockState();
        } else if (tileEntitySource != null) {
            sourceLocation = tileEntitySource.getLocation();
            state = (IBlockState) sourceLocation.getBlock();
        } else { // should never happen but just in case it does
            return spongeEvent;
        }

        EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
        for (Map.Entry<Direction, BlockState> mapEntry : spongeEvent.getNeighbors().entrySet()) {
            if (mapEntry.getKey() != Direction.NONE) {
                facings.add(DirectionFacingProvider.getInstance().get(mapEntry.getKey()).get());
            }
        }

        if (facings.isEmpty()) {
            return spongeEvent;
        }

        BlockPos pos = ((IMixinLocation) (Object) sourceLocation).getBlockPos();
        net.minecraft.world.World world = (net.minecraft.world.World) sourceLocation.getExtent();
        // TODO - the boolean forced redstone bit needs to be set properly
        final NeighborNotifyEvent forgeEvent = new NeighborNotifyEvent(world, pos, state, facings, false);
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        }

        return spongeEvent;
    }

    public static ChangeBlockEvent.Place callBlockPlaceEvent(Event event) {
        ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) event;

        if (spongeEvent.getCause().root() instanceof Player) {
            EntityPlayer player = (EntityPlayer) spongeEvent.getCause().first(Player.class).get();
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
                return spongeEvent;
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

                BlockEvent.PlaceEvent forgeEvent = new BlockEvent.PlaceEvent(blockSnapshot, placedAgainst, player, hand);
                ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                if (forgeEvent.isCanceled()) {
                    spongeEvent.setCancelled(true);
                }
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

                BlockEvent.MultiPlaceEvent forgeEvent = new BlockEvent.MultiPlaceEvent(blockSnapshots, placedAgainst, player, hand);
                ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                if (forgeEvent.isCanceled()) {
                    spongeEvent.setCancelled(true);
                }
            }
        }
        return spongeEvent;
    }

    private static ExplosionEvent.Pre callExplosionEventPre(Event event) {
        ExplosionEvent.Pre spongeEvent = (ExplosionEvent.Pre) event;

        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Start(
                ((net.minecraft.world.World) spongeEvent.getTargetWorld()), ((Explosion) ((ExplosionEvent.Pre) event).getExplosion()));
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        }
        return spongeEvent;
    }

    @SuppressWarnings("unchecked")
    private static ExplosionEvent.Detonate callExplosionEventDetonate(Event event) {
        ExplosionEvent.Detonate spongeEvent = (ExplosionEvent.Detonate) event;

        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Detonate(
                (net.minecraft.world.World) spongeEvent.getTargetWorld(), explosion,
                (List<Entity>) (List<?>) spongeEvent.getEntities());
        explosion.getAffectedBlockPositions().clear();
        for (Location<World> x : spongeEvent.getAffectedLocations()) {
            explosion.getAffectedBlockPositions().add(VecHelper.toBlockPos(x.getPosition()));
        }

        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);

        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        }
        ((ExplosionEvent.Detonate) event).getAffectedLocations().clear();
        for (BlockPos pos : forgeEvent.getAffectedBlocks()) {
            ((ExplosionEvent.Detonate) event).getAffectedLocations().add(new Location<>(spongeEvent.getTargetWorld(), VecHelper.toVector3i(pos)));
        }
        return spongeEvent;
    }

    private static InteractEntityEvent.Secondary callEntityInteractEvent(Event event) {
        InteractEntityEvent.Secondary spongeEvent = (InteractEntityEvent.Secondary) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }
        final EntityPlayerMP entityPlayerMP = EntityUtil.toNative(player.get());
        final EnumHand hand = entityPlayerMP.getActiveHand();

        final EntityPlayer entityPlayer = (EntityPlayer) player.get();
        final Entity entity = (Entity) spongeEvent.getTargetEntity();
        final Vector3d hitVec = spongeEvent.getInteractionPoint().orElse(null);

        PlayerInteractEvent forgeEvent = null;
        if (hitVec != null) {
            forgeEvent = new PlayerInteractEvent.EntityInteractSpecific(entityPlayer, hand, entity, VecHelper.toVec3d(hitVec));
        } else {
            forgeEvent = new PlayerInteractEvent.EntityInteract(entityPlayer, hand, entity);
        }
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        }

        return spongeEvent;
    }

    private static ClientConnectionEvent.Join callPlayerLoggedInEvent(Event event) {
        ClientConnectionEvent.Join spongeEvent = (ClientConnectionEvent.Join) event;
        PlayerLoggedInEvent fmlEvent = new PlayerLoggedInEvent((EntityPlayer) spongeEvent.getTargetEntity());
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(fmlEvent, true);

        return spongeEvent;
    }

    private static ClientConnectionEvent.Disconnect callPlayerLoggedOutEvent(Event event) {
        ClientConnectionEvent.Disconnect spongeEvent = (ClientConnectionEvent.Disconnect) event;
        PlayerLoggedOutEvent fmlEvent = new PlayerLoggedOutEvent((EntityPlayer) spongeEvent.getTargetEntity());
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(fmlEvent, true);

        return spongeEvent;
    }

    private static RespawnPlayerEvent callPlayerRespawnEvent(Event event) {
        RespawnPlayerEvent spongeEvent = (RespawnPlayerEvent) event;
        PlayerRespawnEvent fmlEvent = new PlayerRespawnEvent((EntityPlayer) spongeEvent.getTargetEntity(), !spongeEvent.isDeath());
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(fmlEvent, true);

        return spongeEvent;
    }

    private static MoveEntityEvent.Teleport callEntityTravelToDimensionEvent(Event event) {
        MoveEntityEvent.Teleport spongeEvent = (MoveEntityEvent.Teleport) event;
        org.spongepowered.api.entity.Entity entity = spongeEvent.getTargetEntity();
        if (!(entity instanceof EntityPlayerMP)) {
            return spongeEvent;
        }

        int fromDimensionId = ((net.minecraft.world.World) spongeEvent.getFromTransform().getExtent()).provider.getDimension();
        int toDimensionId = ((net.minecraft.world.World) spongeEvent.getToTransform().getExtent()).provider.getDimension();
        if (fromDimensionId != toDimensionId) {
            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension((EntityPlayerMP) entity, toDimensionId))  {
                spongeEvent.setCancelled(true);
            }
        }

        return spongeEvent;
    }

    private static SaveWorldEvent callWorldSaveEvent(Event event) {
        SaveWorldEvent spongeEvent = (SaveWorldEvent) event;
        // Since Forge only uses a single save handler, we need to make sure to pass the overworld's handler.
        // This makes sure that mods dont attempt to save/read their data from the wrong location.
        ((IMixinWorld) spongeEvent.getTargetWorld()).setCallingWorldEvent(true);
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new WorldEvent.Save((net.minecraft.world.World) spongeEvent.getTargetWorld()), true);
        ((IMixinWorld) spongeEvent.getTargetWorld()).setCallingWorldEvent(false);
        return spongeEvent;
    }

    private static LoadWorldEvent callWorldLoadEvent(Event event) {
        LoadWorldEvent spongeEvent = (LoadWorldEvent) event;
        // Since Forge only uses a single save handler, we need to make sure to pass the overworld's handler.
        // This makes sure that mods dont attempt to save/read their data from the wrong location.
        final net.minecraft.world.World minecraftWorld = (net.minecraft.world.World) spongeEvent.getTargetWorld();
        ((IMixinWorld) spongeEvent.getTargetWorld()).setCallingWorldEvent(true);
        ((IMixinChunkProviderServer) minecraftWorld.getChunkProvider()).setForceChunkRequests(true);
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new WorldEvent.Load(minecraftWorld), true);
        ((IMixinChunkProviderServer) minecraftWorld.getChunkProvider()).setForceChunkRequests(false);
        ((IMixinWorld) minecraftWorld).setCallingWorldEvent(false);
        return spongeEvent;
    }

    private static UnloadWorldEvent callWorldUnloadEvent(Event event) {
        UnloadWorldEvent spongeEvent = (UnloadWorldEvent) event;
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new WorldEvent.Unload((net.minecraft.world.World) spongeEvent.getTargetWorld()), true);

        return spongeEvent;
    }

    private static LoadChunkEvent callChunkLoadEvent(Event event) {
        LoadChunkEvent spongeEvent = (LoadChunkEvent) event;
        final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk();
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new ChunkEvent.Load(chunk), true);
        return spongeEvent;
    }

    private static UnloadChunkEvent callChunkUnloadEvent(Event event) {
        UnloadChunkEvent spongeEvent = (UnloadChunkEvent) event;
        final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk();
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new ChunkEvent.Unload(chunk), true);
        return spongeEvent;
    }
}
