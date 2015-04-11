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
package org.spongepowered.mod.mixin.core.entity.living.animal;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;

import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.animal.DyeColor;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.mixin.core.entity.living.MixinEntityTameable;

@NonnullByDefault
@Mixin(EntityWolf.class)
@Implements(@Interface(iface = Wolf.class, prefix = "wolf$"))
public abstract class MixinEntityWolf extends MixinEntityTameable {

    @Shadow
    public abstract EnumDyeColor getCollarColor();

    @Shadow
    public abstract void setCollarColor(EnumDyeColor p_175547_1_);

    public MixinEntityWolf(World worldIn) {
        super(worldIn);
    }

    public DyeColor wolf$getColor() {
        return (DyeColor) (Object) this.getCollarColor();
    }

    public void wolf$setColor(DyeColor color) {
        this.setCollarColor((EnumDyeColor) (Object) color);
    }

    @Intrinsic
    public void wolf$setTamed(boolean tamed) {
        this.setTamed(tamed);
    }

    public void wolf$setOwner(Tamer tamer) {
        super.sittable$setOwner(tamer);
        if (tamer != null) {
            this.setTamed(true);
            this.navigator.clearPathEntity();
            this.setAttackTarget((EntityLivingBase) null);
            this.aiSit.setSitting(true);
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        } else {
            this.setTamed(false);
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        }
    }

    /*public void wolf$setTamed(boolean tamed) {
        super.setTamed(tamed);
        System.out.println("Overridden");
        if (tamed) {
            this.navigator.clearPathEntity();
            this.setAttackTarget((EntityLivingBase) null);
            this.aiSit.setSitting(true);
        }
    }*/
}
