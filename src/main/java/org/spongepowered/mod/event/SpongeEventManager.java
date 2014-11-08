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
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.mod.SpongeGame;
import org.spongepowered.mod.asm.util.ASMEventListenerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SpongeEventManager implements EventManager {

    private final SpongeGame game;

    private final Map<Object, List<PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>>> forgePluginHandlerMap =
            Maps.newHashMap();
    private final SpongeEventBus eventBus = new SpongeEventBus();

    private final Map<Object, List<EventListener<FMLEvent>>> fmlPluginHandlerMap = Maps.newHashMap();
    private final Map<Class<? extends FMLEvent>, List<PriorityEventListener<FMLEvent>>> fmlEventHandlerMap =
            Maps.newHashMap();

    public SpongeEventManager(SpongeGame game) {
        this.game = game;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(Object o) {
        if (forgePluginHandlerMap.containsKey(o)) {
            return;
        }

        List<PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>> localForgeListeners =
                Lists.newArrayList();
        List<EventListener<FMLEvent>> localFMLListeners = Lists.newArrayList();

        Map<Method, Subscribe> annotationMap = getAnnotationMap(o);

        for (Entry<Method, Subscribe> entry : annotationMap.entrySet()) {
            Class<?>[] parameters = entry.getKey().getParameterTypes();

            if (parameters.length != 1) {
                throw new IllegalArgumentException("Handler methods may only have 1 input parameter");
            }

            Class<?> eventType = parameters[0];
            Class<?> implementingEvent = EventRegistry.getImplementingClass(eventType);

            if (implementingEvent == null) {
                implementingEvent = eventType;
            }

            if (implementingEvent == null) {
                game.getLogger().warn("Unknown event type " + eventType.getCanonicalName() + ", registration failed");
            } else if (net.minecraftforge.fml.common.eventhandler.Event.class.isAssignableFrom(implementingEvent)) {
                // Forge events
                EventListener<net.minecraftforge.fml.common.eventhandler.Event> listener =
                        ASMEventListenerFactory.getListener(EventListener.class, o, entry.getKey());

                PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event> priorityListener =
                        new PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>(entry.getValue().order(), listener);

                eventBus.add(implementingEvent, entry.getValue(), priorityListener);
                localForgeListeners.add(priorityListener);
            } else if (FMLEvent.class.isAssignableFrom(implementingEvent)) {
                // FMLEvents
                //
                // FMLEvens are handled using a hash-map lookup to a listener list

                EventListener<FMLEvent> listener =
                        (EventListener<FMLEvent>) ASMEventListenerFactory.getListener(EventListener.class, o, entry.getKey());
                PriorityEventListener<FMLEvent> priorityListener = new PriorityEventListener<FMLEvent>(entry.getValue().order(), listener);

                localFMLListeners.add(listener);

                List<PriorityEventListener<FMLEvent>> listenerList = fmlEventHandlerMap.get(implementingEvent);
                if (listenerList == null) {
                    listenerList = Lists.newArrayList();
                    fmlEventHandlerMap.put((Class<? extends FMLEvent>) implementingEvent, listenerList);
                }
                listenerList.add(priorityListener);
                Collections.sort(listenerList);

            }
        }
        fmlPluginHandlerMap.put(o, localFMLListeners);
        forgePluginHandlerMap.put(o, localForgeListeners);
    }

    @Override
    public void unregister(Object o) {
        List<PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event>> pluginForgeListeners = forgePluginHandlerMap.remove(o);
        if (pluginForgeListeners == null) {
            return;
        }
        for (PriorityEventListener<net.minecraftforge.fml.common.eventhandler.Event> listener : pluginForgeListeners) {
            eventBus.remove(listener);
        }

        List<EventListener<FMLEvent>> pluginFMLListeners = fmlPluginHandlerMap.remove(o);
        if (pluginFMLListeners == null) {
            throw new IllegalStateException("Forge and FML maps out of alignment");
        }
        for (List<PriorityEventListener<FMLEvent>> eventListeners : fmlEventHandlerMap.values()) {
            eventListeners.removeAll(pluginFMLListeners);
        }
    }

    @Override
    public boolean call(Event spongeEvent) {
        if (spongeEvent instanceof net.minecraftforge.fml.common.eventhandler.Event) {
            FMLCommonHandler.instance().bus().post((net.minecraftforge.fml.common.eventhandler.Event) spongeEvent);
        } else {
            game.getLogger().info("Event not a sub-classes of forge Event or BaseEvent");
            return false;
        }
        return true;
    }

    public SpongeGame getGame() {
        return game;
    }

    @com.google.common.eventbus.Subscribe
    public void onFMLEvent(FMLEvent event) {
        // FML events are rare, so do not use the high speed event system
        //
        // This event manager subscribes to the FML bus and forwards all events onward

        List<PriorityEventListener<FMLEvent>> listeners = fmlEventHandlerMap.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (EventListener<FMLEvent> listener : listeners) {
            try {
                listener.invoke(event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
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
