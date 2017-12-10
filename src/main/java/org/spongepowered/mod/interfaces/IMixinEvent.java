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
package org.spongepowered.mod.interfaces;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public interface IMixinEvent {

    /**
     * Syncs event data from the Sponge event to the Forge equivalent
     *
     * <p>The event argument should be the Forge event if this object is a
     * Sponge event and vice versa</p>
     *
     * @param event The event to sync with
     */
    void syncDataToForge(Event event);

    /**
     * Syncs event data from the Forge event to the Sponge equivalent
     *
     * <p>The event argument should be the Forge event if this object is a
     * Sponge event and vice versa</p>
     *
     * @param event The event to sync with
     */
    void syncDataToSponge(Event event);

    Event createSpongeEvent();

    Cause getCause();

    default void postProcess() {
    }

}
