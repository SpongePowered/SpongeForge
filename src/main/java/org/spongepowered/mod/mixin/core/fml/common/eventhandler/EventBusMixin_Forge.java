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

import co.aikar.timings.Timing;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
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
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.mod.SpongeModPlatform;
import org.spongepowered.mod.bridge.event.ASMEventHandlerBridge;
import org.spongepowered.mod.bridge.event.EventBusBridge_Forge;
import org.spongepowered.mod.event.ForgeToSpongeEventData;
import org.spongepowered.mod.event.ForgeToSpongeEventFactory;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.event.SpongeToForgeEventData;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
@NonnullByDefault
@Mixin(value = EventBus.class, remap = false)
public abstract class EventBusMixin_Forge implements EventBusBridge_Forge {

    // Because Forge can't be bothered to keep track of this information itself
    private static final Map<IEventListener, Class<? extends Event>> forgeImpl$forgeListenerRegistry = new Reference2ReferenceOpenHashMap<>();
    private static final Set<Class<? extends Event>> forgeImpl$forgeListenerEventClasses = new ReferenceOpenHashSet<>();

    @Shadow @Final private int busID;
    @Shadow private IEventExceptionHandler exceptionHandler;

    @Nullable
    private PhaseContext<?> forgeImpl$preEventPhaseCheck(final ASMEventHandlerBridge listener, final Event event) {
        if (forgeImpl$isIgnoredEvent(event)) {
            return null;
        }
        if (!SpongeImplHooks.isMainThread()) {
            // We don't want to throw phases async, nor on the client before sponge is initialized, since main thread is technically
            // allowed before the server has started.
            return null;
        }
        if (event instanceof TickEvent.WorldTickEvent) {
            final TickEvent.WorldTickEvent worldTickEvent = (TickEvent.WorldTickEvent) event;
            final World world = worldTickEvent.world;
            if (world == null || ((WorldBridge) world).bridge$isFake()) {
                return null;
            }
            if (worldTickEvent.phase == TickEvent.Phase.START) {
                return PluginPhase.Listener.PRE_WORLD_TICK_LISTENER
                    .createPhaseContext()
                    .source(listener.forgeBridge$getContainer())
                    .world(world)
                    .event(event);
            } else if (worldTickEvent.phase == TickEvent.Phase.END) {
                return PluginPhase.Listener.POST_WORLD_TICK_LISTENER
                    .createPhaseContext()
                    .source(listener.forgeBridge$getContainer())
                    .world(world)
                    .event(event);
            }
        }
        // Basically some forge mods also listen to the server tick event and perform world changes as well...........
        if (event instanceof TickEvent.ServerTickEvent) {
            final TickEvent.ServerTickEvent serverTickEvent = (TickEvent.ServerTickEvent) event;
            if (serverTickEvent.phase == TickEvent.Phase.START) {
                // Need to prepare all worlds many mods do this
                return PluginPhase.Listener.PRE_SERVER_TICK_LISTENER.createPhaseContext()
                        .source(listener.forgeBridge$getContainer())
                        .event(event);
            } else if (serverTickEvent.phase == TickEvent.Phase.END) {
                // Need to prepare all worlds many mods do this
                return PluginPhase.Listener.POST_SERVER_TICK_LISTENER.createPhaseContext()
                    .source(listener.forgeBridge$getContainer())
                    .event(event);

            }
        }
        if (listener.forgeBridge$getContainer() != null && PhaseTracker.getInstance().getCurrentState().allowsEventListener()) {
            return PluginPhase.Listener.GENERAL_LISTENER.createPhaseContext()
                .event(event)
                .source(listener.forgeBridge$getContainer());
        }
        return null;
    }

    // Events that should not be posted on the event bus
    private boolean forgeImpl$isEventAllowed(final Event event) {
        if (event instanceof LivingDropsEvent) {
            return false;
        } else if (event instanceof WorldEvent.Save) {
            return false;
        } else if (event instanceof WorldEvent.Unload) {
            return false;
        }

        return true;
    }

    private boolean forgeImpl$isClientPlatform() {
        // This can be called before Sponge is initialied, so use this hack
        return SpongeModPlatform.staticGetExecutionType().isClient();
    }

