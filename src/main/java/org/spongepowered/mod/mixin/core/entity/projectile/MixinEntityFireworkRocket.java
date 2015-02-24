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
package org.spongepowered.mod.mixin.core.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.projectile.source.UnknownProjectileSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(net.minecraft.entity.item.EntityFireworkRocket.class)
public abstract class MixinEntityFireworkRocket extends Entity implements Firework {

    public MixinEntityFireworkRocket(World worldIn) {
        super(worldIn);
    }

    @Shadow
    private int lifetime;

    @Shadow
    private int fireworkAge;

    private ProjectileSource projectileSource = new UnknownProjectileSource();

    @Override
    public int getFuseDuration() {
        return this.lifetime - this.fireworkAge;
    }

    @Override
    public void setFuseDuration(int fuseTicks) {
        this.lifetime = fuseTicks + this.fireworkAge;
    }

    @Override
    public void detonate() {
        this.lifetime = -1;
    }

    @Override
    public ProjectileSource getShooter() {
        return this.projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        this.projectileSource = shooter;
    }

}
