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
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.BlockEvent;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.event.entity.EntityConstructingEvent;
import org.spongepowered.api.event.entity.EntityEvent;
import org.spongepowered.api.event.entity.EntitySpawnEvent;
import org.spongepowered.api.event.entity.living.LivingDeathEvent;
import org.spongepowered.api.event.entity.living.LivingEvent;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerPlaceBlockEvent;
import org.spongepowered.api.event.world.ChunkEvent;
import org.spongepowered.api.event.world.ChunkLoadEvent;
import org.spongepowered.api.event.world.ChunkUnloadEvent;
import org.spongepowered.api.event.world.WorldEvent;
import org.spongepowered.api.event.world.WorldLoadEvent;
import org.spongepowered.api.event.world.WorldUnloadEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.event.RegisteredHandler;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinEventBus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
                    .put(BlockUpdateEvent.class, net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent.class)
                    .put(LivingEvent.class, net.minecraftforge.event.entity.living.LivingEvent.class)
                    // .put(LivingDeathEvent.class, net.minecraftforge.event.entity.living.LivingDeathEvent.class)
                    .put(EntityEvent.class, net.minecraftforge.event.entity.EntityEvent.class)
                    .put(EntityConstructingEvent.class, net.minecraftforge.event.entity.EntityEvent.EntityConstructing.class)
                    .put(EntitySpawnEvent.class, EntityJoinWorldEvent.class)
                    .put(PlayerEvent.class, net.minecraftforge.event.entity.player.PlayerEvent.class)
                    .put(PlayerBreakBlockEvent.class, net.minecraftforge.event.world.BlockEvent.BreakEvent.class)
                    .put(PlayerChatEvent.class, ServerChatEvent.class)
                    .put(PlayerInteractBlockEvent.class, PlayerInteractEvent.class)
                    .put(PlayerPlaceBlockEvent.class, net.minecraftforge.event.world.BlockEvent.PlaceEvent.class)
                    .put(ChunkEvent.class, net.minecraftforge.event.world.ChunkEvent.class)
                    .put(ChunkLoadEvent.class, net.minecraftforge.event.world.ChunkEvent.Load.class)
                    .put(ChunkUnloadEvent.class, net.minecraftforge.event.world.ChunkEvent.Unload.class)
                    .put(WorldEvent.class, net.minecraftforge.event.world.WorldEvent.class)
                    .put(WorldLoadEvent.class, net.minecraftforge.event.world.WorldEvent.Load.class)
                    .put(WorldUnloadEvent.class, net.minecraftforge.event.world.WorldEvent.Unload.class)
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
        this.buildFactoryMethodMappings();
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

        Order orderStart = Order.PRE;
        RegisteredHandler.Cache handlerCache = getHandlerCache(event);

        for (IEventListener listener : listeners) {
            if (listener instanceof EventPriority) {
                Order order = this.priorityMappings.get(listener);

                for (int orderIndex = 0; orderIndex <= order.ordinal(); orderIndex++) {
                    Order currentOrder = Order.values()[orderIndex];
                    post(event, handlerCache.getHandlersByOrder(currentOrder));
                }
                orderStart = Order.values()[order.ordinal() + 1];
            }
            try {
                listener.invoke(forgeEvent);
            } catch (Throwable throwable) {
                SpongeMod.instance.getLogger().catching(throwable);
            }
        }

        for (int orderIndex = orderStart.ordinal(); orderIndex <= Order.POST.ordinal(); orderIndex++) {
            Order currentOrder = Order.values()[orderIndex];
            post(event, handlerCache.getHandlersByOrder(currentOrder));
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
