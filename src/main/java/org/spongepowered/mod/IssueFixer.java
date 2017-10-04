package org.spongepowered.mod;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "issue_fixer")
public class IssueFixer {

    @Listener
    public void onFishingEvent(Event event) {

        if (event instanceof InteractEvent) {
            System.err.println(event.getClass().getSimpleName() + " -> " + ((InteractEvent) event).getInteractionPoint().get().toString());
        }
    }
}
