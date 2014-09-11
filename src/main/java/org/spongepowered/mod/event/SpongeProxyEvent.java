/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 SpongePowered <http://spongepowered.org/>
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

import org.spongepowered.api.event.Result;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Represents an cancalable {@link SpongeProxyEvent}
 *
 */
public class SpongeProxyEvent extends Event {
    private final org.spongepowered.api.event.Event event;

    public SpongeProxyEvent(org.spongepowered.api.event.Event event) {
        this.event = event;
    }

    /**
     * Checks if this {@link SpongeProxyEvent} is cancalable
     * @return <code>true</code> if cancalable, <code>false</code> if not
     */
    @Override
    public boolean isCancelable() {
        return event.isCancellable();
    }

    /**
     * Checks if this {@link SpongeProxyEvent} is already canceled
     * @return <code>true</code> if canceled, <code>false</code> if not
     */
    @Override
    public boolean isCanceled() {
        return event.isCancelled();
    }

    /**
     * Sets this event canceled
     * @param cancel <code>true</code> to cancel
     */
    @Override
    public void setCanceled(boolean cancel) {
        event.setCancelled(cancel);
    }

    /**
     * Checks, if this event has a result
     * @return <code>true</code> if there is a result to get, <code>false</code> if not
     */
    @Override
    public boolean hasResult() {
        return event.getResult() != org.spongepowered.api.event.Result.NO_RESULT;
    }

    /**
     * Returns the event {@link Result}
     * @return Event {@link Result}
     */
    @Override
    public Result getResult() {
        final org.spongepowered.api.event.Result result = event.getResult();

        switch (result) {
            case ALLOW:
                return Result.ALLOW;
            case DENY:
                return Result.DENY;
            default:
                return Result.DEFAULT;
        }
    }

    /**
     * Sets the event {@link Result}
     * @param value New result value as {@link Result}
     */
    @Override
    public void setResult(Result value) {
        switch (value) {
            case ALLOW:
                event.setResult(org.spongepowered.api.event.Result.ALLOW);
                break;
            case DENY:
                event.setResult(org.spongepowered.api.event.Result.DENY);
                break;
            default:
                event.setResult(org.spongepowered.api.event.Result.DEFAULT);
        }
    }
}
