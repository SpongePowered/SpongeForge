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

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.mod.asm.util.ASMEventListenerHolderFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SpongeEventBus {

    private final Map<Class<?>, Map<EventPriority, EventListenerHolder<Event>>> eventAndPriorityToEventListenerHolderMap;

    public SpongeEventBus() {
        this.eventAndPriorityToEventListenerHolderMap = Maps.newHashMap();
    }

    public void add(Class<?> implementingEvent, Subscribe annotation, PriorityEventListener<Event> listener) {
        EventListenerHolder<Event> holder = getEventHolder(implementingEvent, annotation);
        holder.add(listener);
    }

    public void remove(PriorityEventListener<Event> listener) {
        Iterator<Map<EventPriority, EventListenerHolder<Event>>> mapIterator = eventAndPriorityToEventListenerHolderMap.values().iterator();
        while (mapIterator.hasNext()) {
            Map<EventPriority, EventListenerHolder<Event>> map = mapIterator.next();
            Iterator<EventListenerHolder<Event>> holderIterator = map.values().iterator();
            while (holderIterator.hasNext()) {
                EventListenerHolder<Event> holder = holderIterator.next();
                if (holder != listener.getHolder()) {
                    continue;
                }
                holder.remove(listener);
                if (holder.isEmpty()) {
                    FMLCommonHandler.instance().bus().unregister(holder);
                    holderIterator.remove();
                }
            }
            if (map.isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    private EventListenerHolder<Event> getEventHolder(Class<?> implementingEvent, Subscribe annotation) {

        Map<EventPriority, EventListenerHolder<Event>> priorityHolderMap = eventAndPriorityToEventListenerHolderMap.get(implementingEvent);

        if (priorityHolderMap == null) {
            priorityHolderMap = new HashMap<EventPriority, EventListenerHolder<Event>>();
            eventAndPriorityToEventListenerHolderMap.put(implementingEvent, priorityHolderMap);
        }

        EventPriority priority = PriorityMap.getEventPriority(annotation.order());
        EventListenerHolder<Event> eventListenerHolder = priorityHolderMap.get(priority);

        if (eventListenerHolder == null) {
            eventListenerHolder = ASMEventListenerHolderFactory.getNewEventListenerHolder(implementingEvent, priority, !annotation.ignoreCancelled());
            priorityHolderMap.put(priority, eventListenerHolder);
            FMLCommonHandler.instance().bus().register(eventListenerHolder);
        }

        return eventListenerHolder;
    }

}
