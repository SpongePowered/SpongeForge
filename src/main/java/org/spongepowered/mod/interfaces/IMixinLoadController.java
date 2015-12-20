package org.spongepowered.mod.interfaces;

import org.spongepowered.api.plugin.PluginContainer;

public interface IMixinLoadController {
    void setActiveModContainer(PluginContainer container);
}
