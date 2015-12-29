package org.spongepowered.mod.interfaces;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

public interface IMixinFMLFingerprintViolationEvent {

    void setCause(Cause cause);

    void setPlugin(PluginContainer container);

}
