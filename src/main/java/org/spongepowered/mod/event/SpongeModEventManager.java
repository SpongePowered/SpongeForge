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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Singleton;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinASMEventHandler;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.interfaces.IMixinLoadController;

import java.util.List;

import javax.inject.Inject;

@Singleton
public class SpongeModEventManager extends SpongeEventManager {

    @SuppressWarnings("unused") private final ImmutableBiMap<EventPriority, Order> priorityMappings =
            new ImmutableBiMap.Builder<EventPriority, Order>()
                    .put(EventPriority.HIGHEST, Order.FIRST)
                    .put(EventPriority.HIGH, Order.EARLY)
                    .put(EventPriority.NORMAL, Order.DEFAULT)
                    .put(EventPriority.LOW, Order.LATE)
                    .put(EventPriority.LOWEST, Order.LAST)
                    .build();


	@SuppressWarnings({"unchecked"})
	Class<? extends Event>[] useItemStack = new Class[] {UseItemStackEvent.Start.class, UseItemStackEvent.Tick.class, UseItemStackEvent.Stop.class, UseItemStackEvent.Finish.class, UseItemStackEvent.Replace.class, UseItemStackEvent.Reset.class};
	@SuppressWarnings({"unchecked"})
	Class<? extends Event>[] interactEntity = new Class[] {InteractEntityEvent.Primary.MainHand.class, InteractEntityEvent.Primary.OffHand.class, InteractEntityEvent.Secondary.MainHand.class, InteractEntityEvent.Secondary.OffHand.class};
	@SuppressWarnings({"unchecked"})
	Class<? extends Event>[] interactBlock = new Class[] {InteractBlockEvent.Primary.MainHand.class, InteractBlockEvent.Primary.OffHand.class, InteractBlockEvent.Secondary.MainHand.class, InteractBlockEvent.Secondary.OffHand.class};
	@SuppressWarnings({"unchecked"})
	Class<? extends Event>[] spawnEntityEvent = new Class[] {SpawnEntityEvent.ChunkLoad.class, SpawnEntityEvent.Spawner.class};

    @SuppressWarnings("unchecked")
    public final ImmutableMultimap<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, Class<? extends Event>>
            forgeToSpongeEventMapping =
            new ImmutableMultimap.Builder<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, Class<? extends Event>>()
                    .put(ItemExpireEvent.class, DestructEntityEvent.Death.class)
                    .put(ItemTossEvent.class, DropItemEvent.Dispense.class)

                    .put(EnderTeleportEvent.class, MoveEntityEvent.Teleport.Portal.class)

                    .put(LivingAttackEvent.class, org.spongepowered.api.event.entity.AttackEntityEvent.class)
                    .put(LivingDeathEvent.class, DestructEntityEvent.Death.class)
                    .putAll(LivingDropsEvent.class, DropItemEvent.Destruct.class, DropItemEvent.Custom.class)
                    .putAll(LivingEntityUseItemEvent.class, this.useItemStack)
                    .put(LivingEvent.LivingJumpEvent.class, MoveEntityEvent.class)
                    .put(LivingExperienceDropEvent.class, HarvestEntityEvent.TargetPlayer.class)
                    .put(LivingHealEvent.class, HealEntityEvent.class)
                    .put(LivingHurtEvent.class, org.spongepowered.api.event.entity.AttackEntityEvent.class)
                    .putAll(LivingSpawnEvent.class, SpawnEntityEvent.Spawner.class)
                    .putAll(ZombieEvent.class, SpawnEntityEvent.ChunkLoad.class, SpawnEntityEvent.Spawner.class)

                    .put(MinecartCollisionEvent.class, CollideEntityEvent.Impact.class)
                    .putAll(MinecartInteractEvent.class, this.interactEntity)

                    .put(ArrowLooseEvent.class, SpawnEntityEvent.Spawner.class)
                    .putAll(ArrowNockEvent.class, this.useItemStack)
                    .put(AttackEntityEvent.class, org.spongepowered.api.event.entity.AttackEntityEvent.class)

                    .putAll(BonemealEvent.class, this.interactBlock)
                    .putAll(BonemealEvent.class, this.useItemStack)

                    .putAll(EntityItemPickupEvent.class, ChangeInventoryEvent.Pickup.Pre.class,  DestructEntityEvent.class)

                    .putAll(FillBucketEvent.class, this.interactBlock)
                    .putAll(FillBucketEvent.class, this.useItemStack)

                    .putAll(PlayerDestroyItemEvent.class, DestructEntityEvent.class, DropItemEvent.Destruct.class)
                    .putAll(PlayerDropsEvent.class, DropItemEvent.Dispense.class, DropItemEvent.Destruct.class, DestructEntityEvent.Death.class)
                    .putAll(net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck.class, ChangeBlockEvent.Modify.class, ChangeBlockEvent.Post.class)
                    .put(PlayerFlyableFallEvent.class, MoveEntityEvent.class)

