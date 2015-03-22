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
package org.spongepowered.mod.mixin.core.event.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.entity.EntityExplosionEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinExplosion;
import org.spongepowered.mod.util.VecHelper;
import org.spongepowered.mod.wrapper.BlockWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@NonnullByDefault
@Mixin(value = ExplosionEvent.Detonate.class, remap = false)
public abstract class MixinEventEntityExplosion extends ExplosionEvent implements EntityExplosionEvent {

    @Shadow
    public abstract List<BlockPos> getAffectedBlocks();

    private List<BlockLoc> affectedBlocks;

    public MixinEventEntityExplosion(net.minecraft.world.World world, Explosion explosion) {
        super(world, explosion);
        this.affectedBlocks = new ArrayList<BlockLoc>();
    }

    @Override
    public Entity getEntity() {
        return (Entity) this.explosion.getExplosivePlacedBy();
    }

    //TODO: Implement getCause()

    @Override
    public List<BlockLoc> getBlocks() {
        //TODO:
        //  - Convert List<BlockPos> to List<BlockLoc> in constructor
        //  - Changes made by plugins should somehow be applied to Minecraft's internal list (currently read-only)

        if (this.affectedBlocks.isEmpty() && !this.getAffectedBlocks().isEmpty()) {
            for (BlockPos blockPos : this.getAffectedBlocks()) {
                this.affectedBlocks.add(new BlockWrapper((World) this.world, blockPos));
            }
        }

        return this.affectedBlocks;
    }

    @Override
    public void filter(Predicate<BlockLoc> predicate) {
        checkNotNull(predicate, "The filter predicate cannot be null!");
        Iterator<BlockPos> iterator = this.getAffectedBlocks().iterator();
        while (iterator.hasNext()) {
            if (predicate.apply(new BlockWrapper((World) this.world, iterator.next())) == false) {
                iterator.remove();
            }
        }
    }

    @Override
    public Location getExplosionLocation() {
        return new Location((Extent) this.world, VecHelper.toVector(this.explosion.getPosition()));
    }

    @Override
    public double getYield() {
        return ((IMixinExplosion) this.explosion).getYield();
    }

    @Override
    public void setYield(double yield) {
        ((IMixinExplosion) this.explosion).setYield((float) yield);
    }
}
