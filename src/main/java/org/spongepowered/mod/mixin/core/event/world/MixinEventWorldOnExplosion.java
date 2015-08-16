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
package org.spongepowered.mod.mixin.core.event.world;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.world.WorldOnExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;

import java.util.Iterator;
import java.util.List;

@Mixin(value = ExplosionEvent.Detonate.class, remap = false)
public abstract class MixinEventWorldOnExplosion extends MixinEventWorldExplosion implements WorldOnExplosionEvent {

    @Shadow private List<net.minecraft.entity.Entity> entityList;
    private List<Location<World>> locations, originalLocations;
    private List<Entity> originalEntities;

    @Override
    public List<Location<World>> getLocations() {
        if (this.locations == null) {
            this.locations = Lists.newArrayList();
            for (Object pos : this.explosion.func_180343_e()) {
                this.locations.add(new Location<World>(getWorld(), VecHelper.toVector((BlockPos) pos)));
            }

            this.originalLocations = ImmutableList.copyOf(locations);
        }
        return this.locations;
    }

    @Override
    public void filterLocations(Predicate<Location<World>> predicate) {
        if (((ExplosionEvent.Detonate) (Object) this).isCancelable()) {
            Iterator<Location<World>> iterator = this.getLocations().iterator();
            while (iterator.hasNext()) {
                if (!predicate.apply(iterator.next())) {
                    iterator.remove();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Entity> getEntities() {
        if (originalEntities == null) {
            originalEntities = ImmutableList.copyOf((List<Entity>) (Object) this.entityList);
        }
        return (List<Entity>) (Object) this.entityList;
    }

    @Override
    public void filterEntities(Predicate<Entity> predicate) {
        if (((ExplosionEvent.Detonate) (Object) this).isCancelable()) {
            Iterator<Entity> iterator = this.getEntities().iterator();
            while (iterator.hasNext()) {
                if (!predicate.apply(iterator.next())) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public List<Location<World>> getOriginalLocations() {
        return originalLocations;
    }

    @Override
    public List<Entity> getOriginalEntities() {
        return originalEntities;
    }
}
