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

import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.event.RegisteredListener;

public class EventData {

    private final boolean useCauseStackManager;
    private org.spongepowered.api.event.Event spongeEvent;
    private net.minecraftforge.fml.common.eventhandler.Event forgeEvent;
    private Class<? extends net.minecraftforge.fml.common.eventhandler.Event> forgeClass;
    private IEventListener[] forgeListeners;
    private RegisteredListener.Cache spongeListenerCache;
    private boolean beforeModifications = false;
    private boolean forced = false;

    // Used when mods initiate an event (Forge -> Sponge)
    public EventData(net.minecraftforge.fml.common.eventhandler.Event forgeEvent, final IEventListener[] forgeListeners) {
        this.forgeEvent = forgeEvent;
        this.forgeListeners = forgeListeners;
        this.useCauseStackManager = SpongeModEventManager.shouldUseCauseStackManager(false);
    }

    // Used when plugins initiate an event  (Sponge -> Forge)
    public EventData(org.spongepowered.api.event.Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> forgeClass, RegisteredListener.Cache cache, boolean useCauseStackManager) {
        this.spongeEvent = spongeEvent;
        this.forgeClass = forgeClass;
        this.spongeListenerCache = cache;
        this.useCauseStackManager = useCauseStackManager;
    }

    public net.minecraftforge.fml.common.eventhandler.Event getForgeEvent() {
        return this.forgeEvent;
    }

    public org.spongepowered.api.event.Event getSpongeEvent() {
        return this.spongeEvent;
    }

    public IEventListener[] getForgeListeners() {
        return this.forgeListeners;
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

    public Class<? extends net.minecraftforge.fml.common.eventhandler.Event> getForgeClass() {
        return this.forgeClass;
    }

    public void setForgeEvent(net.minecraftforge.fml.common.eventhandler.Event event) {
        this.forgeEvent = event;
    }
}