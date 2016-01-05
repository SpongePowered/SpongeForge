package org.spongepowered.mod.mixin.core.fml.common.event;

import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.plugin.PluginFingerprintViolationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinFMLFingerprintViolationEvent;

import java.io.File;
import java.util.Collection;
import java.util.Set;

@Mixin(value = FMLFingerprintViolationEvent.class, remap = false)
public class MixinFMLFingerprintViolationEvent implements PluginFingerprintViolationEvent, IMixinFMLFingerprintViolationEvent {

    @Shadow public Set<String> fingerprints;
    @Shadow public File source;
    @Shadow public String expectedFingerprint;
    private PluginContainer container;
    private Cause cause;

    @Override
    public PluginContainer getPlugin() {
        return this.container;
    }

    @Override
    public void setPlugin(PluginContainer container) {
        this.container = container;
    }

    @Override
    public String getExpectedFingerprint() {
        return this.expectedFingerprint;
    }

    @Override
    public Collection<String> getFingerprints() {
        return this.fingerprints;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public void setCause(Cause cause) {
        this.cause = cause;
    }

}
