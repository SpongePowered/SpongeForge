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
package org.spongepowered.mod.mixin.core.entity.projectile.fireball;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@NonnullByDefault
@Mixin(net.minecraft.entity.projectile.EntityWitherSkull.class)
public abstract class MixinEntityWitherSkull extends MixinEntityFireball implements WitherSkull {

    private float damage = 0.0f;
    private boolean damageSet = false;

    @ModifyArg(method = "onImpact(Lnet/minecraft/util/MovingObjectPosition;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    protected float onAttackEntityFrom(float amount) {
        return (float) getDamage();
    }

    public double getDamage() {
        if (this.damageSet) {
            return this.damage;
        } else {
            if (this.shootingEntity != null) {
                return 8.0f;
            } else {
                return 5.0f;
            }
        }
    }

    public void setDamage(double damage) {
        this.damageSet = true;
        this.damage = (float) damage;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey("damageAmount")) {
            this.damage = compound.getFloat("damageAmount");
            this.damageSet = true;
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        if (this.damageSet) {
            compound.setFloat("damageAmount", this.damage);
        } else {
            compound.removeTag("damageAmount");
        }
    }

}
