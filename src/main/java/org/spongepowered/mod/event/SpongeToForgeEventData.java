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

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.RegisteredListener;

public class SpongeToForgeEventData {

    private final Event spongeEvent;
    private final Class<? extends net.minecraftforge.fml.common.eventhandler.Event> forgeClass;
    private final RegisteredListener.Cache listenerCache;
    private final boolean useCauseStackManager;
    private net.minecraftforge.fml.common.eventhandler.Event forgeEvent;

    public SpongeToForgeEventData(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> forgeClass, RegisteredListener.Cache cache, boolean useCauseStackManager) {
        this.spongeEvent = spongeEvent;
        this.forgeClass = forgeClass;
        this.listenerCache = cache;
        this.useCauseStackManager = useCauseStackManager;
    }

    public SpongeToForgeEventData(ForgeToSpongeEventData eventData) {
        this.spongeEvent = eventData.getSpongeEvent();
        this.forgeClass = eventData.getForgeEvent().getClass();
        this.listenerCache = eventData.getSpongeListenerCache();
        this.useCauseStackManager = eventData.useCauseStackManager();
        this.forgeEvent = eventData.getForgeEvent();
    }

    public boolean useCauseStackManager() {
        return this.useCauseStackManager;
    }

    public Event getSpongeEvent() {
        return this.spongeEvent;
    }

    public net.minecraftforge.fml.common.eventhandler.Event getForgeEvent() {
        return this.forgeEvent;
    }

    public RegisteredListener.Cache getSpongeListenerCache() {
        return this.listenerCache;
    }

    public Class<? extends net.minecraftforge.fml.common.eventhandler.Event> getForgeClass() {
        return this.forgeClass;
    }

    public void setForgeEvent(net.minecraftforge.fml.common.eventhandler.Event event) {
        this.forgeEvent = event;
    }

    public void propagateCancelled() {
        // Only propagate if Sponge Event wasn't cancelled already
        if (this.spongeEvent instanceof Cancellable && this.forgeEvent.isCancelable() && !((Cancellable) this.spongeEvent).isCancelled()) {
            ((Cancellable) this.spongeEvent).setCancelled(this.forgeEvent.isCanceled());
        }
    }
}
