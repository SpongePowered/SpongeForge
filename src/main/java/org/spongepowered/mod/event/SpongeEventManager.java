/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLEvent;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.event.Event;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.asm.util.ASMEventListenerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeEventManager implements EventManager {

    private final Map<Object, List<PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>>> forgePluginHandlerMap =
            Maps.newHashMap();
    private final SpongeEventBus eventBus = new SpongeEventBus();

    @SuppressWarnings("unchecked")
    @Override
    public void register(Object plugin, Object o) {
        if (forgePluginHandlerMap.containsKey(o)) {
            return;
        }

        List<PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>> localForgeListeners =
                Lists.newArrayList();

        Map<Method, Subscribe> annotationMap = getAnnotationMap(o);

        for (Entry<Method, Subscribe> entry : annotationMap.entrySet()) {
            Class<?>[] parameters = entry.getKey().getParameterTypes();

            if (parameters.length != 1) {
                throw new IllegalArgumentException("Handler methods may only have 1 input parameter");
            }

            @Nullable
            Class<?> eventType = parameters[0];
            @Nullable
            Class<?> implementingEvent = EventRegistry.getImplementingClass(eventType);

            if (implementingEvent == null) {
                implementingEvent = eventType;
            }

            if (implementingEvent == null) {
                SpongeMod.instance.getLogger().error("Null event type, registration failed");
            } else if (net.minecraftforge.fml.common.eventhandler.Event.class.isAssignableFrom(implementingEvent)) {
                // Forge events
                EventListener<net.minecraftforge.fml.common.eventhandler.Event> listener =
                        ASMEventListenerFactory.getListener(EventListener.class, o, entry.getKey());

                PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event> priorityListener =
                        new PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>(entry.getValue().order(), listener);

                eventBus.add(implementingEvent, entry.getValue(), priorityListener);
                localForgeListeners.add(priorityListener);
            } else if (!FMLEvent.class.isAssignableFrom(implementingEvent)) {
                // ^ Events extending FMLEvent need to be handled on a per-plugin basis. Current code for that is in SpongePluginHandler.
                SpongeMod.instance.getLogger().warn("Unknown event type " + eventType.getCanonicalName() + ", registration failed");
            }
        }
        forgePluginHandlerMap.put(o, localForgeListeners);
    }

    @Override
    public void unregister(Object o) {
        @Nullable
        List<PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>> pluginForgeListeners = forgePluginHandlerMap.remove(o);
        if (pluginForgeListeners == null) {
            return;
        }
        for (PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event> listener : pluginForgeListeners) {
            eventBus.remove(listener);
        }
    }

    @Override
    public boolean post(Event spongeEvent) {
        if (spongeEvent instanceof net.minecraftforge.fml.common.eventhandler.Event) {
            FMLCommonHandler.instance().bus().post((net.minecraftforge.fml.common.eventhandler.Event) spongeEvent);
        } else {
            SpongeMod.instance.getLogger().info("Event not a sub-classes of forge Event or BaseEvent");
            return false;
        }
        return true;
    }

    private Map<Method, Subscribe> getAnnotationMap(Object o) {
        Map<Method, Subscribe> map = new HashMap<Method, Subscribe>();

        Set<? extends Class<?>> superClasses = TypeToken.of(o.getClass()).getTypes().rawTypes();

        for (Method method : o.getClass().getMethods()) {
            for (Class<?> clazz : superClasses) {
                try {
                    Method localMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());

                    if (localMethod.isAnnotationPresent(Subscribe.class)) {
                        Subscribe annotation = localMethod.getAnnotation(Subscribe.class);
                        map.put(method, annotation);
                        break;
                    }
                } catch (NoSuchMethodException e) {
                    ;
                }
            }
        }
        return map;
    }

}
