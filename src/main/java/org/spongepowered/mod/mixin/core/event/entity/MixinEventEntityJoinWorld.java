/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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
package org.spongepowered.mod.mixin.core.event.entity;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@NonnullByDefault
@Mixin(value = EntityJoinWorldEvent.class, remap = false)
public abstract class MixinEventEntityJoinWorld extends MixinEventEntity implements SpawnEntityEvent {

    protected EntitySnapshot entitySnapshot;
    protected ImmutableList<EntitySnapshot> entitySnapshots;
    protected List<Entity> entities;
    protected Cause cause;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.entity.Entity entity, net.minecraft.world.World world, CallbackInfo ci) {
        if (StaticMixinHelper.processingInternalForgeEvent) {
            return;
        }

        if (!world.isRemote) { // ignore client
            this.entitySnapshot = ((Entity) this.entity).createSnapshot();
            this.entities = new ArrayList<>();
            this.entities.add((Entity) this.entity);
            this.entitySnapshots = ImmutableList.of(this.entitySnapshot);
            this.cause = Cause.of(NamedCause.source(this.entity));
        }
    }

    @Override
    public List<Entity> getEntities() {
        return this.entities;
    }

    @Override
    public List<Entity> filterEntityLocations(Predicate<Location<World>> predicate) {
        Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext()) {
            if (!predicate.test(((Entity) iterator.next()).getLocation())) {
                iterator.remove();
            }
        }
        return this.entities;
    }

    @Override
    public List<Entity> filterEntities(Predicate<Entity> predicate) {
        Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext()) {
            if (!predicate.test((Entity) iterator.next())) {
                iterator.remove();
            }
        }
        return this.entities;
    }

    @Override
    public List<EntitySnapshot> getEntitySnapshots() {
        return this.entitySnapshots;
    }

    @Override
    public World getTargetWorld() {
        return (World) this.entity.worldObj;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
