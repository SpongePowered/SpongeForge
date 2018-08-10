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

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.common.event.RegisteredListener;

public class ForgeToSpongeEventData {

    private final Event forgeEvent;
    private final IEventListener[] forgeListeners;
    private final boolean isClient;
    private final boolean useCauseStackManager;
    private org.spongepowered.api.event.Event spongeEvent;
    private RegisteredListener.Cache spongeListenerCache;
    private boolean beforeModifications = false;
    private boolean forced = false;

    public ForgeToSpongeEventData(Event forgeEvent, final IEventListener[] forgeListeners, boolean isClient) {
        this.forgeEvent = forgeEvent;
        this.forgeListeners = forgeListeners;
        this.isClient = isClient;
        this.useCauseStackManager = SpongeModEventManager.shouldUseCauseStackManager(false);
    }

    public Event getForgeEvent() {
        return this.forgeEvent;
    }

    public org.spongepowered.api.event.Event getSpongeEvent() {
        return this.spongeEvent;
    }

    public IEventListener[] getForgeListeners() {
        return this.forgeListeners;
    }

    public boolean isClient() {
        return this.isClient;
    }

    public boolean useCauseStackManager() {
        return this.useCauseStackManager;
    }

    public boolean isBeforeModifications() {
        return this.beforeModifications;
    }

    public boolean isForced() {
        return this.forced;
    }

    public RegisteredListener.Cache getSpongeListenerCache() {
        return this.spongeListenerCache;
    }

    public void setBeforeModifications(boolean beforeModifications) {
        this.beforeModifications = beforeModifications;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public void setSpongeEvent(org.spongepowered.api.event.Event event) {
        this.spongeEvent = event;
    }

    public void setSpongeListenerCache(RegisteredListener.Cache cache) {
        this.spongeListenerCache = cache;
    }

    public void propagateCancelled() {
        // Only propagate if Forge Event wasn't cancelled already
        if (this.spongeEvent instanceof Cancellable && this.forgeEvent.isCancelable() && !this.forgeEvent.isCanceled()) {
            this.forgeEvent.setCanceled(((Cancellable) this.spongeEvent).isCancelled());
        }
    }
}
