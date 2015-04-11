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
package org.spongepowered.mod.mixin.core.entity.living.monster;

import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityCreeper.class)
@Implements(@Interface(iface = Creeper.class, prefix = "creeper$"))
public abstract class MixinEntityCreeper extends MixinEntityMob {

    @Shadow private int explosionRadius;
    @Shadow private int timeSinceIgnited;
    @Shadow private int fuseTime = 30;
    @Shadow public abstract boolean getPowered();
    @Shadow public abstract void explode();
    @Shadow public abstract void ignite();

    public void creeper$detonate() {
        this.explode();
    }

    public void creeper$ignite() {
        this.ignite();
    }

    public void creeper$ignite(int fuseTicks) {
        this.timeSinceIgnited = 0;
        this.fuseTime = fuseTicks;
        this.ignite();
    }

    public int getFuseDuration() {
        return this.fuseTime - this.timeSinceIgnited;
    }

    public void setFuseDuration(int fuseTicks) {
        this.timeSinceIgnited = this.fuseTime - fuseTicks;
    }

    public boolean isPowered() {
        return getPowered();
    }

    public void setPowered(boolean powered) {
        if (powered) {
            this.dataWatcher.updateObject(17, (byte) 1);
        } else {
            this.dataWatcher.updateObject(17, (byte) 0);
        }
    }

    public int getExplosionRadius() {
        return this.explosionRadius;
    }

    public void setExplosionRadius(int radius) {
        this.explosionRadius = radius;
    }
}
