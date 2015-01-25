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
package org.spongepowered.mod.mixin.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.World;
import org.spongepowered.api.entity.projectile.Arrow;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.projectile.source.UnknownProjectileSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityArrow.class)
@Implements(@Interface(iface = Arrow.class, prefix = "arrow$"))
public abstract class MixinEntityArrow extends Entity implements Arrow {

    @Shadow public double damage;

    @Shadow public int knockbackStrength;

    @Shadow public Entity shootingEntity;

    // Not all ProjectileSources are entities (e.g. BlockProjectileSource).
    // This field is used to store a ProjectileSource that isn't an entity.
    @Nullable
    public ProjectileSource projectileSource;

    @Shadow
    public abstract boolean getIsCritical();

    @Shadow
    public abstract void setIsCritical(boolean critical);

    @Override
    public ProjectileSource getShooter() {
        if (this.projectileSource != null && this.projectileSource instanceof ProjectileSource) {
            return this.projectileSource;
        } else if (this.shootingEntity != null && this.shootingEntity instanceof ProjectileSource) {
            return (ProjectileSource) this.shootingEntity;
        }
        return new UnknownProjectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof Entity) {
            // This allows things like Vanilla kill attribution to take place
            this.shootingEntity = (Entity) shooter;
        } else {
            this.shootingEntity = null;
        }
        this.projectileSource = shooter;
    }

    public double arrow$getDamage() {
        return this.damage;
    }

    public void arrow$setDamage(double damage) {
        this.damage = damage;
    }

    public void arrow$setKnockbackStrength(int knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }

    @Override
    public int getKnockbackStrength() {
        return this.knockbackStrength;
    }

    @Override
    public boolean isCritical() {
        return this.getIsCritical();
    }

    @Override
    public void setCritical(boolean critical) {
        this.setIsCritical(critical);
    }

    public MixinEntityArrow(World worldIn) {
        super(worldIn);
    }
}
