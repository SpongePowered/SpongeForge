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
package org.spongepowered.mod.mixin.core.entity.living;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Rotations;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.util.VecHelper;

@Mixin(EntityArmorStand.class)
@Implements(@Interface(iface = ArmorStand.class, prefix = "astand$"))
public abstract class MixinArmorStand extends MixinEntityLivingBase {

    @Shadow private Rotations headRotation;
    @Shadow private Rotations bodyRotation;
    @Shadow private Rotations leftArmRotation;
    @Shadow private Rotations rightArmRotation;
    @Shadow private Rotations leftLegRotation;
    @Shadow private Rotations rightLegRotation;
    @Shadow public abstract void setHeadRotation(Rotations p_175415_1_);
    @Shadow public abstract void setLeftArmRotation(Rotations p_175405_1_);
    @Shadow public abstract void setRightArmRotation(Rotations p_175428_1_);
    @Shadow public abstract void setLeftLegRotation(Rotations p_175417_1_);
    @Shadow public abstract void setRightLegRotation(Rotations p_175427_1_);
    @Shadow public abstract void setBodyRotation(Rotations p_175424_1_);
    @Shadow public abstract boolean getShowArms();
    @Shadow public abstract boolean hasNoBasePlate();
    @Shadow public abstract boolean hasNoGravity();
    @Shadow protected abstract void setNoBasePlate(boolean p_175426_1_);
    @Shadow protected abstract void setNoGravity(boolean p_175425_1_);

    public Vector3d getHeadDirection() {
        return VecHelper.toVector(this.headRotation);
    }

    public void setHeadDirection(Vector3d direction) {
        setHeadRotation(VecHelper.toRotation(direction));
    }

    public Vector3d getBodyRotation() {
        return VecHelper.toVector(this.bodyRotation);
    }

    public void setBodyDirection(Vector3d direction) {
        setBodyRotation(VecHelper.toRotation(direction));
    }

    public Vector3d getLeftArmDirection() {
        return VecHelper.toVector(this.leftArmRotation);
    }

    public void setLeftArmDirection(Vector3d direction) {
        setLeftArmRotation(VecHelper.toRotation(direction));
    }

    public Vector3d getRightArmDirection() {
        return VecHelper.toVector(this.rightArmRotation);
    }

    public void setRightArmDirection(Vector3d direction) {
        setRightArmRotation(VecHelper.toRotation(direction));
    }

    public Vector3d getLeftLegDirection() {
        return VecHelper.toVector(this.leftLegRotation);
    }

    public void setLeftLegDirection(Vector3d direction) {
        setLeftLegRotation(VecHelper.toRotation(direction));
    }

    public Vector3d getRightLegDirection() {
        return VecHelper.toVector(this.rightLegRotation);
    }

    public void setRightLegDirection(Vector3d direction) {
        setRightLegRotation(VecHelper.toRotation(direction));
    }

    public boolean astand$isSmall() {
        return (this.dataWatcher.getWatchableObjectByte(10) & 1) != 0;
    }

    public void astand$setSmall(boolean small) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(10);
        this.dataWatcher.updateObject(10, (byte) (small ? (b0 | 1) : (b0 & -2)));
    }

    public boolean astand$doesShowArms() {
        return this.getShowArms();
    }

    public void astand$setShowArms(boolean showArms) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(10);
        this.dataWatcher.updateObject(10, (byte) (showArms ? (b0 | 4) : (b0 & -5)));
    }

    public boolean astand$hasBasePlate() {
        return !this.hasNoBasePlate();
    }

    public void astand$setHasBasePlate(boolean baseplate) {
        this.setNoBasePlate(!baseplate);
    }

    public boolean astand$hasGravity() {
        return !this.hasNoGravity();
    }

    public void astand$setGravity(boolean gravity) {
        this.setNoGravity(!gravity);
    }
}
