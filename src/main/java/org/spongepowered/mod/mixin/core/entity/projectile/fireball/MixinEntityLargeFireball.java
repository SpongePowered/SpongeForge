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

import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.world.World;
import org.spongepowered.api.entity.projectile.explosive.fireball.LargeFireball;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@NonnullByDefault
@Mixin(net.minecraft.entity.projectile.EntityLargeFireball.class)
public abstract class MixinEntityLargeFireball extends EntityFireball implements LargeFireball {

    @Shadow
    public int explosionPower = 1;

    private float damage = 6.0f;

    public MixinEntityLargeFireball(World worldIn) {
        super(worldIn);
    }

    @ModifyArg(method = "onImpact(Lnet/minecraft/util/MovingObjectPosition;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    protected float onAttackEntityFrom(float amount) {
        return this.damage;
    }

    @Override
    public double getDamage() {
        return this.damage;
    }

    @Override
    public void setDamage(double damage) {
        this.damage = (float) damage;
    }

    @Override
    public int getExplosionPower() {
        return this.explosionPower;
    }

    @Override
    public void setExplosionPower(int explosionPower) {
        this.explosionPower = explosionPower;
    }
}