                    // This event is ignored as we handle it differently on plugin side. See MixinNetHandlerPlayServer for more info.
                    //.putAll(PlayerInteractEvent.EntityInteract.class, InteractEntityEvent.Secondary.MainHand.class, InteractEntityEvent.Secondary.OffHand.class)
                    .putAll(PlayerInteractEvent.EntityInteractSpecific.class, InteractEntityEvent.Secondary.MainHand.class, InteractEntityEvent.Secondary.OffHand.class)
                    .putAll(PlayerInteractEvent.RightClickBlock.class, InteractBlockEvent.Secondary.MainHand.class, InteractBlockEvent.Secondary.OffHand.class)
                    .putAll(PlayerInteractEvent.RightClickItem.class, InteractBlockEvent.Secondary.MainHand.class, InteractBlockEvent.Secondary.OffHand.class)
                    .putAll(PlayerInteractEvent.LeftClickBlock.class, InteractBlockEvent.Primary.MainHand.class, InteractBlockEvent.Primary.OffHand.class)
                    .putAll(PlayerInteractEvent.LeftClickEmpty.class, InteractBlockEvent.Primary.MainHand.class, InteractBlockEvent.Primary.OffHand.class)

                    .putAll(PlayerPickupXpEvent.class, ChangeEntityExperienceEvent.class, DestructEntityEvent.class)

                    .putAll(UseHoeEvent.class, this.interactBlock)
                    .putAll(UseHoeEvent.class, this.useItemStack)

                    .putAll(EntityEvent.EntityConstructing.class, ConstructEntityEvent.Pre.class, ConstructEntityEvent.Post.class)
                    .putAll(EntityEvent.EntityConstructing.class, this.spawnEntityEvent)
                    .put(EntityEvent.EnteringChunk.class, MoveEntityEvent.class)
                    .putAll(EntityJoinWorldEvent.class, this.spawnEntityEvent)
                    .putAll(EntityMountEvent.class, RideEntityEvent.Dismount.class, RideEntityEvent.Mount.class)
                    .putAll(EntityStruckByLightningEvent.class, LightningEvent.Pre.class, LightningEvent.Strike.class, LightningEvent.Post.class)
                    .put(EntityTravelToDimensionEvent.class, MoveEntityEvent.Teleport.Portal.class)


                    // These are handled in MixinPlayerInteractionManager#onTryHarvestBlock
                    //.putAll(BlockEvent.HarvestDropsEvent.class, SpawnEntityEvent.class, DropItemEvent.class, ChangeBlockEvent.class)
                    //.putAll(BlockEvent.BreakEvent.class, ChangeBlockEvent.Break.class)
                    .putAll(BlockEvent.PlaceEvent.class, ChangeBlockEvent.Place.class, ChangeBlockEvent.Modify.class, ChangeBlockEvent.Post.class)
                    .putAll(BlockEvent.MultiPlaceEvent.class, ChangeBlockEvent.Place.class)
                    .put(BlockEvent.NeighborNotifyEvent.class, NotifyNeighborBlockEvent.class)

                    .put(ChunkEvent.Load.class, LoadChunkEvent.class)
                    .put(ExplosionEvent.Start.class, org.spongepowered.api.event.world.ExplosionEvent.Pre.class)
                    .put(ExplosionEvent.Detonate.class, org.spongepowered.api.event.world.ExplosionEvent.Detonate.class)
                    .put(WorldEvent.Load.class, LoadWorldEvent.class)
                    .put(WorldEvent.Unload.class, UnloadWorldEvent.class)

                    .put(CommandEvent.class, SendCommandEvent.class)

                    .put(ServerChatEvent.class, MessageChannelEvent.Chat.class)

                    .putAll(PlayerEvent.ItemPickupEvent.class, DestructEntityEvent.class, ChangeInventoryEvent.Pickup.class)
                    .putAll(PlayerEvent.PlayerLoggedInEvent.class, ClientConnectionEvent.Auth.class, ClientConnectionEvent.Login.class, ClientConnectionEvent.Join.class)
                    .put(PlayerEvent.PlayerLoggedOutEvent.class, ClientConnectionEvent.Disconnect.class)
                    .put(PlayerEvent.PlayerChangedDimensionEvent.class, MoveEntityEvent.Teleport.Portal.class)

                    .build();



    @Inject
    public SpongeModEventManager(Logger logger, PluginManager pluginManager) {
        super(logger, pluginManager);
    }

    public static boolean shouldUseCauseStackManager(boolean allowClientThread) {
        final boolean client = Sponge.getGame().getPlatform().getExecutionType().isClient();
        final boolean hasServer = Sponge.isServerAvailable();
        return (allowClientThread && client && !hasServer) || (hasServer && Sponge.getServer().isMainThread());
    }

