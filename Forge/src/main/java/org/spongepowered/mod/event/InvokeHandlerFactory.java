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

import org.spongepowered.api.util.event.Cancellable;
import org.spongepowered.api.util.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class InvokeHandlerFactory implements HandlerFactory {

    @Override
    public Handler createHandler(Object object, Method method, boolean ignoreCancelled) {
        return new InvokeHandler(object, method, ignoreCancelled);
    }

    private static class InvokeHandler implements Handler {

        private final Object object;
        private final Method method;
        private final boolean ignoreCancelled;

        public InvokeHandler(Object object, Method method, boolean ignoreCancelled) {
            this.object = object;
            this.method = method;
            this.ignoreCancelled = ignoreCancelled;
        }

        @Override
        public void handle(Event event) throws InvocationTargetException {
            try {
                if (this.ignoreCancelled && (event instanceof Cancellable) && ((Cancellable) event).isCancelled()) {
                    return;
                }
                this.method.invoke(this.object, event);
            } catch (IllegalAccessException e) {
                throw new InvocationTargetException(e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            InvokeHandler that = (InvokeHandler) o;

            if (!this.method.equals(that.method)) {
                return false;
            }
            if (!this.object.equals(that.object)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.object.hashCode();
            result = 31 * result + this.method.hashCode();
            return result;
        }
    }

}
