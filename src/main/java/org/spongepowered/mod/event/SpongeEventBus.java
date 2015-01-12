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

package org.spongepowered.mod.event;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.util.event.Cancellable;
import org.spongepowered.api.util.event.Event;
import org.spongepowered.api.util.event.Order;
import org.spongepowered.api.util.event.Subscribe;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeEventBus implements EventManager {

    private static final Logger log = LoggerFactory.getLogger(SpongeEventBus.class);

    private final Object lock = new Object();
    private final PluginManager pluginManager;
    private final HandlerFactory handlerFactory = new HandlerClassFactory("org.spongepowered.mod.event.handler");
    private final Multimap<Class<?>, RegisteredHandler> handlersByEvent = HashMultimap.create();

    /**
     * A cache of all the handlers for an event type for quick event posting.
     *
     * <p>The cache is currently entirely invalidated if handlers are added
     * or removed.</p>
     */
    private final LoadingCache<Class<?>, HandlerCache> handlersCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, HandlerCache>() {
                @Override
                public HandlerCache load(Class<?> type) throws Exception {
                    return bakeHandlers(type);
                }
            });

    @Inject
    public SpongeEventBus(PluginManager pluginManager) {
        checkNotNull(pluginManager, "pluginManager");
        this.pluginManager = pluginManager;
    }

    @SuppressWarnings("unchecked")
    private HandlerCache bakeHandlers(Class<?> rootType) {
        List<RegisteredHandler> registrations = Lists.newArrayList();
        Set<Class<?>> types = (Set) TypeToken.of(rootType).getTypes().rawTypes();

        synchronized (this.lock) {
            for (Class<?> type : types) {
                if (Event.class.isAssignableFrom(type)) {
                    registrations.addAll(this.handlersByEvent.get(type));
                }
            }
        }

        Collections.sort(registrations);

        return new HandlerCache(registrations);
    }

    private HandlerCache getHandlerCache(Class<?> type) {
        return this.handlersCache.getUnchecked(type);
    }

    @SuppressWarnings("unchecked")
    private List<Subscriber> findAllSubscribers(Object object) {
        List<Subscriber> subscribers = Lists.newArrayList();
        Class<?> type = object.getClass();

        for (Method method : type.getMethods()) {
            @Nullable Subscribe subscribe = method.getAnnotation(Subscribe.class);

            if (subscribe != null) {
                Class<?>[] paramTypes = method.getParameterTypes();

                if (isValidHandler(method)) {
                    Class<Event> eventClass = (Class<Event>) paramTypes[0];
                    Handler handler = this.handlerFactory.createHandler(object, method, subscribe.ignoreCancelled());
                    subscribers.add(new Subscriber(eventClass, handler, subscribe.order()));
                } else {
                    log.warn("The method {} on {} has @{} but has the wrong signature",
                            method, method.getDeclaringClass().getName(), Subscribe.class.getName());
                }
            }
        }

        return subscribers;
    }

    public boolean register(Class<?> type, Handler handler, Order order, PluginContainer container) {
        return register(new Subscriber(type, handler, order), container);
    }

    public boolean register(Subscriber subscriber, PluginContainer container) {
        return registerAll(Lists.newArrayList(subscriber), container);
    }

    private boolean registerAll(List<Subscriber> subscribers, PluginContainer container) {
        synchronized (this.lock) {
            boolean changed = false;

            for (Subscriber sub : subscribers) {
                if (this.handlersByEvent.put(sub.getEventClass(), new RegisteredHandler(sub.getHandler(), sub.getOrder(), container))) {
                    changed = true;
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }

            return changed;
        }
    }

    public boolean unregister(Class<?> type, Handler handler) {
        return unregister(new Subscriber(type, handler));
    }

    public boolean unregister(Subscriber subscriber) {
        return unregisterAll(Lists.newArrayList(subscriber));
    }

    public boolean unregisterAll(List<Subscriber> subscribers) {
        synchronized (this.lock) {
            boolean changed = false;

            for (Subscriber sub : subscribers) {
                if (this.handlersByEvent.remove(sub.getEventClass(), RegisteredHandler.createForComparison(sub.getHandler()))) {
                    changed = true;
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }

            return changed;
        }
    }

    private void callListener(Handler handler, Event event) {
        try {
            handler.handle(event);
        } catch (Throwable t) {
            log.warn("A handler raised an error when handling an event", t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(Object plugin, Object object) {
        checkNotNull(object, "plugin");
        checkNotNull(object, "object");

        Optional<PluginContainer> container = this.pluginManager.fromInstance(object);
        if (!container.isPresent()) {
            throw new IllegalArgumentException("The specified object is not a plugin object");
        }

        registerAll(findAllSubscribers(object), container.get());
    }

    @Override
    public void unregister(Object object) {
        checkNotNull(object, "object");
        unregisterAll(findAllSubscribers(object));
    }

    @Override
    public boolean post(Event event) {
        checkNotNull(event, "event");

        for (Handler handler : getHandlerCache(event.getClass()).getHandlers()) {
            callListener(handler, event);
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    public boolean post(Event event, Order order) {
        checkNotNull(event, "event");
        checkNotNull(event, "order");

        for (Handler handler : getHandlerCache(event.getClass()).getHandlersByOrder(order)) {
            callListener(handler, event);
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    private static boolean isValidHandler(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return !Modifier.isStatic(method.getModifiers())
                && !Modifier.isAbstract(method.getModifiers())
                && !Modifier.isInterface(method.getDeclaringClass().getModifiers())
                && method.getReturnType() == void.class
                && paramTypes.length == 1
                && Event.class.isAssignableFrom(paramTypes[0]);
    }

}
