package org.spongepowered.mod.entity;

import net.minecraft.entity.Entity;

public interface SpongeEntityRegistry {

    void registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates);
}
