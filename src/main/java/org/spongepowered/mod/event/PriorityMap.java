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

import static cpw.mods.fml.common.eventhandler.EventPriority.HIGH;
import static cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST;
import static cpw.mods.fml.common.eventhandler.EventPriority.LOW;
import static cpw.mods.fml.common.eventhandler.EventPriority.LOWEST;
import static cpw.mods.fml.common.eventhandler.EventPriority.NORMAL;

import org.spongepowered.api.event.Order;

import cpw.mods.fml.common.eventhandler.EventPriority;

public class PriorityMap {

    private static final EventPriority[] eventPriorities;
    private static final Order[] orders;

    static {
        eventPriorities = new EventPriority[] {HIGHEST, HIGHEST, HIGH, HIGH, NORMAL, LOW, LOW, LOWEST, LOWEST};
        orders = Order.values();
    }

    private PriorityMap() {
    }
    
    public static EventPriority getEventPriority(Order order) {
        return eventPriorities[order.ordinal()];
    }

    public static Order getOrder(EventPriority priority) {
        return orders[priority.ordinal()];
    }
}
