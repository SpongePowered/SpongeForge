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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.interfaces.IMixinEventBus;

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
                    .put(ChangeBlockEvent.Break.class, BlockEvent.BreakEvent.class)
                    .put(MessageSinkEvent.class, ServerChatEvent.class)
                    //.put(DropItemEvent.Harvest.class, BlockEvent.HarvestDropsEvent.class)
                    .put(InteractBlockEvent.class, PlayerInteractEvent.class)
                    .put(InteractEntityEvent.Secondary.class, EntityInteractEvent.class)
                    .put(ChangeBlockEvent.Place.class, BlockEvent.PlaceEvent.class)
                    .put(TargetWorldEvent.class, WorldEvent.class)
                    .put(LoadWorldEvent.class, WorldEvent.Load.class)
                    .put(UnloadWorldEvent.class, WorldEvent.Unload.class)
                    .put(UseItemStackEvent.Start.class, PlayerUseItemEvent.Start.class)
                    .put(UseItemStackEvent.Tick.class, PlayerUseItemEvent.Tick.class)
                    .put(UseItemStackEvent.Stop.class, PlayerUseItemEvent.Stop.class)
                    .put(UseItemStackEvent.Finish.class, PlayerUseItemEvent.Finish.class)
                    .put(ClientConnectionEvent.Join.class, PlayerEvent.PlayerLoggedInEvent.class)
                    .put(ClientConnectionEvent.Disconnect.class, PlayerEvent.PlayerLoggedOutEvent.class)
                    .build();

    public static final ImmutableBiMap<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>> eventBulkMappings =
            new ImmutableBiMap.Builder<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>>()
                .put(CollideEntityEvent.class, EntityItemPickupEvent.class)
                .put(SpawnEntityEvent.class, EntityJoinWorldEvent.class)
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
                    .put(PlayerEvent.class, FMLCommonHandler.instance().bus())
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

        // sync plugin data for Mods
        ((IMixinEvent) forgeEvent).syncDataToForge(spongeEvent);

        for (IEventListener listener : listeners) {
            try {
                listener.invoke(forgeEvent);
            } catch (Throwable throwable) {
                SpongeMod.instance.getLogger().catching(throwable);
            }
        }

        // sync Forge data for Plugins
        ((IMixinEvent)spongeEvent).syncDataToSponge(forgeEvent);

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false);
        }

        // sync plugin data for Forge
        ((IMixinEvent) forgeEvent).syncDataToForge(spongeEvent);

        if (spongeEvent instanceof Cancellable) {
            if (((Cancellable) spongeEvent).isCancelled()) {
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

        spongeEvent = SpongeForgeEventFactory.callForgeEvent(spongeEvent, clazz);

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(spongeEvent, listenerCache.getListenersByOrder(order), false, false);
        }

        return spongeEvent instanceof Cancellable && ((Cancellable) spongeEvent).isCancelled();
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredListener<?>> listeners, boolean beforeModifications, boolean forced) {
        for (@SuppressWarnings("rawtypes")
        RegisteredListener listener : listeners) {
            try {
                if (forced || (!listener.isBeforeModifications() && !beforeModifications) || (listener.isBeforeModifications() && beforeModifications)) {
                    listener.handle(event);
                }
            } catch (Throwable e) {
                Sponge.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), listener.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event spongeEvent) {
        if (SpongeMod.instance.isClientThread()) {
            return false;
        }

        Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz = this.eventMappings.get(spongeEvent.getClass().getInterfaces()[0]);
        if (clazz == null) {
            clazz = eventBulkMappings.get(spongeEvent.getClass().getInterfaces()[0]);
            if (clazz != null) {
                return postBulk(spongeEvent, clazz);
            }
        } else {
            net.minecraftforge.fml.common.eventhandler.Event forgeEvent = SpongeForgeEventFactory.findAndCreateForgeEvent(spongeEvent, clazz);
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
        return super.post(spongeEvent, getHandlerCache(spongeEvent).getListeners()); // no checking for modifications required
    }

}
