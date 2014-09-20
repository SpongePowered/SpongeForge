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

public abstract class EventListenerHolder<T> {

    protected PriorityEventListener<T>[] listeners;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected EventListenerHolder() {
        this.listeners = new PriorityEventListener[0];
    }

    public boolean isEmpty() {
        return listeners.length == 0;
    }

    public void add(PriorityEventListener<T> listener) {
        if (listener.getHolder() != null) {
            throw new IllegalArgumentException("Listener already contained in a listener holder");
        }
        listener.setHolder(this);

        @SuppressWarnings({"unchecked", "rawtypes" })
        PriorityEventListener<T>[] newListeners = new PriorityEventListener[listeners.length + 1];
        int i;
        for (i = 0; i < listeners.length; i++) {
            if (listener.compareTo(listeners[i]) <= 0) {
                break;
            }
            newListeners[i] = listeners[i];
        }
        newListeners[i++] = listener;
        for (; i < newListeners.length; i++) {
            newListeners[i] = listeners[i - 1];
        }
        listeners = newListeners;
    }

    public void remove(PriorityEventListener<T> listener) {
        if (listener.getHolder() != this) {
            throw new IllegalArgumentException("EventListenerHolder does not contain event");
        }
        @SuppressWarnings({"unchecked", "rawtypes" })
        PriorityEventListener<T>[] newListeners = new PriorityEventListener[listeners.length - 1];
        int i = 0;
        int j = 0;
        while (i < listeners.length) {
            if (listeners[i] == listener) {
                i++;
                break;
            }
            newListeners[j++] = listeners[i++];
        }
        while (i < listeners.length) {
            newListeners[j++] = listeners[i++];
        }
        listener.setHolder(null);
        listeners = newListeners;
    }

    public void invoke(T event) {
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].invoke(event);
        }
    }

}
