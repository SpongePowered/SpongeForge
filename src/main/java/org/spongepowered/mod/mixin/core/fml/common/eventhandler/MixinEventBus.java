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
package org.spongepowered.mod.mixin.core.fml.common.eventhandler;

import co.aikar.timings.TimingsManager;
import com.google.common.base.Throwables;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.mod.event.SpongeForgeEventFactory;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.interfaces.IMixinASMEventHandler;
import org.spongepowered.mod.interfaces.IMixinEventBus;

@NonnullByDefault
@Mixin(value = EventBus.class, remap = false)
public abstract class MixinEventBus implements IMixinEventBus {

    private EventBus eventBus = (EventBus) (Object) this;

    @Shadow @Final private int busID;
    @Shadow private IEventExceptionHandler exceptionHandler;

    // Events that should not be posted on the event bus
    private boolean isEventAllowed(Event event) {
        if (event instanceof BlockEvent.PlaceEvent) {
            return false;
        } else if (event instanceof BlockEvent.BreakEvent) {
            return false;
        } else if (event instanceof EntityInteractEvent) {
            return false;
        } else if (event instanceof LivingDropsEvent) {
            return false;
        } else if (event instanceof WorldEvent.Save) {
            return false;
        } else if (StaticMixinHelper.packetPlayer != null && event instanceof AttackEntityEvent) {
            return false;
        }

        return true;
    }

    @Overwrite
    public boolean post(Event event) {
        return post(event, false);
    }

    @Override
    public boolean post(Event event, boolean forced) {
        if (!forced && !isEventAllowed(event)) {
            return false;
        }

        IEventListener[] listeners = event.getListenerList().getListeners(this.busID);
        if (!forced && event instanceof org.spongepowered.api.event.Event && !Sponge.getGame().getPlatform().getExecutionType().isClient()) {
            boolean cancelled = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post(null, event, listeners);
            if (!cancelled) {
                SpongeForgeEventFactory.onForgePost(event);
            }

            return cancelled;
        } else {
            listeners = event.getListenerList().getListeners(this.busID);
            int index = 0;
            IMixinASMEventHandler modListener = null;
            try {
                if (SpongeImpl.isInitialized()) {
                    TimingsManager.MOD_EVENT_HANDLER.startTimingIfSync();
                }
                for (; index < listeners.length; index++) {
                    if (listeners[index] instanceof IMixinASMEventHandler ) {
                        modListener = (IMixinASMEventHandler) listeners[index];
                        modListener.getTimingsHandler().startTimingIfSync();
                        listeners[index].invoke(event);
                        modListener.getTimingsHandler().stopTimingIfSync();
                    } else {
                        listeners[index].invoke(event);
                    }
                }
            } catch (Throwable throwable) {
                if (modListener != null) {
                    modListener.getTimingsHandler().stopTimingIfSync();
                }
                this.exceptionHandler.handleException(this.eventBus, event, listeners, index, throwable);
                Throwables.propagate(throwable);
            }
            if (SpongeImpl.isInitialized()) {
                TimingsManager.MOD_EVENT_HANDLER.stopTimingIfSync();
            }
            return (event.isCancelable() ? event.isCanceled() : false);
        }
    }

    @Override
    public int getBusID() {
        return this.busID;
    }
}
