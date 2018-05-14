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
package org.spongepowered.mod.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;
import org.spongepowered.mod.util.WrappedArrayList;

import java.util.ArrayList;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Entity.class, priority = 1001)
public abstract class MixinEntity implements IMixinEntity {

    @Shadow public net.minecraft.world.World world;
    @Shadow public boolean isDead;
    @Shadow(remap = false) public ArrayList<EntityItem> capturedDrops = new WrappedArrayList((Entity) (Object) this, new ArrayList<>());

    @Shadow protected abstract void setSize(float width, float height);


    /**
     * @author blood - May 30th, 2016
     * @author gabizou - May 31st, 2016 - Update for 1.9.4
     *
     * @reason - rewritten to support {@link MoveEntityEvent.Teleport.Portal}
     *
     * @param toDimensionId The id of target dimension.
     */
    @Nullable
    @Overwrite(remap = false)
    public net.minecraft.entity.Entity changeDimension(int toDimensionId, ITeleporter teleporter) {
        if (!this.world.isRemote && !this.isDead) {
            // Sponge Start - Handle teleportation solely in TrackingUtil where everything can be debugged.
            return EntityUtil.transferEntityToDimension(this, toDimensionId, (IMixinITeleporter) teleporter);
            // Sponge End
        }
        return null;
    }

    /**
     * @author gabizou - May 8th, 2018
     * @reason this re-assigns the capture list at the end of a tick if this entity was considered as "per-entity" captures.
     * This avoids leaking the PhaseContext for the entity and the potentila leakage of world objects, etc.
     */
    @Override
    public void clearWrappedCaptureList() {
        this.capturedDrops = new ArrayList<>();

    }
}
