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

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.TimingsManager;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.event.SpongeForgeEventFactory;
import org.spongepowered.mod.event.SpongeForgeEventHooks;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.interfaces.IMixinASMEventHandler;
import org.spongepowered.mod.interfaces.IMixinEventBus;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NonnullByDefault
@Mixin(value = EventBus.class, remap = false)
public abstract class MixinEventBus implements IMixinEventBus {

    private EventBus eventBus = (EventBus) (Object) this;

    // Because Forge can't be bothered to keep track of this information itself
    private Map<IEventListener, Class<? extends Event>> forgeListenerRegistry = new HashMap<>();

    @Shadow @Final private int busID;
    @Shadow private IEventExceptionHandler exceptionHandler;

    // Events that should not be posted on the event bus
    private boolean isEventAllowed(Event event) {
        if (event instanceof BlockEvent.PlaceEvent) {
            return false;
        } else if (event instanceof BlockEvent.BreakEvent) {
            return false;
        } else if (event instanceof PlayerInteractEvent.EntityInteract) {
            return false;
        } else if (event instanceof LivingDropsEvent) {
            return false;
        } else if (event instanceof WorldEvent.Save) {
            return false;
        } else if (event instanceof WorldEvent.Unload) {
            return false;
        } else if (event instanceof AttackEntityEvent) { // TODO - gabizou - figure this one out
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
                    final IEventListener listener = listeners[index];
                    if (listener instanceof IMixinASMEventHandler ) {
                        modListener = (IMixinASMEventHandler) listener;
                        modListener.getTimingsHandler().startTimingIfSync();
                        SpongeForgeEventHooks.preEventPhaseCheck(listener, event);
                        listener.invoke(event);
                        SpongeForgeEventHooks.postEventPhaseCheck(listener, event);
                        modListener.getTimingsHandler().stopTimingIfSync();
                    } else {
                        listener.invoke(event);
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

    @SuppressWarnings("unchecked")
    @Redirect(method = "register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/ListenerList;register(ILnet/minecraftforge/fml/common/eventhandler/EventPriority;Lnet/minecraftforge/fml/common/eventhandler/IEventListener;)V"))
    public void onRegister(ListenerList list, int id, EventPriority priority, IEventListener listener, Class<? extends Event> eventType, Object target, Method method, ModContainer owner) {
        list.register(id, priority, listener);

        SpongeModEventManager manager = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager());

        for (Class clazz: TypeToken.of(eventType).getTypes().rawTypes()) {
            Collection<Class<? extends org.spongepowered.api.event.Event>> spongeEvents = manager.forgeToSpongeEventMapping.get(clazz);
            if (spongeEvents != null) {
                for (Class<? extends org.spongepowered.api.event.Event> event : spongeEvents) {
                    manager.checker.registerListenerFor(event);
                }
            }
        }

        this.forgeListenerRegistry.put(listener, eventType);
    }

    @Redirect(method = "unregister", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/ListenerList;unregisterAll(ILnet/minecraftforge/fml/common/eventhandler/IEventListener;)V"))
    public void onUnregisterListener(int id, IEventListener listener) {
        ListenerList.unregisterAll(id, listener);

        SpongeModEventManager manager = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager());

        for (Class clazz: TypeToken.of(checkNotNull(this.forgeListenerRegistry.remove(listener))).getTypes().rawTypes()) {
            Collection<Class<? extends org.spongepowered.api.event.Event>> spongeEvents = manager.forgeToSpongeEventMapping.get(clazz);
            if (spongeEvents != null) {
                for (Class<? extends org.spongepowered.api.event.Event> event : spongeEvents) {
                    manager.checker.unregisterListenerFor(event);
                }
            }
        }
    }

    @Override
    public int getBusID() {
        return this.busID;
    }
}
