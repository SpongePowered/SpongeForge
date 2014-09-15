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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.SpongeEventHandler;
import org.spongepowered.mod.SpongeGame;
import org.spongepowered.mod.asm.EventListener;
import org.spongepowered.mod.asm.util.ASMEventListenerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;

import cpw.mods.fml.common.event.FMLEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.ListenerList;

public class SpongeEventManager implements EventManager {

    private final SpongeGame game;

    private final Map<Object, List<IEventListener>> forgePluginHandlerMap = new HashMap<Object, List<IEventListener>>();

    private final Map<Object, List<EventListener<FMLEvent>>> fmlPluginHandlerMap = new HashMap<Object, List<EventListener<FMLEvent>>>();
    private final Map<Class<? extends FMLEvent>, List<EventListener<FMLEvent>>> fmlEventHandlerMap = new HashMap<Class<? extends FMLEvent>, List<EventListener<FMLEvent>>>();

    public SpongeEventManager(SpongeGame game) {
        this.game = game;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(Object o) {
        if (forgePluginHandlerMap.containsKey(o)) {
            return;
        }

        List<IEventListener> localForgeListeners = new ArrayList<IEventListener>();
        List<EventListener<FMLEvent>> localFMLListeners = new ArrayList<EventListener<FMLEvent>>();

        Map<Method, SpongeEventHandler> annotationMap = getAnnotationMap(o);
        for (Entry<Method, SpongeEventHandler> entry : annotationMap.entrySet()) {
            Class<?>[] parameters = entry.getKey().getParameterTypes();

            if (parameters.length != 1) {
                throw new IllegalArgumentException("Handler methods may only have 1 input parameter");
            }

            Class<?> eventType = parameters[0];
            Class<?> implementingEvent = EventRegistry.getImplementingClass(eventType);

            if (implementingEvent == null) {
                game.getLogger().warn("Unknown event type " + eventType.getCanonicalName() + ", registration failed");
            } else if (cpw.mods.fml.common.eventhandler.Event.class.isAssignableFrom(implementingEvent)) {
                // Forge events
                //
                // Forge events are handled by directly subscribing to the ListenerLists for the events
                
                final IEventListener listener = ASMEventListenerFactory.getListener(IEventListener.class, o, entry.getKey());

                localForgeListeners.add(listener);

                // Create event in order to call .getListenerList() for that event
                cpw.mods.fml.common.eventhandler.Event event = createEvent(implementingEvent);
                if (event == null) {
                    game.getLogger().error("Unable to create event of type " + implementingEvent.getCanonicalName() + " for registration");
                    continue;
                }

                // Add the listener directly to the ListenerList
                int busID = getForgeEventBusId();
                ListenerList listeners = event.getListenerList();
                listeners.register(busID, EventPriority.NORMAL, listener);
            } else if (FMLEvent.class.isAssignableFrom(implementingEvent)){
                // FMLEvents
                //
                // FMLEvens are handled using a hash-map lookup to a listener list
                
                EventListener<FMLEvent> listener = (EventListener<FMLEvent>) ASMEventListenerFactory.getListener(EventListener.class, o, entry.getKey());
                
                localFMLListeners.add(listener);
                
                List<EventListener<FMLEvent>> listenerList = fmlEventHandlerMap.get(implementingEvent);
                if (listenerList == null) {
                    listenerList = new ArrayList<EventListener<FMLEvent>>();
                    fmlEventHandlerMap.put((Class<? extends FMLEvent>) implementingEvent, listenerList);
                }
                listenerList.add(listener);
            }
        }
        fmlPluginHandlerMap.put(o, localFMLListeners);
        forgePluginHandlerMap.put(o, localForgeListeners);
    }

    @Override
    public void unregister(Object o) {
        List<IEventListener> pluginForgeListeners = forgePluginHandlerMap.remove(o);
        if (pluginForgeListeners == null) {
            return;
        }
        int busId = getForgeEventBusId();
        for (IEventListener listener : pluginForgeListeners) {
            ListenerList.unregisterAll(busId, listener);
        }
        
        List<EventListener<FMLEvent>> pluginFMLListeners = fmlPluginHandlerMap.remove(o);
        if (pluginFMLListeners == null) {
            throw new IllegalStateException("Forge and FML maps out of alignment");
        }
        for (List<EventListener<FMLEvent>> eventListeners : fmlEventHandlerMap.values()) {
            eventListeners.removeAll(pluginFMLListeners);
        }
    }

    @Override
    public boolean call(Event spongeEvent) {
        // TODO - support for events generated by plugins or the Sponge mod
        game.getLogger().warn("Warning custom events are not supported");
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
        
        List<EventListener<FMLEvent>> listeners = fmlEventHandlerMap.get(event.getClass());
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
                }
            }
        }
        return map;
    }

    private static cpw.mods.fml.common.eventhandler.Event createEvent(Class<?> implementingEvent) {
        try {
            return (cpw.mods.fml.common.eventhandler.Event) implementingEvent.newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    private static int getForgeEventBusId() {
        Field field;
        try {
            field = EventBus.class.getDeclaredField("busID");
            field.setAccessible(true);
            return (Integer) field.get(MinecraftForge.EVENT_BUS);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