    // Uses Forge mixins
    public boolean post(Event spongeEvent, net.minecraftforge.fml.common.eventhandler.Event forgeEvent, IEventListener[] listeners,
            boolean useCauseStackManager) {
        checkNotNull(forgeEvent, "forgeEvent");

        if (spongeEvent == null) { // Fired by Forge
            spongeEvent = ((IMixinEvent) forgeEvent).createSpongeEvent();
        }

        boolean isNotSameEvent = spongeEvent != forgeEvent;
        RegisteredListener.Cache listenerCache = getHandlerCache(spongeEvent);
        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), true, false, useCauseStackManager);
        }

        // If there are no forge listeners for event, skip sync
        // If plugin cancelled event before modifications, ignore mods
        if (listeners.length > 0 && !forgeEvent.isCanceled()) {
            if (isNotSameEvent) {
                // Sync the forge event from Sponge
                ((IMixinEvent) forgeEvent).syncDataToForge(spongeEvent);
            }

            for (IEventListener listener : listeners) {
                try {
                    if (listener instanceof IMixinASMEventHandler) {
                        IMixinASMEventHandler modListener = (IMixinASMEventHandler) listener;
                        modListener.getTimingsHandler().startTimingIfSync();
                        listener.invoke(forgeEvent);
                        modListener.getTimingsHandler().stopTimingIfSync();
                    } else {
                        listener.invoke(forgeEvent);
                    }
                } catch (Throwable throwable) {
                    this.logger.error("Encountered an exception while processing a Forge event listener", throwable);
                }
            }

            if (isNotSameEvent) {
                // Sync the forge event back to Sponge
                ((IMixinEvent) forgeEvent).syncDataToSponge(spongeEvent);
            }
        }

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false, useCauseStackManager);
        }

        if (spongeEvent instanceof Cancellable && spongeEvent != forgeEvent) {
            if (forgeEvent.isCancelable() && ((Cancellable) spongeEvent).isCancelled()) {
                forgeEvent.setCanceled(true);
            }
        }
        return forgeEvent.isCancelable() && forgeEvent.isCanceled();
    }

    // Uses SpongeForgeEventFactory (required for any events shared in SpongeCommon)
    private boolean post(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz, boolean useCauseStackManager) {
        RegisteredListener.Cache listenerCache = getHandlerCache(spongeEvent);
        SpongeForgeEventFactory.handlePrefireLogic(spongeEvent);

        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), true, false, useCauseStackManager);
        }

        boolean cancelled = false;
        if (spongeEvent instanceof Cancellable) {
            cancelled = ((Cancellable) spongeEvent).isCancelled();
        }
        if (!cancelled) {
            spongeEvent = SpongeForgeEventFactory.callForgeEvent(spongeEvent, clazz);
        }

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false, useCauseStackManager);
        }

        return spongeEvent instanceof Cancellable && ((Cancellable) spongeEvent).isCancelled();
    }

    @SuppressWarnings("unchecked")
    private boolean post(Event event, List<RegisteredListener<?>> listeners, boolean beforeModifications, boolean forced,
            boolean useCauseStackManager) {
        ModContainer oldContainer = ((IMixinLoadController) SpongeMod.instance.getController()).getActiveModContainer();
        for (@SuppressWarnings("rawtypes")
        RegisteredListener listener : listeners) {
            ((IMixinLoadController) SpongeMod.instance.getController()).setActiveModContainer((ModContainer) listener.getPlugin());
            try {
                if (forced || (!listener.isBeforeModifications() && !beforeModifications)
                        || (listener.isBeforeModifications() && beforeModifications)) {
                    listener.getTimingsHandler().startTimingIfSync();
                    if (event instanceof AbstractEvent) {
                        ((AbstractEvent) event).currentOrder = listener.getOrder();
                    }
                    if (useCauseStackManager) {
                        Sponge.getCauseStackManager().pushCause(listener.getPlugin());
                        try (CauseStackManager.StackFrame ignored = Sponge.getCauseStackManager().pushCauseFrame()) {
                            listener.handle(event);
                        }
                        Sponge.getCauseStackManager().popCause();
                    } else {
                        listener.handle(event);
                    }
                }
            } catch (Throwable e) {
                this.logger.error("Could not pass {} to {}", event.getClass().getSimpleName(), listener.getPlugin(), e);
            } finally {
                listener.getTimingsHandler().stopTimingIfSync();
            }
        }
        if (event instanceof AbstractEvent) {
            ((AbstractEvent) event).currentOrder = null;
        }
        ((IMixinLoadController) SpongeMod.instance.getController()).setActiveModContainer(oldContainer);
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event spongeEvent, boolean allowClientThread) {
        if (!allowClientThread & Sponge.getGame().getPlatform().getExecutionType().isClient()) {
            return false;
        }
        final boolean useCauseStackManager = shouldUseCauseStackManager(allowClientThread);
        if (spongeEvent.getClass().getInterfaces().length > 0) {
            Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz = SpongeForgeEventFactory.getForgeEventClass(spongeEvent);
            if (clazz != null) {
                return post(spongeEvent, clazz, useCauseStackManager);
            }
        }
        // no checking for modifications required
        return post(spongeEvent, getHandlerCache(spongeEvent).getListeners(), false, true, useCauseStackManager);
    }
}
