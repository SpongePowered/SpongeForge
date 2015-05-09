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

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.event.RegisteredHandler;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.mod.SpongeMod;

import javax.inject.Inject;

public class SpongeModEventManager extends SpongeEventManager {

    private final ImmutableMap<EventPriority, Order> priorityMappings = new ImmutableMap.Builder<EventPriority, Order>()
            .put(EventPriority.HIGHEST, Order.FIRST)
            .put(EventPriority.HIGH, Order.EARLY)
            .put(EventPriority.NORMAL, Order.DEFAULT)
            .put(EventPriority.LOW, Order.LATE)
            .put(EventPriority.LOWEST, Order.LAST)
            .build();

    @Inject
    public SpongeModEventManager(PluginManager pluginManager) {
        super(pluginManager);
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

}
