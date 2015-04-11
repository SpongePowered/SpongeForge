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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.event.Order;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

class HandlerCache {

    private final List<Handler> handlers;
    private final EnumMap<Order, List<Handler>> orderGrouped;

    @SuppressWarnings("unchecked")
    HandlerCache(List<RegisteredHandler> registrations) {
        this.handlers = Lists.newArrayList();
        for (RegisteredHandler reg : registrations) {
            this.handlers.add(reg.getHandler());
        }

        this.orderGrouped = Maps.newEnumMap(Order.class);
        for (Order order : Order.values()) {
            this.orderGrouped.put(order, new ArrayList<Handler>());
        }
        for (RegisteredHandler reg : registrations) {
            this.orderGrouped.get(reg.getOrder()).add(reg.getHandler());
        }
    }

    public List<Handler> getHandlers() {
        return this.handlers;
    }

    public List<Handler> getHandlersByOrder(Order order) {
        return this.orderGrouped.get(order);
    }

}
