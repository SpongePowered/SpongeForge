package org.spongepowered.mod.mixin.event.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import org.spongepowered.api.event.entity.EntitySpawnEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.EntityJoinWorldEvent.class, remap = false)
public abstract class MixinEventEntityJoinWorld extends EntityEvent implements EntitySpawnEvent {

    public MixinEventEntityJoinWorld(Entity entity) {
        super(entity);
    }

    @Override
    public Location getLocation() {
        return getEntity().getLocation();
    }
}
