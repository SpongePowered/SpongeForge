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
package org.spongepowered.mod.mixin.core.entity.projectile.fireball;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.projectile.source.UnknownProjectileSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(net.minecraft.entity.projectile.EntityFireball.class)
public abstract class MixinEntityFireball extends Entity implements Fireball {

    @Shadow
    public EntityLivingBase shootingEntity;

    @Shadow
    protected abstract void onImpact(MovingObjectPosition p_70227_1_);

    private ProjectileSource projectileSource = null;

    public MixinEntityFireball(World worldIn) {
        super(worldIn);
    }

    @Override
    public ProjectileSource getShooter() {
        if (this.projectileSource == null || this.projectileSource != this.shootingEntity) {
            if (this.shootingEntity != null) {
                this.projectileSource = (ProjectileSource) this.shootingEntity;
            } else {
                this.projectileSource = new UnknownProjectileSource();
            }
        }
        return this.projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        this.projectileSource = shooter;
        if (shooter instanceof EntityLivingBase) {
            this.shootingEntity = (EntityLivingBase) shooter;
        } else {
            this.shootingEntity = null;
        }
    }

    @Override
    public void detonate() {
        this.onImpact(new MovingObjectPosition(null));
    }

}
