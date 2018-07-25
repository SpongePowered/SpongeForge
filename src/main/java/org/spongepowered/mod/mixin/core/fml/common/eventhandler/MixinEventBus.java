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

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.mod.SpongeModPlatform;
import org.spongepowered.mod.event.ForgeToSpongeEventData;
import org.spongepowered.mod.event.ForgeToSpongeEventFactory;
import org.spongepowered.mod.event.SpongeToForgeEventData;
import org.spongepowered.mod.event.SpongeForgeEventHooks;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.interfaces.IMixinASMEventHandler;
import org.spongepowered.mod.interfaces.IMixinEventBus;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = EventBus.class, remap = false)
public abstract class MixinEventBus implements IMixinEventBus {

    // Because Forge can't be bothered to keep track of this information itself
    private static Map<IEventListener, Class<? extends Event>> forgeListenerRegistry = new HashMap<>();
    private static Set<Class<? extends Event>> forgeListenerEventClasses = new HashSet<>();

    @Shadow @Final private int busID;
    @Shadow private IEventExceptionHandler exceptionHandler;

    // Events that should not be posted on the event bus
    private boolean isEventAllowed(Event event) {
        if (event instanceof LivingDropsEvent) {
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

    private boolean isClientPlatform() {
        // This can be called before Sponge is initialied, so use this hack
        return SpongeModPlatform.staticGetExecutionType().isClient();
    }

    private boolean isIgnoredEvent(Event event) {
        if (event instanceof TickEvent) {
            return true;
        }
        if (event instanceof EntityEvent.CanUpdate) {
            return true;
        }
        if (event instanceof GetCollisionBoxesEvent) {
            return true;
        }
        if (event instanceof AttachCapabilitiesEvent) {
            return true;
        }
        if (event instanceof OreDictionary.OreRegisterEvent) {
            return true;
        }
        if (event instanceof FluidRegistry.FluidRegisterEvent) {
            return true;
        }
        return false;
    }

    /**
     * @author unknown
     * @reason Use added boolean flag to direct whether the event is forced or not, since we sync sponge to forge events quite often.
     *
     * @param event The event to post
     * @return The boolean cancellable value
     */
    @Overwrite
    public boolean post(Event event) {
        return post(event, false);
    }

    @Override
    public boolean post(SpongeToForgeEventData eventData) {
        final boolean result = post(eventData.getForgeEvent(), true);
        eventData.propagateCancelled();
        return result;
    }

    @Override
    public boolean post(Event event, boolean forced) {
        Class<? extends org.spongepowered.api.event.Event> spongeEventClass = null;

        final IEventListener[] listeners = event.getListenerList().getListeners(this.busID);
        if (!forced && !this.isClientPlatform() && SpongeImpl.isInitialized() && !isIgnoredEvent(event)) {
            if (!isEventAllowed(event)) {
                return false;
            }

            spongeEventClass = ForgeToSpongeEventFactory.getSpongeClass(event);
            if (spongeEventClass != null) {
                final RegisteredListener.Cache listenerCache = ((SpongeModEventManager) Sponge.getEventManager()).getHandlerCache(spongeEventClass);
                if (!listenerCache.getListeners().isEmpty()) {
                    final ForgeToSpongeEventData forgeEventData = new ForgeToSpongeEventData(event, listeners, this.isClientPlatform());
                    forgeEventData.setSpongeListenerCache(listenerCache);
                    return ((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post(forgeEventData);
                }
            }
        }

        int index = 0;
        IMixinASMEventHandler modListener = null;
        try {
            for (; index < listeners.length; index++) {
                final IEventListener listener = listeners[index];
                if (listener instanceof IMixinASMEventHandler) {
                    modListener = (IMixinASMEventHandler) listener;
                    modListener.getTimingsHandler().startTimingIfSync();
                    try (PhaseContext<?> context = SpongeForgeEventHooks.preEventPhaseCheck(listener, event)) {
                        if (context != null) {
                            context.buildAndSwitch();
                        }
                        listener.invoke(event);
                    }
                    modListener.getTimingsHandler().stopTimingIfSync();
                } else {
                    listener.invoke(event);
                }
            }
        } catch (Throwable throwable) {
            if (modListener != null) {
                modListener.getTimingsHandler().stopTimingIfSync();
            }
            this.exceptionHandler.handleException((EventBus) (Object) this, event, listeners, index, throwable);
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }
        return (event.isCancelable() ? event.isCanceled() : false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Redirect(method = "register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/ListenerList;register(ILnet/minecraftforge/fml/common/eventhandler/EventPriority;Lnet/minecraftforge/fml/common/eventhandler/IEventListener;)V", remap = false))
    public void onRegister(ListenerList list, int id, EventPriority priority, IEventListener listener, Class<? extends Event> eventType, Object target, Method method, ModContainer owner) {
        list.register(id, priority, listener);

        SpongeModEventManager manager = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager());

        if (!forgeListenerEventClasses.contains(eventType)) {
            for (Class clazz : TypeToken.of(eventType).getTypes().rawTypes()) {
                Collection<Class<? extends org.spongepowered.api.event.Event>> spongeEvents = manager.forgeToSpongeEventMapping.get(clazz);
                if (spongeEvents != null) {
                    for (Class<? extends org.spongepowered.api.event.Event> event : spongeEvents) {
                        manager.checker.registerListenerFor(event);
                    }
                }
            }
        }

        forgeListenerRegistry.put(listener, eventType);
        forgeListenerEventClasses.add(eventType);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Redirect(method = "unregister", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/ListenerList;unregisterAll(ILnet/minecraftforge/fml/common/eventhandler/IEventListener;)V", remap = false))
    public void onUnregisterListener(int id, IEventListener listener) {
        ListenerList.unregisterAll(id, listener);

        SpongeModEventManager manager = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager());

        for (Class clazz: TypeToken.of(checkNotNull(forgeListenerRegistry.remove(listener))).getTypes().rawTypes()) {
            Collection<Class<? extends org.spongepowered.api.event.Event>> spongeEvents = manager.forgeToSpongeEventMapping.get(clazz);
            if (spongeEvents != null) {
                for (Class<? extends org.spongepowered.api.event.Event> event : spongeEvents) {
                    manager.checker.unregisterListenerFor(event);
                }
            }
        }

        // update event class cache
        Iterator<Class<? extends Event>> it = forgeListenerEventClasses.iterator();
        while (it.hasNext()) {
            Class clazz = it.next();

            boolean found = false;
            for (Map.Entry<IEventListener, Class<? extends Event>> mapEntry : forgeListenerRegistry.entrySet()) {
                if (clazz.equals(mapEntry.getValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                it.remove();
            }
        }
    }

    @Override
    public Set<Class<? extends Event>> getEventListenerClassList() {
        return forgeListenerEventClasses;
    }

    @Override
    public int getBusID() {
        return this.busID;
    }
}
