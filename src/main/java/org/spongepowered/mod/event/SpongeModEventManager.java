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

import co.aikar.timings.TimingsManager;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
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
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.DismountEntityEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MountEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinASMEventHandler;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.interfaces.IMixinLoadController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class SpongeModEventManager extends SpongeEventManager {

    @SuppressWarnings("unused") private final ImmutableBiMap<EventPriority, Order> priorityMappings =
            new ImmutableBiMap.Builder<EventPriority, Order>()
                    .put(EventPriority.HIGHEST, Order.FIRST)
                    .put(EventPriority.HIGH, Order.EARLY)
                    .put(EventPriority.NORMAL, Order.DEFAULT)
                    .put(EventPriority.LOW, Order.LATE)
                    .put(EventPriority.LOWEST, Order.LAST)
                    .build();

    private final ImmutableMap<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>> eventMappings =
            new ImmutableMap.Builder<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>>()
                    .put(TargetChunkEvent.class, ChunkEvent.class)
                    .put(LoadChunkEvent.class, ChunkEvent.Load.class)
                    .put(UnloadChunkEvent.class, ChunkEvent.Unload.class)
                    .put(ConstructEntityEvent.Post.class, EntityEvent.EntityConstructing.class)
                    .put(TargetEntityEvent.class, EntityEvent.class)
                    .put(MessageChannelEvent.Chat.class, ServerChatEvent.class)
                    .put(InteractEntityEvent.Primary.class, AttackEntityEvent.class)
                    .put(TargetWorldEvent.class, WorldEvent.class)
                    .put(SaveWorldEvent.class, WorldEvent.Save.class)
                    .put(UseItemStackEvent.Start.class, LivingEntityUseItemEvent.Start.class)
                    .put(UseItemStackEvent.Tick.class, LivingEntityUseItemEvent.Tick.class)
                    .put(UseItemStackEvent.Stop.class, LivingEntityUseItemEvent.Stop.class)
                    .put(UseItemStackEvent.Finish.class, LivingEntityUseItemEvent.Finish.class)
                    .put(ClientConnectionEvent.Join.class, PlayerEvent.PlayerLoggedInEvent.class)
                    .put(ClientConnectionEvent.Disconnect.class, PlayerEvent.PlayerLoggedOutEvent.class)
                    .put(SleepingEvent.Pre.class, PlayerSleepInBedEvent.class)
                    .build();

    @SuppressWarnings("unchecked")
    public final ImmutableMultimap<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, Class<? extends Event>>
            forgeToSpongeEventMapping =
            new ImmutableMultimap.Builder<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, Class<? extends Event>>()
                    .put(ItemEvent.class, AffectEntityEvent.class)
                    .put(ItemExpireEvent.class, DestructEntityEvent.class)
                    .put(ItemTossEvent.class, DropItemEvent.class)

                    .put(EnderTeleportEvent.class, MoveEntityEvent.class)

                    .put(LivingAttackEvent.class, org.spongepowered.api.event.entity.AttackEntityEvent.class)
                    .put(LivingDeathEvent.class, DestructEntityEvent.class)
                    .put(LivingDropsEvent.class, DropItemEvent.class)
                    .put(LivingEntityUseItemEvent.class, UseItemStackEvent.class)
                    .put(LivingEvent.class, AffectEntityEvent.class)
                    .put(LivingEvent.LivingJumpEvent.class, MoveEntityEvent.class)
                    .put(LivingExperienceDropEvent.class, HarvestEntityEvent.class)
                    .put(LivingHealEvent.class, HealEntityEvent.class)
                    .put(LivingHurtEvent.class, org.spongepowered.api.event.entity.AttackEntityEvent.class)
                    .put(LivingSpawnEvent.class, SpawnEntityEvent.class)
                    .put(ZombieEvent.class, SpawnEntityEvent.class)

                    .put(MinecartCollisionEvent.class, CollideEntityEvent.class)
                    .put(MinecartInteractEvent.class, InteractEntityEvent.class)

                    .put(ArrowLooseEvent.class, SpawnEntityEvent.class)
                    .put(ArrowNockEvent.class, UseItemStackEvent.class)
                    .put(AttackEntityEvent.class, org.spongepowered.api.event.entity.AttackEntityEvent.class)
                    .putAll(BonemealEvent.class, InteractBlockEvent.class, UseItemStackEvent.class)
                    .putAll(EntityItemPickupEvent.class, ChangeInventoryEvent.class, DestructEntityEvent.class)
                    .putAll(FillBucketEvent.class, InteractBlockEvent.class, UseItemStackEvent.class)
                    .putAll(PlayerDestroyItemEvent.class, DestructEntityEvent.class, DropItemEvent.class)
                    .putAll(PlayerDropsEvent.class, DropItemEvent.class, DestructEntityEvent.class)
                    .putAll(net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck.class, ChangeBlockEvent.Modify.class, ChangeBlockEvent.Modify.class, ChangeBlockEvent.Post.class)
                    .put(PlayerFlyableFallEvent.class, MoveEntityEvent.class)
                    .put(PlayerInteractEvent.class, InteractEvent.class)
                    .putAll(PlayerPickupXpEvent.class, ChangeEntityExperienceEvent.class, DestructEntityEvent.class)
                    .putAll(UseHoeEvent.class, InteractBlockEvent.class, UseItemStackEvent.class)

                    .put(EntityEvent.EntityConstructing.class, SpawnEntityEvent.class)
                    .put(EntityEvent.EnteringChunk.class, MoveEntityEvent.class)
                    .put(EntityJoinWorldEvent.class, SpawnEntityEvent.class)
                    .putAll(EntityMountEvent.class, MountEntityEvent.class, DismountEntityEvent.class)
                    .put(EntityStruckByLightningEvent.class, LightningEvent.class)
                    .put(EntityTravelToDimensionEvent.class, MoveEntityEvent.class)


                    .putAll(BlockEvent.HarvestDropsEvent.class, SpawnEntityEvent.class, DropItemEvent.class, ChangeBlockEvent.class)
                    .putAll(BlockEvent.BreakEvent.class, ChangeBlockEvent.Break.class, ChangeBlockEvent.Post.class)
                    .putAll(BlockEvent.PlaceEvent.class, ChangeBlockEvent.Place.class, ChangeBlockEvent.Modify.class, ChangeBlockEvent.Post.class)
                    .put(BlockEvent.MultiPlaceEvent.class, ChangeBlockEvent.Place.class)
                    .put(BlockEvent.NeighborNotifyEvent.class, NotifyNeighborBlockEvent.class)

                    .put(ChunkDataEvent.Load.class, LoadChunkEvent.class)
                    .put(ChunkEvent.Load.class, LoadChunkEvent.class)

                    .put(ExplosionEvent.class, org.spongepowered.api.event.world.ExplosionEvent.class)
                    .put(WorldEvent.Load.class, LoadWorldEvent.class)
                    .put(WorldEvent.Unload.class, UnloadWorldEvent.class)

                    .put(CommandEvent.class, SendCommandEvent.class)

                    .put(ServerChatEvent.class, MessageChannelEvent.Chat.class)

                    .putAll(PlayerEvent.ItemPickupEvent.class, DestructEntityEvent.class, ChangeInventoryEvent.Pickup.class)
                    .put(PlayerEvent.PlayerLoggedInEvent.class, ClientConnectionEvent.class)
                    .put(PlayerEvent.PlayerLoggedOutEvent.class, ClientConnectionEvent.class)
                    .put(PlayerEvent.PlayerChangedDimensionEvent.class, MoveEntityEvent.class)

                    .build();



    @Inject
    public SpongeModEventManager(PluginManager pluginManager) {
        super(pluginManager);
    }

    // Uses Forge mixins
    public boolean post(Event spongeEvent, net.minecraftforge.fml.common.eventhandler.Event forgeEvent, IEventListener[] listeners) {
        checkNotNull(forgeEvent, "forgeEvent");

        if (spongeEvent == null) { // Fired by Forge
            spongeEvent = ((IMixinEvent) forgeEvent).createSpongeEvent();
        }
        RegisteredListener.Cache listenerCache = getHandlerCache(spongeEvent);
        // Fire events to plugins before modifications
        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), true, false);
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.stopTimingIfSync();

        // If there are no forge listeners for event, skip sync
        if (listeners.length > 0) {
            // sync plugin data for Mods
            ((IMixinEvent) forgeEvent).syncDataToForge(spongeEvent);
            TimingsManager.MOD_EVENT_HANDLER.startTimingIfSync();
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
                    SpongeImpl.getLogger().catching(throwable);
                }
            }
            TimingsManager.MOD_EVENT_HANDLER.stopTimingIfSync();

            // sync Forge data for Plugins
            ((IMixinEvent) forgeEvent).syncDataToSponge(spongeEvent);
        }

        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false);
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.stopTimingIfSync();

        // sync plugin data for Forge
        ((IMixinEvent) forgeEvent).syncDataToForge(spongeEvent);

        ((IMixinEvent) forgeEvent).postProcess();

        if (spongeEvent instanceof Cancellable && spongeEvent != forgeEvent) {
            if (forgeEvent.isCancelable() && ((Cancellable) spongeEvent).isCancelled()) {
                forgeEvent.setCanceled(true);
            }
        }
        return forgeEvent.isCancelable() && forgeEvent.isCanceled();
    }

    // Uses SpongeForgeEventFactory (required for any events shared in SpongeCommon)
    public boolean post(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz) {
        RegisteredListener.Cache listenerCache = getHandlerCache(spongeEvent);
        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), true, false);
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.stopTimingIfSync();

        TimingsManager.MOD_EVENT_HANDLER.startTimingIfSync();
        SpongeCommonEventFactory.processingInternalForgeEvent = true;
        spongeEvent = SpongeForgeEventFactory.callForgeEvent(spongeEvent, clazz);
        SpongeCommonEventFactory.processingInternalForgeEvent = false;
        TimingsManager.MOD_EVENT_HANDLER.stopTimingIfSync();

        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false);
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.stopTimingIfSync();

        return spongeEvent instanceof Cancellable && ((Cancellable) spongeEvent).isCancelled();
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredListener<?>> listeners, boolean beforeModifications, boolean forced) {
        ModContainer oldContainer = ((IMixinLoadController) SpongeMod.instance.getController()).getActiveModContainer();
        for (@SuppressWarnings("rawtypes")
        RegisteredListener listener : listeners) {
            ((IMixinLoadController) SpongeMod.instance.getController()).setActiveModContainer((ModContainer) listener.getPlugin());
            try {
                if (forced || (!listener.isBeforeModifications() && !beforeModifications)
                        || (listener.isBeforeModifications() && beforeModifications)) {
                    listener.getTimingsHandler().startTimingIfSync();
                    listener.handle(event);
                    listener.getTimingsHandler().stopTimingIfSync();
                }
            } catch (Throwable e) {
                SpongeImpl.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), listener.getPlugin(), e);
            }
        }
        ((IMixinLoadController) SpongeMod.instance.getController()).setActiveModContainer(oldContainer);
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event event) {
        return this.post(event, false);
    }

    @Override
    public boolean post(Event spongeEvent, boolean allowClientThread) {
        if (!allowClientThread & Sponge.getGame().getPlatform().getExecutionType().isClient()) {
            return false;
        }

        if (spongeEvent.getClass().getInterfaces().length > 0) {
            Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz =
                    this.eventMappings.get(spongeEvent.getClass().getInterfaces()[0]);
            if (clazz == null) {
                clazz = SpongeForgeEventFactory.getForgeEventClass(spongeEvent.getClass());
                if (clazz != null) {
                    return post(spongeEvent, clazz);
                }
            } else {
                SpongeCommonEventFactory.processingInternalForgeEvent = true;
                net.minecraftforge.fml.common.eventhandler.Event forgeEvent = SpongeForgeEventFactory.findAndCreateForgeEvent(spongeEvent, clazz);
                SpongeCommonEventFactory.processingInternalForgeEvent = false;
                if (forgeEvent != null) {
                    return post(spongeEvent, forgeEvent,
                            forgeEvent.getListenerList().getListeners(((IMixinEventBus) SpongeForgeEventFactory.getForgeEventBus(forgeEvent.getClass())).getBusID()));
                }
            }
        }
        // no checking for modifications required
        return post(spongeEvent, getHandlerCache(spongeEvent).getListeners(), false, true);
    }

}
