/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.entity;

import com.google.common.base.MoreObjects;
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

    public SpongeEntityType(int id, String name, Class<? extends Entity> clazz) {
        this(id, name, "minecraft", clazz);
    }

    public SpongeEntityType(int id, String name, String modId, Class<? extends Entity> clazz) {
        this.entityTypeId = id;
        this.entityName = name;
        this.entityClass = clazz;
        this.modId = modId;
    }

    public SpongeEntityType(EntityRegistration entityRegistration) {
        this.entityTypeId = entityRegistration.getModEntityId();
        this.entityName = entityRegistration.getEntityName();
        this.entityClass = entityRegistration.getEntityClass();
        this.modId = entityRegistration.getContainer().getModId();
    }

    @Override
    public String getId() {
        return this.modId + ":" + this.entityName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends org.spongepowered.api.entity.Entity> getEntityClass() {
        return (Class<? extends org.spongepowered.api.entity.Entity>) this.entityClass;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.entityTypeId)
                .add("name", this.entityTypeId)
                .add("modid", this.modId)
                .add("class", this.entityClass.getName())
                .toString();
    }

}
