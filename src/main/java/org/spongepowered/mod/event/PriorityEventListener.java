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

import org.spongepowered.api.event.Order;

public class PriorityEventListener<T> implements EventListener<T>, Comparable<PriorityEventListener<T>> {

    private final EventListener<T> listener;
    private final Order order;
    private EventListenerHolder<T> holder;
    
    public PriorityEventListener(Order order, EventListener<T> listener) {
        this.listener = listener;
        this.order = order;
    }

    public EventListenerHolder<T> getHolder() {
        return holder;
    }

    public void setHolder(EventListenerHolder<T> holder) {
        this.holder = holder;
    }

    @Override
    public void invoke(T event) {
        listener.invoke(event);
    }

    @Override
    public int compareTo(PriorityEventListener<T> o) {
        return order.compareTo(o.order);
    }

}
