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

import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.entity.ExplosionPrimeEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinExplosion;

@NonnullByDefault
@Mixin(value = ExplosionEvent.Start.class, remap = false)
public abstract class MixinEventExplosionPrime extends ExplosionEvent implements ExplosionPrimeEvent {

    public MixinEventExplosionPrime(World world, Explosion explosion) {
        super(world, explosion);
    }

    @Override
    public Entity getEntity() {
        return (Entity) this.explosion.getExplosivePlacedBy();
    }

    @Override
    public Game getGame() {
        return SpongeMod.instance.getGame();
    }

    @Override
    public double getRadius() {
        return ((IMixinExplosion) this.explosion).getRadius();
    }

    @Override
    public void setRadius(double radius) {
        ((IMixinExplosion) this.explosion).setRadius((float) radius);
    }

    @Override
    public boolean isFlammable() {
        return ((IMixinExplosion) this.explosion).isFlammable();
    }

    @Override
    public void setFlammable(boolean flammable) {
        ((IMixinExplosion) this.explosion).setFlammable(flammable);
    }
}