    private boolean forgeImpl$isIgnoredEvent(final Event event) {
        if (event instanceof TickEvent && ((TickEvent) event).side == Side.CLIENT) {
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

        return SpongeImplHooks.isEventClientEvent(event);
    }

    private boolean forgeImpl$isTimedEvent(final Event event) {
        return !(event instanceof AttachCapabilitiesEvent);
    }

    /**
     * @author unknown
     * @reason Use added boolean flag to direct whether the event is forced or not, since we sync sponge to forge events quite often.
     *
     * @param event The event to post
     * @return The boolean cancellable value
     */
    @Overwrite
    public boolean post(final Event event) {
        return forgeBridge$post(event, false);
    }

    @Override
    public boolean forgeBridge$post(final SpongeToForgeEventData eventData) {
        final boolean result = forgeBridge$post(eventData.getForgeEvent(), true);
        eventData.propagateCancelled();
        return result;
    }

    @Override
    public boolean forgeBridge$post(final Event event, final boolean forced) {
        Class<? extends org.spongepowered.api.event.Event> spongeEventClass = null;

        final IEventListener[] listeners = event.getListenerList().getListeners(this.busID);
        if (!forced && SpongeImpl.isInitialized() && SpongeImplHooks.isMainThread() && !forgeImpl$isIgnoredEvent(event)) {
            if (!forgeImpl$isEventAllowed(event)) {
                return false;
            }

            spongeEventClass = ForgeToSpongeEventFactory.getSpongeClass(event);
            if (spongeEventClass != null) {
                final RegisteredListener.Cache listenerCache = ((SpongeModEventManager) Sponge.getEventManager()).getHandlerCache(spongeEventClass);
                if (!listenerCache.getListeners().isEmpty()) {
                    final ForgeToSpongeEventData forgeEventData = new ForgeToSpongeEventData(event, listeners);
                    forgeEventData.setSpongeListenerCache(listenerCache);
                    return ((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post(forgeEventData);
                }
            }
        }

        int index = 0;
        try {
            for (; index < listeners.length; index++) {
                final IEventListener listener = listeners[index];
                if (SpongeImpl.isInitialized() && listener instanceof ASMEventHandlerBridge) {
                    // Set up the timing object, since it's a try with resources, it'll always close
                    // Likewise, the PhaseContext for GeneralListener will be enabled
                    // Note: As per JLS 14.20.3, the resources are closed in the opposite order in which they are initialized
                    // in which case the PhaseContext will unwind and close out before Timings closes out the listener.
                    try (final Timing timing = forgeImpl$isTimedEvent(event) ? ((TimingBridge) listener).bridge$getTimingsHandler() : null;
                         final PhaseContext<?> context = forgeImpl$preEventPhaseCheck((ASMEventHandlerBridge) listener, event)) {
                        // However, we don't want to add to the timing of the event listener for whatever costs may be involved with the PhaseTracker
                        // switching phases.
                        if (context != null) {
                            context.buildAndSwitch();
                        }
                        if (timing != null) {
                            timing.startTimingIfSync();
                        }
                        listener.invoke(event);
                    }
                } else {
                    listener.invoke(event);
                }
            }
        } catch (Throwable throwable) {
            this.exceptionHandler.handleException((EventBus) (Object) this, event, listeners, index, throwable);
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }
        return event.isCancelable() && event.isCanceled();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Redirect(method = "register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/eventhandler/ListenerList;register(ILnet/minecraftforge/fml/common/eventhandler/EventPriority;Lnet/minecraftforge/fml/common/eventhandler/IEventListener;)V",
            remap = false))
    private void forgeImpl$updateEventMapping(final ListenerList list, final int id, final EventPriority priority, final IEventListener listener,
        final Class<? extends Event> eventType, final Object target, final Method method, final ModContainer owner) {
        list.register(id, priority, listener);

        final SpongeModEventManager manager = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager());

        for (final Class clazz : TypeToken.of(eventType).getTypes().rawTypes()) {
            final Collection<Class<? extends org.spongepowered.api.event.Event>> spongeEvents = manager.forgeToSpongeEventMapping.get(clazz);
            if (spongeEvents != null) {
                for (final Class<? extends org.spongepowered.api.event.Event> event : spongeEvents) {
                    manager.checker.registerListenerFor(event);
                }
            }
        }

        forgeImpl$forgeListenerRegistry.put(listener, eventType);
        forgeImpl$forgeListenerEventClasses.add(eventType);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Redirect(method = "unregister",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/eventhandler/ListenerList;unregisterAll(ILnet/minecraftforge/fml/common/eventhandler/IEventListener;)V",
            remap = false))
    private void forgeImpl$updateListenerRegistry(final int id, final IEventListener listener) {
        ListenerList.unregisterAll(id, listener);

        final SpongeModEventManager manager = ((SpongeModEventManager) SpongeImpl.getGame().getEventManager());

        final Class<? extends Event> type = checkNotNull(forgeImpl$forgeListenerRegistry.remove(listener));
        for (final Class clazz: TypeToken.of(type).getTypes().rawTypes()) {
            final Collection<Class<? extends org.spongepowered.api.event.Event>> spongeEvents = manager.forgeToSpongeEventMapping.get(clazz);
            if (spongeEvents != null) {
                for (final Class<? extends org.spongepowered.api.event.Event> event : spongeEvents) {
                    manager.checker.unregisterListenerFor(event);
                }
            }
        }

        // update event class cache
        if (!forgeImpl$forgeListenerRegistry.containsValue(type)) {
            forgeImpl$forgeListenerEventClasses.remove(type);
        }
    }

    @Override
    public Set<Class<? extends Event>> forgeBridge$getEventListenerClassList() {
        return forgeImpl$forgeListenerEventClasses;
    }

    @Override
    public int forgeBridge$getBusID() {
        return this.busID;
    }
}
