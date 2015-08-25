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
import com.google.common.collect.Maps;
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
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.source.block.BlockEvent;
import org.spongepowered.api.event.source.block.BlockUpdateNeighborBlockEvent;
import org.spongepowered.api.event.source.entity.EntityEvent;
import org.spongepowered.api.event.source.entity.living.LivingEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerBreakBlockEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerChatEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerHarvestBlockEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerPlaceBlockEvent;
import org.spongepowered.api.event.source.world.WorldEvent;
import org.spongepowered.api.event.target.entity.ConstructEntityEvent;
import org.spongepowered.api.event.target.entity.SpawnEntityEvent;
import org.spongepowered.api.event.target.world.LoadWorldEvent;
import org.spongepowered.api.event.target.world.UnloadWorldEvent;
import org.spongepowered.api.event.target.world.chunk.ChangeChunkEvent;
import org.spongepowered.api.event.target.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.target.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.event.RegisteredHandler;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.interfaces.IMixinEventBus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class SpongeModEventManager extends SpongeEventManager {

    private static final String PACKAGE_PREFIX = "org.spongepowered.api.event.impl";

    private final ImmutableBiMap<EventPriority, Order> priorityMappings = new ImmutableBiMap.Builder<EventPriority, Order>()
            .put(EventPriority.HIGHEST, Order.FIRST)
            .put(EventPriority.HIGH, Order.EARLY)
            .put(EventPriority.NORMAL, Order.DEFAULT)
            .put(EventPriority.LOW, Order.LATE)
            .put(EventPriority.LOWEST, Order.LAST)
            .build();

    private final ImmutableMap<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>> eventMappings =
            new ImmutableMap.Builder<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>>()
                    .put(BlockEvent.class, net.minecraftforge.event.world.BlockEvent.class)
                    .put(BlockUpdateNeighborBlockEvent.class, net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent.class)
                    .put(ChangeChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.class)
                    .put(LoadChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.Load.class)
                    .put(UnloadChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.Unload.class)
                    .put(ConstructEntityEvent.class, net.minecraftforge.event.entity.EntityEvent.EntityConstructing.class)
                    .put(EntityEvent.class, net.minecraftforge.event.entity.EntityEvent.class)
                    .put(SpawnEntityEvent.class, net.minecraftforge.event.entity.EntityJoinWorldEvent.class)
                    .put(LivingEvent.class, net.minecraftforge.event.entity.living.LivingEvent.class)
                    .put(PlayerBreakBlockEvent.class, net.minecraftforge.event.world.BlockEvent.BreakEvent.class)
                    .put(PlayerChatEvent.class, net.minecraftforge.event.ServerChatEvent.class)
                    .put(PlayerEvent.class, net.minecraftforge.event.entity.player.PlayerEvent.class)
                    .put(PlayerHarvestBlockEvent.class, net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent.class)
                    .put(PlayerInteractBlockEvent.class, net.minecraftforge.event.entity.player.PlayerInteractEvent.class)
                    .put(PlayerPlaceBlockEvent.class, net.minecraftforge.event.world.BlockEvent.PlaceEvent.class)
                    .put(WorldEvent.class, net.minecraftforge.event.world.WorldEvent.class)
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

    private final Map<Class<? extends Event>, Method> factoryMethodMappings = Maps.newHashMap();

    @Inject
    public SpongeModEventManager(PluginManager pluginManager) {
        super(pluginManager);
        // this.buildFactoryMethodMappings(); disable for now
    }

    private void buildFactoryMethodMappings() {
        for (Map.Entry<Class<? extends Event>, Class<? extends net.minecraftforge.fml.common.eventhandler.Event>> entry : this.eventMappings
                .entrySet()) {
            try {
                Method method = entry.getValue().getDeclaredMethod("fromSpongeEvent", entry.getKey());
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException(String.format("Method %s must be static!", method));
                } else if (!method.getReturnType().equals(entry.getValue())) {
                    throw new IllegalStateException(String.format("Method %s has an invalid signature!"));
                }

                method.setAccessible(true);

                this.factoryMethodMappings.put(entry.getKey(), method);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(String.format("Unable to locate method fromSpongeEvent in class %s!", entry.getValue()), e);
            }
        }
    }

    public boolean post(net.minecraftforge.fml.common.eventhandler.Event forgeEvent, IEventListener[] listeners) {
        checkNotNull(forgeEvent, "forgeEvent");
        Event event = (Event) forgeEvent;
        RegisteredHandler.Cache handlerCache = getHandlerCache(event);

        // Fire events to plugins before modifications
        for (Order order : Order.values()) {
            postBeforeModifications(event, handlerCache.getHandlersByOrder(order));
        }

        // Sync plugin data then fire off to Forge
        // TODO: finish other events
        if (event instanceof PlayerBreakBlockEvent || event instanceof PlayerPlaceBlockEvent) {
            forgeEvent = ((IMixinEvent) event).fromSpongeEvent(event);
            event = (Event) forgeEvent;
            }
        for (IEventListener listener : listeners) {
            try {
                listener.invoke(forgeEvent);
            } catch (Throwable throwable) {
                SpongeMod.instance.getLogger().catching(throwable);
            }
        }

        // Fire events to plugins after modifications (default)
        for (Order order : Order.values()) {
            post(event, handlerCache.getHandlersByOrder(order));
        }

        return forgeEvent.isCancelable() && forgeEvent.isCanceled();
    }

    private boolean postForgeEvent(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        // Avoid separate mappings for events defined as inner classes
        Class<?> enclosingClass = forgeEvent.getClass().getEnclosingClass();
        EventBus bus = this.busMappings.get(enclosingClass == null ? forgeEvent.getClass() : enclosingClass);
        if (bus == null) {
            bus = MinecraftForge.EVENT_BUS;
        }
        return post(forgeEvent, forgeEvent.getListenerList().getListeners(((IMixinEventBus) bus).getBusID()));
    }

    @SuppressWarnings("unchecked")
    protected static boolean postBeforeModifications(Event event, List<RegisteredHandler<?>> handlers) {
        for (@SuppressWarnings("rawtypes") RegisteredHandler handler : handlers) {
            try {
                if (handler.isBeforeModifications()) {
                    handler.handle(event);
                }
            } catch (Throwable e) {
                Sponge.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredHandler<?>> handlers) {
        for (@SuppressWarnings("rawtypes") RegisteredHandler handler : handlers) {
            try {
                if (!handler.isBeforeModifications()) {
                    handler.handle(event);
                }
            } catch (Throwable e) {
                Sponge.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event event) {
        if (event.getClass().getCanonicalName().startsWith(PACKAGE_PREFIX)) {
            if (!(event.getClass().getInterfaces().length == 1)) {
                throw new IllegalArgumentException("This isn't a real generated event class at all! "
                        + "Any class created by the event generator should implement exactly one interface! "
                        + "(If you're a plugin, nice try.)");
            }
            Class<?> interfaceClazz = event.getClass().getInterfaces()[0];
            if (this.factoryMethodMappings.containsKey(interfaceClazz)) {
                try {
                    net.minecraftforge.fml.common.eventhandler.Event forgeEvent =
                            (net.minecraftforge.fml.common.eventhandler.Event) this.factoryMethodMappings.get(interfaceClazz).invoke(null, event);
                    return postForgeEvent(forgeEvent);
                } catch (Throwable throwable) {
                    Sponge.getLogger().error("Could not post {}", event.getClass().getSimpleName(), throwable);
                }
            }
        }
        if (event instanceof net.minecraftforge.fml.common.eventhandler.Event) {
            return postForgeEvent((net.minecraftforge.fml.common.eventhandler.Event) event);
        }
        return super.post(event);
    }

}
