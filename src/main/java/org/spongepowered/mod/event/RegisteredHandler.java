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

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.event.Order;

class RegisteredHandler implements Comparable<RegisteredHandler> {

    private final Handler handler;
    private final Order order;
    private final PluginContainer container;

    RegisteredHandler(Handler handler, Order order, PluginContainer container) {
        this.handler = handler;
        this.order = order;
        this.container = container;
    }

    static RegisteredHandler createForComparison(Handler handler) {
        return new RegisteredHandler(handler, null, null);
    }

    public Handler getHandler() {
        return this.handler;
    }

    public Order getOrder() {
        return this.order;
    }

    public PluginContainer getContainer() {
        return this.container;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisteredHandler that = (RegisteredHandler) o;
        return this.handler.equals(that.handler);
    }

    @Override
    public int hashCode() {
        return this.handler.hashCode();
    }

    @Override
    public int compareTo(RegisteredHandler o) {
        return getOrder().ordinal() - o.getOrder().ordinal();
    }

}
