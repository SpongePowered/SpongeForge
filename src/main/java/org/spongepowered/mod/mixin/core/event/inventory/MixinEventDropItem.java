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
package org.spongepowered.mod.mixin.core.event.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.mixin.core.event.entity.MixinEventEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.item.ItemEvent.class, remap = false)
public abstract class MixinEventDropItem extends MixinEventEntity implements DropItemEvent.Custom {

    protected EntitySnapshot entitySnapshot;
    protected ImmutableList<EntitySnapshot> entitySnapshots;
    protected List<Entity> entities;
    @Shadow public EntityItem entityItem;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityItem itemEntity, CallbackInfo ci) {
        this.entitySnapshot = ((Entity) itemEntity).createSnapshot();
        this.entities = new ArrayList<>();
        this.entities.add((Entity) itemEntity);
        this.entitySnapshots = ImmutableList.of(this.entitySnapshot);
    }

    @Override
    public List<Entity> getEntities() {
        return this.entities;
    }

    @Override
    public List<Entity> filterEntityLocations(Predicate<Location<World>> predicate) {
        Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext()) {
            if (!predicate.test(((Item) iterator.next()).getLocation())) {
                iterator.remove();
            }
        }
        return this.entities;
    }

    @Override
    public List<Entity> filterEntities(Predicate<Entity> predicate) {
        Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext()) {
            if (!predicate.test((Item) iterator.next())) {
                iterator.remove();
            }
        }
        return this.entities;
    }

    @Override
    public List<EntitySnapshot> getEntitySnapshots() {
        return this.entitySnapshots;
    }

    @Mixin(value = ItemTossEvent.class, remap = false)
    static abstract class Toss extends MixinEventDropItem implements DropItemEvent.Dispense {

        @Shadow public EntityPlayer player;

        @Override
        public World getTargetWorld() {
            return (World) this.player.worldObj;
        }

        @Override
        public Cause getCause() {
            return Cause.of(this.player);
        }
    }

}
