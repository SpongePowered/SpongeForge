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
package org.spongepowered.mod.mixin.event;

import com.google.common.base.Throwables;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.event.SpongeEventBus;

@NonnullByDefault
@Mixin(value = net.minecraftforge.fml.common.eventhandler.EventBus.class, remap = false)
public abstract class MixinEventBus {

    private EventBus eventBus = (EventBus) (Object) this;

    @Shadow private int busID;
    @Shadow private IEventExceptionHandler exceptionHandler;

    @Overwrite
    public boolean post(Event event) {
        IEventListener[] listeners = event.getListenerList().getListeners(this.busID);

        if (event instanceof org.spongepowered.api.util.event.Event) {
            return ((SpongeEventBus) SpongeMod.instance.getGame().getEventManager()).post(event, listeners);
        } else {
            listeners = event.getListenerList().getListeners(this.busID);
            int index = 0;
            try {
                for (; index < listeners.length; index++) {
                    listeners[index].invoke(event);
                }
            } catch (Throwable throwable) {
                this.exceptionHandler.handleException(this.eventBus, event, listeners, index, throwable);
                Throwables.propagate(throwable);
            }
            return (event.isCancelable() ? event.isCanceled() : false);
        }
    }

}
