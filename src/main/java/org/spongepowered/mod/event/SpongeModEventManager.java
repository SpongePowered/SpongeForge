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
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.BreakBlockEvent;
import org.spongepowered.api.event.block.HarvestBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.PlaceBlockEvent;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
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
                     //.put(BlockEvent.class, net.minecraftforge.event.world.BlockEvent.class)
                    .put(NotifyNeighborBlockEvent.class, net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent.class)
                    .put(TargetChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.class)
                    .put(LoadChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.Load.class)
                    .put(UnloadChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.Unload.class)
                    .put(ConstructEntityEvent.Post.class, net.minecraftforge.event.entity.EntityEvent.EntityConstructing.class)
                    .put(TargetEntityEvent.class, net.minecraftforge.event.entity.EntityEvent.class)
                    .put(SpawnEntityEvent.class, net.minecraftforge.event.entity.EntityJoinWorldEvent.class)
                    .put(BreakBlockEvent.class, net.minecraftforge.event.world.BlockEvent.BreakEvent.class)
                    .put(MessageSinkEvent.class, net.minecraftforge.event.ServerChatEvent.class)
                    .put(HarvestBlockEvent.class, net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent.class)
                    .put(InteractBlockEvent.class, net.minecraftforge.event.entity.player.PlayerInteractEvent.class)
                    .put(PlaceBlockEvent.class, net.minecraftforge.event.world.BlockEvent.PlaceEvent.class)
                    .put(TargetWorldEvent.class, net.minecraftforge.event.world.WorldEvent.class)
                    .put(LoadWorldEvent.class, net.minecraftforge.event.world.WorldEvent.Load.class)
                    .put(UnloadWorldEvent.class, net.minecraftforge.event.world.WorldEvent.Unload.class)
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
                    .put(net.minecraftforge.fml.common.gameevent.PlayerEvent.class, FMLCommonHandler.instance().bus())
                    .build();

    @Inject
    public SpongeModEventManager(PluginManager pluginManager) {
        super(pluginManager);
    }

    public boolean post(net.minecraftforge.fml.common.eventhandler.Event forgeEvent, IEventListener[] listeners) {
        checkNotNull(forgeEvent, "forgeEvent");
        RegisteredListener.Cache listenerCache = getHandlerCache((Event) forgeEvent);

        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            postBeforeModifications((Event) forgeEvent, listenerCache.getListenersByOrder(order));
        }

        // sync plugin data for Mods
        ((IMixinEvent) forgeEvent).syncDataToForge();

        for (IEventListener listener : listeners) {
            try {
                listener.invoke(forgeEvent);
            } catch (Throwable throwable) {
                SpongeMod.instance.getLogger().catching(throwable);
            }
        }

        // sync Forge data for Plugins
        ((IMixinEvent) forgeEvent).syncDataToSponge();

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post((Event) forgeEvent, listenerCache.getListenersByOrder(order));
        }

        // sync plugin data for Forge
        ((IMixinEvent) forgeEvent).syncDataToForge();

        return forgeEvent.isCancelable() && forgeEvent.isCanceled();
    }

    @SuppressWarnings("unchecked")
    protected static boolean postBeforeModifications(Event event, List<RegisteredListener<?>> listeners) {
        for (@SuppressWarnings("rawtypes")
        RegisteredListener listener : listeners) {
            try {
                if (listener.isBeforeModifications()) {
                    listener.handle(event);
                }
            } catch (Throwable e) {
                Sponge.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), listener.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredListener<?>> listeners) {
        for (@SuppressWarnings("rawtypes")
        RegisteredListener listener : listeners) {
            try {
                if (!listener.isBeforeModifications()) {
                    listener.handle(event);
                }
            } catch (Throwable e) {
                Sponge.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), listener.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event event) {
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            return false;
        }

        Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz = this.eventMappings.get(event.getClass().getInterfaces()[0]);
        if (clazz != null) {
            net.minecraftforge.fml.common.eventhandler.Event forgeEvent = SpongeForgeEventFactory.findAndCreateForgeEvent(event, clazz);
            if (forgeEvent != null) {
                // Avoid separate mappings for events defined as inner classes
                Class<?> enclosingClass = forgeEvent.getClass().getEnclosingClass();
                EventBus bus = this.busMappings.get(enclosingClass == null ? forgeEvent.getClass() : enclosingClass);
                if (bus == null) {
                    bus = MinecraftForge.EVENT_BUS;
                }
                return post(forgeEvent, forgeEvent.getListenerList().getListeners(((IMixinEventBus) bus).getBusID()));
            }
        }
        return super.post(event);
    }

}
