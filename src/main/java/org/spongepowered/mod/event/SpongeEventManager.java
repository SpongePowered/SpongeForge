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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.api.event.BaseEvent;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.SpongeEventHandler;
import org.spongepowered.mod.SpongeGame;
import org.spongepowered.mod.asm.util.ASMEventListenerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;

import cpw.mods.fml.common.event.FMLEvent;

public class SpongeEventManager implements EventManager {

    private final SpongeGame game;

    private final Map<Object, List<PriorityEventListener<cpw.mods.fml.common.eventhandler.Event>>> forgePluginHandlerMap =
            new HashMap<Object, List<PriorityEventListener<cpw.mods.fml.common.eventhandler.Event>>>();
    private final SpongeEventBus eventBus = new SpongeEventBus();

    private final Map<Object, List<EventListener<FMLEvent>>> fmlPluginHandlerMap = new HashMap<Object, List<EventListener<FMLEvent>>>();
    private final Map<Class<? extends FMLEvent>, List<PriorityEventListener<FMLEvent>>> fmlEventHandlerMap = 
            new HashMap<Class<? extends FMLEvent>, List<PriorityEventListener<FMLEvent>>>();

    public SpongeEventManager(SpongeGame game) {
        this.game = game;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(Object o) {
        if (forgePluginHandlerMap.containsKey(o)) {
            return;
        }

        List<PriorityEventListener<cpw.mods.fml.common.eventhandler.Event>> localForgeListeners = 
                new ArrayList<PriorityEventListener<cpw.mods.fml.common.eventhandler.Event>>();
        List<EventListener<FMLEvent>> localFMLListeners = new ArrayList<EventListener<FMLEvent>>();

        Map<Method, SpongeEventHandler> annotationMap = getAnnotationMap(o);
        
        for (Entry<Method, SpongeEventHandler> entry : annotationMap.entrySet()) {
            Class<?>[] parameters = entry.getKey().getParameterTypes();

            if (parameters.length != 1) {
                throw new IllegalArgumentException("Handler methods may only have 1 input parameter");
            }

            Class<?> eventType = parameters[0];
            Class<?> implementingEvent;

            if (BaseEvent.class.isAssignableFrom(eventType)) {
                implementingEvent = eventType;
            } else {
                implementingEvent = EventRegistry.getImplementingClass(eventType);
            }
            
            if (implementingEvent == null) {
                game.getLogger().warn("Unknown event type " + eventType.getCanonicalName() + ", registration failed");
            } else if (BaseEvent.class.equals(implementingEvent)) {
                game.getLogger().warn("Handlers may not listen for BaseEvent directly, registration failed");
            } else if (cpw.mods.fml.common.eventhandler.Event.class.isAssignableFrom(implementingEvent)) {
                // Forge events
                EventListener<cpw.mods.fml.common.eventhandler.Event> listener = 
                        ASMEventListenerFactory.getListener(EventListener.class, o, entry.getKey());

                PriorityEventListener<cpw.mods.fml.common.eventhandler.Event> priorityListener = 
                        new PriorityEventListener<cpw.mods.fml.common.eventhandler.Event>(entry.getValue().order(), listener);

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
                    listenerList = new ArrayList<PriorityEventListener<FMLEvent>>();
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
        List<PriorityEventListener<cpw.mods.fml.common.eventhandler.Event>> pluginForgeListeners = forgePluginHandlerMap.remove(o);
        if (pluginForgeListeners == null) {
            return;
        }
        for (PriorityEventListener<cpw.mods.fml.common.eventhandler.Event> listener : pluginForgeListeners) {
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
        if (spongeEvent instanceof cpw.mods.fml.common.eventhandler.Event) {
            MinecraftForge.EVENT_BUS.post((cpw.mods.fml.common.eventhandler.Event) spongeEvent);
        } else {
            game.getLogger().info("Event not a sub-classes of forge Event or BaseEvent");
            return false;
        }
        return true;
    }

    public SpongeGame getGame() {
        return game;
    }

    @Subscribe
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



    private Map<Method, SpongeEventHandler> getAnnotationMap(Object o) {
        Map<Method, SpongeEventHandler> map = new HashMap<Method, SpongeEventHandler>();

        Set<? extends Class<?>> superClasses = TypeToken.of(o.getClass()).getTypes().rawTypes();

        for (Method method : o.getClass().getMethods()) {
            for (Class<?> clazz : superClasses) {
                try {
                    Method localMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());

                    if (localMethod.isAnnotationPresent(SpongeEventHandler.class)) {
                        SpongeEventHandler annotation = localMethod.getAnnotation(SpongeEventHandler.class);
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
