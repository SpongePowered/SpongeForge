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

import com.google.common.base.Throwables;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
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
import org.spongepowered.mod.interfaces.IMixinEventBus;

@NonnullByDefault
@Mixin(value = EventBus.class, remap = false)
public abstract class MixinEventBus implements IMixinEventBus {

    private EventBus eventBus = (EventBus) (Object) this;

    @Shadow @Final private int busID;
    @Shadow private IEventExceptionHandler exceptionHandler;

    @Overwrite
    public boolean post(Event event) {
        return post(event, false);
    }

    @Override
    public boolean post(Event event, boolean forgeOnly) {
        IEventListener[] listeners = event.getListenerList().getListeners(this.busID);

        if (!forgeOnly && event instanceof org.spongepowered.api.event.Event && !Sponge.getGame().getPlatform().getExecutionType().isClient()) {
            if (event instanceof BlockEvent.PlaceEvent
                || event instanceof BlockEvent.BreakEvent
                || event instanceof ItemTossEvent
                || (StaticMixinHelper.packetPlayer != null && event instanceof AttackEntityEvent)) {
                return false; // let the event happen, we will just capture it
            }
            boolean cancelled = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post(null, event, listeners);
            if (!cancelled) {
                SpongeForgeEventFactory.onForgePost(event);
            }

            return cancelled;
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

    @Override
    public int getBusID() {
        return this.busID;
    }
}
