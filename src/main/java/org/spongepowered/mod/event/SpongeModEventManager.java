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
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.interfaces.IMixinLoadController;

import java.util.List;

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
                    .put(NotifyNeighborBlockEvent.class, BlockEvent.NeighborNotifyEvent.class)
                    .put(TargetChunkEvent.class, ChunkEvent.class)
                    .put(LoadChunkEvent.class, ChunkEvent.Load.class)
                    .put(UnloadChunkEvent.class, ChunkEvent.Unload.class)
                    .put(ConstructEntityEvent.Post.class, EntityEvent.EntityConstructing.class)
                    .put(TargetEntityEvent.class, EntityEvent.class)
                    .put(DestructEntityEvent.Death.class, LivingDeathEvent.class)
                    .put(MessageChannelEvent.Chat.class, ServerChatEvent.class)
                    .put(DropItemEvent.Destruct.class, LivingDropsEvent.class)
                    .put(DropItemEvent.Dispense.class, ItemTossEvent.class)
                    //.put(DropItemEvent.Harvest.class, BlockEvent.HarvestDropsEvent.class)
                    .put(InteractEntityEvent.Primary.class, AttackEntityEvent.class)
                    .put(TargetWorldEvent.class, WorldEvent.class)
                    .put(LoadWorldEvent.class, WorldEvent.Load.class)
                    .put(SaveWorldEvent.class, WorldEvent.Save.class)
                    .put(UnloadWorldEvent.class, WorldEvent.Unload.class)
                    .put(UseItemStackEvent.Start.class, PlayerUseItemEvent.Start.class)
                    .put(UseItemStackEvent.Tick.class, PlayerUseItemEvent.Tick.class)
                    .put(UseItemStackEvent.Stop.class, PlayerUseItemEvent.Stop.class)
                    .put(UseItemStackEvent.Finish.class, PlayerUseItemEvent.Finish.class)
                    .put(ClientConnectionEvent.Join.class, PlayerEvent.PlayerLoggedInEvent.class)
                    .put(ClientConnectionEvent.Disconnect.class, PlayerEvent.PlayerLoggedOutEvent.class)
                    .put(SleepingEvent.Pre.class, PlayerSleepInBedEvent.class)
                    .build();

    public static final ImmutableMap<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>> eventBulkMappings =
            new ImmutableMap.Builder<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>>()
                .put(CollideEntityEvent.class, EntityItemPickupEvent.class)
                .put(InteractBlockEvent.class, PlayerInteractEvent.class)
                .put(InteractBlockEvent.Primary.class, PlayerInteractEvent.class)
                .put(InteractBlockEvent.Secondary.class, PlayerInteractEvent.class)
                .put(InteractEntityEvent.Secondary.class, EntityInteractEvent.class)
                .put(SpawnEntityEvent.class, EntityJoinWorldEvent.class)
                .put(ChangeBlockEvent.Break.class, BlockEvent.BreakEvent.class)
                .put(ChangeBlockEvent.Place.class, BlockEvent.PlaceEvent.class)
                .build();

    private final ImmutableMap<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, EventBus> busMappings =
            new ImmutableMap.Builder<Class<? extends net.minecraftforge.fml.common.eventhandler.Event>, EventBus>()
                    .put(OreGenEvent.class, MinecraftForge.ORE_GEN_BUS)
                    .put(WorldTypeEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(BiomeEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(DecorateBiomeEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(InitMapGenEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(InitNoiseGensEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(PopulateChunkEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(SaplingGrowTreeEvent.class, MinecraftForge.TERRAIN_GEN_BUS)
                    .put(PlayerEvent.class, MinecraftForge.EVENT_BUS)
                    .build();

    @Inject
    public SpongeModEventManager(PluginManager pluginManager) {
        super(pluginManager);
    }

    public boolean post(Event spongeEvent, net.minecraftforge.fml.common.eventhandler.Event forgeEvent, IEventListener[] listeners) {
        checkNotNull(forgeEvent, "forgeEvent");

        if (spongeEvent == null) { // Fired by Forge
            spongeEvent = ((IMixinEvent) forgeEvent).createSpongeEvent();
        }
        RegisteredListener.Cache listenerCache = getHandlerCache(spongeEvent);
        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), true, false);
        }

        // If there are no forge listeners for event, skip sync
        if (listeners.length > 0) {
            // sync plugin data for Mods
            ((IMixinEvent) forgeEvent).syncDataToForge(spongeEvent);

            for (IEventListener listener : listeners) {
                try {
                    listener.invoke(forgeEvent);
                } catch (Throwable throwable) {
                    SpongeImpl.getLogger().catching(throwable);
                }
            }

            // sync Forge data for Plugins
            ((IMixinEvent) forgeEvent).syncDataToSponge(spongeEvent);
        }

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false);
        }

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

    public boolean postBulk(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz) {
        RegisteredListener.Cache listenerCache = getHandlerCache(spongeEvent);
        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), true, false);
        }

        StaticMixinHelper.processingInternalForgeEvent = true;
        spongeEvent = SpongeForgeEventFactory.callForgeEvent(spongeEvent, clazz);
        StaticMixinHelper.processingInternalForgeEvent = false;

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false);
        }

        return spongeEvent instanceof Cancellable && ((Cancellable) spongeEvent).isCancelled();
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredListener<?>> listeners, boolean beforeModifications, boolean forced) {
        ModContainer oldContainer = ((IMixinLoadController) SpongeMod.instance.getController()).getActiveModContainer();
        for (@SuppressWarnings("rawtypes") RegisteredListener listener : listeners) {
            ((IMixinLoadController) SpongeMod.instance.getController()).setActiveModContainer((ModContainer) listener.getPlugin());
            try {
                if (forced || (!listener.isBeforeModifications() && !beforeModifications) || (listener.isBeforeModifications() && beforeModifications)) {
                    listener.handle(event);
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

        if(spongeEvent.getClass().getInterfaces().length > 0) {
            Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz = this.eventMappings.get(spongeEvent.getClass().getInterfaces()[0]);
            if (clazz == null) {
                clazz = eventBulkMappings.get(spongeEvent.getClass().getInterfaces()[0]);
                if (clazz != null) {
                    return postBulk(spongeEvent, clazz);
                }
            } else {
                StaticMixinHelper.processingInternalForgeEvent = true;
                net.minecraftforge.fml.common.eventhandler.Event forgeEvent = SpongeForgeEventFactory.findAndCreateForgeEvent(spongeEvent, clazz);
                StaticMixinHelper.processingInternalForgeEvent = false;
                if (forgeEvent != null) {
                    // Avoid separate mappings for events defined as inner classes
                    Class<?> enclosingClass = forgeEvent.getClass().getEnclosingClass();
                    EventBus bus = this.busMappings.get(enclosingClass == null ? forgeEvent.getClass() : enclosingClass);
                    if (bus == null) {
                        bus = MinecraftForge.EVENT_BUS;
                    }

                    return post(spongeEvent, forgeEvent, forgeEvent.getListenerList().getListeners(((IMixinEventBus) bus).getBusID()));
                }
            }
        }
        return post(spongeEvent, getHandlerCache(spongeEvent).getListeners(), false, true); // no checking for modifications required
    }

}
