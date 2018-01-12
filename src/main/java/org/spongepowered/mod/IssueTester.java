package org.spongepowered.mod;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "issue")
public class IssueTester {

  @Listener
  public void onEvent(UseItemStackEvent event) {
    System.err.println(event);
  }
}
