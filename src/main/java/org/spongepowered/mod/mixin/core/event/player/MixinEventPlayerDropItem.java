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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.target.entity.item.CreateItemEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.event.callback.CallbackList;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(value = ItemTossEvent.class, remap = false)
public abstract class MixinEventPlayerDropItem extends ItemEvent implements CreateItemEvent {

    @Shadow public EntityPlayer player;
    private final Cause cause = Cause.of(this.player);

    public MixinEventPlayerDropItem(EntityItem itemEntity) {
        super(itemEntity);
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public Transform<World> getTargetTransform() {
        return null;
    }

    @Override
    public Item getTargetEntity() {
        return (Item) this.entityItem;
    }

    @Override
    public boolean isCancelled() {
        return this.isCanceled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.setCanceled(cancel);
    }

}
