package org.spongepowered.mod.mixin.core.fml.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.FMLModContainer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.interfaces.IMixinFMLFingerprintViolationEvent;

@Mixin(FMLModContainer.class)
public class MixinFMLModContainer {

    @Redirect(method = "constructMod", at = @At(value = "INVOKE", target = "Lcom/google/common/eventbus/EventBus;post(Ljava/lang/Object;)V"))
    public void fingerprintViolationPost(EventBus bus, Object event) {
        ((IMixinFMLFingerprintViolationEvent) event).setCause(Cause.of(Sponge.getGame()));
        ((IMixinFMLFingerprintViolationEvent) event).setPlugin((PluginContainer) this);

        // Yes, we post to both.
        bus.post(event);
        SpongeImpl.postEvent((Event) event);
    }

}
