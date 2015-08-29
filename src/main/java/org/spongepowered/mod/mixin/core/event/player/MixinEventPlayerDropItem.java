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
package org.spongepowered.mod.mixin.core.event.player;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.inventory.DropItemStackEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.mixin.core.event.inventory.MixinEventItem;

import java.util.List;

@NonnullByDefault
@Mixin(value = ItemTossEvent.class, remap = false)
public abstract class MixinEventPlayerDropItem extends MixinEventItem implements DropItemStackEvent.Post.SourcePlayer {

    @Shadow public EntityPlayer player;

    @Override
    public Player getSourceEntity() {
        return (Player) this.player;
    }

    @Override
    public ImmutableList<EntitySnapshot> getEntitySnapshots() {
        // TODO
        return null;
    }

    @Override
    public List<Item> getEntities() {
        // TODO
        return null;
    }

    @Override
    public List<Item> filterEntityLocations(Predicate<Location<World>> predicate) {
        // TODO
        return null;
    }

    @Override
    public List<Item> filterEntities(Predicate<? extends Entity> predicate) {
        // TODO
        return null;
    }

    @Override
    public Transform<World> getSourceTransform() {
        // TODO
        return null;
    }

    @Override
    public Cause getCause() {
        return Cause.of(this.player);
    }
}
