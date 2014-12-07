package org.spongepowered.mod.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;

import org.spongepowered.api.entity.EntityType;

public class SpongeEntityType implements EntityType {

    public final int entityTypeId;
    public final String entityName;
    public final String modId;
    public final Class<? extends Entity> entityClass;
    // currently not used
    public int trackingRange;
    public int updateFrequency;
    public boolean sendsVelocityUpdates;

    public SpongeEntityType(int id, String name, Class<? extends Entity> clazz)
    {
        this(id, name, "minecraft", clazz);
    }

    public SpongeEntityType(int id, String name, String modId, Class<? extends Entity> clazz)
    {
        this.entityTypeId = id;
        this.entityName = name;
        this.entityClass = clazz;
        this.modId = modId;
    }

    public SpongeEntityType(EntityRegistration entityRegistration)
    {
        this.entityTypeId = entityRegistration.getModEntityId();
        this.entityName = entityRegistration.getEntityName();
        this.entityClass = entityRegistration.getEntityClass();
        this.modId = entityRegistration.getContainer().getModId();
    }

    @Override
    public String getId() {
        return this.modId + ":" + entityName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SpongeEntityType other = (SpongeEntityType) obj;
        if (!this.entityName.equals(other.entityName)) {
            return false;
        } else if (!this.entityClass.equals(other.entityClass)) {
            return false;
        } else if (this.entityTypeId != other.entityTypeId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.entityTypeId;
    }
}
