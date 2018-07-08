package org.spongepowered.mod.event;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.RegisteredListener;

public class SpongeEventData {

    private final Event spongeEvent;
    private final Class<? extends net.minecraftforge.fml.common.eventhandler.Event> forgeClass;
    private final RegisteredListener.Cache listenerCache;
    private final boolean useCauseStackManager;
    private net.minecraftforge.fml.common.eventhandler.Event forgeEvent;

    public SpongeEventData(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> forgeClass, boolean useCauseStackManager) {
        this.spongeEvent = spongeEvent;
        this.forgeClass = forgeClass;
        this.listenerCache = ((SpongeModEventManager) Sponge.getEventManager()).getHandlerCache(spongeEvent);
        this.useCauseStackManager = useCauseStackManager;
    }

    public SpongeEventData(ForgeEventData eventData) {
        this.spongeEvent = eventData.getSpongeEvent();
        this.forgeClass = eventData.getForgeEvent().getClass();
        this.listenerCache = eventData.getSpongeListenerCache();
        this.useCauseStackManager = eventData.useCauseStackManager();
        this.forgeEvent = eventData.getForgeEvent();
    }

    public boolean useCauseStackManager() {
        return this.useCauseStackManager;
    }

    public Event getSpongeEvent() {
        return this.spongeEvent;
    }

    public net.minecraftforge.fml.common.eventhandler.Event getForgeEvent() {
        return this.forgeEvent;
    }

    public RegisteredListener.Cache getSpongeListenerCache() {
        return this.listenerCache;
    }

    public Class<? extends net.minecraftforge.fml.common.eventhandler.Event> getForgeClass() {
        return this.forgeClass;
    }

    public void setForgeEvent(net.minecraftforge.fml.common.eventhandler.Event event) {
        this.forgeEvent = event;
    }

    public void propagateCancelled() {
        if (this.spongeEvent instanceof Cancellable && this.forgeEvent.isCancelable()) {
            ((Cancellable) this.spongeEvent).setCancelled(this.forgeEvent.isCanceled());
        }
    }
}
