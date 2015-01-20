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
package org.spongepowered.mod.mixin.entity.living;

import com.flowpowered.math.vector.Vector3f;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Rotations;

import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityArmorStand.class)
@Implements(@Interface(iface = ArmorStand.class, prefix = "astand$"))
public abstract class MixinArmorStand extends EntityLivingBase {

    @Shadow private Rotations headRotation;

    @Shadow private Rotations bodyRotation;

    @Shadow private Rotations leftArmRotation;

    @Shadow private Rotations rightArmRotation;

    @Shadow private Rotations leftLegRotation;

    @Shadow private Rotations rightLegRotation;

    @Shadow
    public abstract void setHeadRotation(Rotations p_175415_1_);

    @Shadow
    public abstract void setBodyRotation(Rotations p_175424_1_);

    @Shadow
    public abstract void setLeftArmRotation(Rotations p_175405_1_);

    @Shadow
    public abstract void setRightArmRotation(Rotations p_175428_1_);

    @Shadow
    public abstract void setLeftLegRotation(Rotations p_175417_1_);

    @Shadow
    public abstract void setRightLegRotation(Rotations p_175427_1_);

    @Shadow
    public abstract boolean getShowArms();

    @Shadow
    public abstract boolean hasNoBasePlate();

    @Shadow
    protected abstract void setNoBasePlate(boolean p_175426_1_);

    public MixinArmorStand(net.minecraft.world.World worldIn) {
        super(worldIn);
    }

    public Vector3f astand$getHeadDirection() {
        return new Vector3f(this.headRotation.func_179415_b(), this.headRotation.func_179416_c(), this.headRotation.func_179413_d());
    }

    public void astand$setHeadDirection(Vector3f direction) {
        setHeadRotation(new Rotations(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Vector3f astand$getBodyRotation() {
        return new Vector3f(this.bodyRotation.func_179415_b(), this.bodyRotation.func_179416_c(), this.bodyRotation.func_179413_d());
    }

    public void astand$setBodyDirection(Vector3f direction) {
        setBodyRotation(new Rotations(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Vector3f astand$getLeftArmDirection() {
        return new Vector3f(this.leftArmRotation.func_179415_b(), this.leftArmRotation.func_179416_c(), this.leftArmRotation.func_179413_d());
    }

    public void astand$setLeftArmDirection(Vector3f direction) {
        setLeftArmRotation(new Rotations(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Vector3f astand$getRightArmDirection() {
        return new Vector3f(this.rightArmRotation.func_179415_b(), this.rightArmRotation.func_179416_c(), this.rightArmRotation.func_179413_d());
    }

    public void astand$setRightArmDirection(Vector3f direction) {
        setRightArmRotation(new Rotations(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Vector3f astand$getLeftLegDirection() {
        return new Vector3f(this.leftLegRotation.func_179415_b(), this.leftLegRotation.func_179416_c(), this.leftLegRotation.func_179413_d());
    }

    public void astand$setLeftLegDirection(Vector3f direction) {
        setLeftLegRotation(new Rotations(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Vector3f astand$getRightLegDirection() {
        return new Vector3f(this.rightLegRotation.func_179415_b(), this.rightLegRotation.func_179416_c(), this.rightLegRotation.func_179413_d());
    }

    public void astand$setRightLegDirection(Vector3f direction) {
        setRightLegRotation(new Rotations(direction.getX(), direction.getY(), direction.getZ()));
    }

    public boolean astand$isSmall() {
        return (this.dataWatcher.getWatchableObjectByte(10) & 1) != 0;
    }

    public void astand$setSmall(boolean small) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(10);
        this.dataWatcher.updateObject(10, Byte.valueOf((byte) (small ? (b0 | 1) : (b0 & -2))));
    }

    public boolean astand$doesShowArms() {
        return this.getShowArms();
    }

    public void astand$setShowArms(boolean showArms) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(10);
        this.dataWatcher.updateObject(10, Byte.valueOf((byte) (showArms ? (b0 | 4) : (b0 & -5))));
    }

    public boolean astand$hasBasePlate() {
        return !this.hasNoBasePlate();
    }

    public void astand$setHasBasePlate(boolean baseplate) {
        this.setNoBasePlate(!baseplate);
    }
}
