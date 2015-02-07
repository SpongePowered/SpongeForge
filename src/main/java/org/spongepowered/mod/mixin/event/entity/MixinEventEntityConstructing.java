package org.spongepowered.mod.mixin.event.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import org.spongepowered.api.event.entity.EntityConstructingEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;

@NonnullByDefault
@Mixin(value = EntityEvent.EntityConstructing.class, remap = false)
public abstract class MixinEventEntityConstructing extends EntityEvent implements EntityConstructingEvent {

    public MixinEventEntityConstructing(Entity entity) {
        super(entity);
    }
}
