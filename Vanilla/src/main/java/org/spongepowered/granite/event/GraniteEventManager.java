/*
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
package org.spongepowered.granite.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.util.event.Cancellable;
import org.spongepowered.api.util.event.Event;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.granite.Granite;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GraniteEventManager implements EventManager {

    private final Object lock = new Object();

    private final Granite granite;
    private final PluginManager pluginManager;
    private final EventHandlerFactory handlerFactory = new EventHandlerClassFactory(getClass().getPackage().getName() + ".handler");
    private final Multimap<Class<?>, RegisteredHandler> handlersByEvent = HashMultimap.create();

    private final LoadingCache<Class<? extends Event>, RegisteredHandler[]> handlersCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Event>, RegisteredHandler[]>() {
                @Override
                public RegisteredHandler[] load(Class<? extends Event> key) throws Exception {
                    return bakeHandlers(key);
                }
            });

    @Inject
    public GraniteEventManager(Granite granite, PluginManager pluginManager) {
        this.granite = checkNotNull(granite, "granite");
        this.pluginManager = checkNotNull(pluginManager, "pluginManager");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RegisteredHandler[] bakeHandlers(Class<? extends Event> rootEvent) {
        List<RegisteredHandler> registrations = Lists.newArrayList();
        Set<Class<?>> types = (Set) TypeToken.of(rootEvent).getTypes().rawTypes();

        synchronized (this.lock) {
            for (Class<?> type : types) {
                if (Event.class.isAssignableFrom(type)) {
                    registrations.addAll(this.handlersByEvent.get(type));
                }
            }
        }

        RegisteredHandler[] handlers = registrations.toArray(new RegisteredHandler[registrations.size()]);
        Arrays.sort(handlers);
        return handlers;
    }


    private static boolean isValidHandler(Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isInterface(method.getDeclaringClass().getModifiers())
                || method.getReturnType() != void.class) {
            return false;
        }

        Class<?>[] parameters = method.getParameterTypes();
        return parameters.length == 1 && Event.class.isAssignableFrom(parameters[0]);
    }

    @SuppressWarnings("unchecked")
    public void register(PluginContainer container, Object listener) {
        checkNotNull(container, "container");
        checkNotNull(listener, "listener");

        List<RegisteredHandler> handlers = Lists.newArrayList();

        Class<?> handle = listener.getClass();
        for (Method method : handle.getMethods()) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                if (isValidHandler(method)) {
                    try {
                        Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                        EventHandler handler = this.handlerFactory.get(listener, method);
                        handlers.add(new RegisteredHandler(container, eventClass, subscribe.order(), handler, method, subscribe.ignoreCancelled()));
                    } catch (Exception e) {
                        this.granite.getLogger().error("Failed to create handler for " + method + " on " + method.getDeclaringClass().getName(), e);
                    }

                } else {
                    this.granite.getLogger().warn("The method {} on {} has @{} but has the wrong signature", method,
                            method.getDeclaringClass().getName(), Subscribe.class.getName());
                }
            }
        }

        synchronized (this.lock) {
            boolean changed = false;

            for (RegisteredHandler handler : handlers) {
                if (this.handlersByEvent.put(handler.getEventClass(), handler)) {
                    changed = true;
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }
        }
    }

    @Override
    public void register(Object plugin, Object listener) {
        Optional<PluginContainer> container = this.pluginManager.fromInstance(plugin);
        checkArgument(container.isPresent(), "Unknown plugin: %s", plugin);
        register(container.get(), listener);
    }

    @Override
    public void unregister(Object listener) {
        checkNotNull(listener, "listener");
        synchronized (this.lock) {
            boolean changed = false;

            Iterator<RegisteredHandler> itr = this.handlersByEvent.values().iterator();
            while (itr.hasNext()) {
                RegisteredHandler handler = itr.next();
                if (listener.equals(handler.getHandle())) {
                    itr.remove();
                    changed = true;
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }
        }
    }

    @Override
    public boolean post(Event event) {
        checkNotNull(event, "event");
        for (RegisteredHandler handler : this.handlersCache.getUnchecked(event.getClass())) {
            try {
                handler.handle(event);
            } catch (Throwable e) {
                this.granite.getLogger().error("Could not pass " + event.getClass().getSimpleName() + " to " + handler.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

}
