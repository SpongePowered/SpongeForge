package org.spongepowered.mod.mixin.event.entity;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.entity.EntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.EntityEvent.class, remap = false)
public abstract class MixinEventEntity implements EntityEvent {

    @Shadow public net.minecraft.entity.Entity entity;

    @Override
    public Entity getEntity() {
        return (Entity)this.entity;
    }
}
