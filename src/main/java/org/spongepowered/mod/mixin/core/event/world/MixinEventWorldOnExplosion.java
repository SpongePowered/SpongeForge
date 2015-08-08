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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.world.WorldOnExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(ExplosionEvent.Detonate.class)
public abstract class MixinEventWorldOnExplosion extends MixinEventWorldExplosion implements WorldOnExplosionEvent {

    private List<Vector3i> blockList;

    @Shadow private List<net.minecraft.entity.Entity> entityList;

    @Override
    public List<Vector3i> getAffectedBlockPositions() {
        if (this.blockList == null) {
            this.blockList = new ArrayList<Vector3i>();
            for (Object pos : this.explosion.func_180343_e()) {
                this.blockList.add(VecHelper.toVector((BlockPos) pos));
            }
        }
        return this.blockList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Entity> getAffectedEntities() {
        return (List<Entity>) (Object) this.entityList;
    }

}
