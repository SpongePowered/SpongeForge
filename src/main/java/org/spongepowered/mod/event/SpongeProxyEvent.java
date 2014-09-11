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

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Represents an cancelable {@link SpongeProxyEvent}
 *
 */
public class SpongeProxyEvent extends Event {
    private final org.spongepowered.api.event.Event event;

    /**
     * Initializes the {@link SpongeProxyEvent} with parent {@link org.spongepowered.api.event.Event}
     * @param event Parent {@link org.spongepowered.api.event.Event}
     */
    public SpongeProxyEvent(org.spongepowered.api.event.Event event) {
        this.event = event;
    }

    /**
     * Checks if the parent {@link org.spongepowered.api.event.Event} is cancalable
     * @return <code>true</code> if cancalable, <code>false</code> if not
     */
    @Override
    public boolean isCancelable() {
        return event.isCancellable();
    }

    /**
     * Checks if parent {@link org.spongepowered.api.event.Event} is already canceled
     * @return {@code true} if canceled, {@code false} if not
     */
    @Override
    public boolean isCanceled() {
        return event.isCancelled();
    }

    /**
     * Sets the parent {@link org.spongepowered.api.event.Event} canceled
     * @param cancel {@code true} to cancel
     */
    @Override
    public void setCanceled(boolean cancel) {
        event.setCancelled(cancel);
    }

    /**
     * Checks, if parent {@link org.spongepowered.api.event.Event} has a result
     * @return {@code true} if there is a result to get, {@code false} if not
     */
    @Override
    public boolean hasResult() {
        return event.getResult() != org.spongepowered.api.event.Result.NO_RESULT;
    }

    /**
     * Returns the {@link cpw.mods.fml.common.eventhandler.Event.Result} of parent {@link org.spongepowered.api.event.Event}
     * @return The parents {@link org.spongepowered.api.event.Event} {@link cpw.mods.fml.common.eventhandler.Event.Result}
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
     * Sets the parent event {@link cpw.mods.fml.common.eventhandler.Event.Result}
     * @param value New result value as {@link cpw.mods.fml.common.eventhandler.Event.Result}
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
