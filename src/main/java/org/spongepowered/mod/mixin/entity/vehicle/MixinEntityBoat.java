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
package org.spongepowered.mod.mixin.entity.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.world.World;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NonnullByDefault
@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat extends Entity implements Boat {

    @Shadow private double speedMultiplier;

    private double maxSpeed;
    private boolean moveOnLand;
    private double occupiedDecelerationSpeed;
    private double unoccupiedDecelerationSpeed;

    private double tempMotionX;
    private double tempMotionZ;
    private double tempSpeedMultiplier;
    private double initialDisplacement;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(World world, CallbackInfo ci) {
        this.maxSpeed = 0.35;
        this.moveOnLand = false;
        this.occupiedDecelerationSpeed = 0f;
        this.unoccupiedDecelerationSpeed = 0.8f;
    }

    @Inject(method = "onUpdate()V", at = @At(value = "INVOKE", target = "net.minecraft.entity.Entity.moveEntity(DDD)V"))
    public void implementLandBoats(CallbackInfo ci) {
        if (this.onGround && this.moveOnLand) {
            this.motionX /= 0.5;
            this.motionY /= 0.5;
            this.motionZ /= 0.5;
        }
    }

    @Inject(method = "onUpdate()V", at = @At(value = "INVOKE", target = "java.lang.Math.sqrt(D)D", ordinal = 0))
    public void beforeModifyMotion(CallbackInfo ci) {
        this.initialDisplacement = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    }

    @Inject(method = "onUpdate()V", at = @At(value = "INVOKE", target = "java.lang.Math.sqrt(D)D", ordinal = 1))
    public void beforeLimitSpeed(CallbackInfo ci) {
        this.tempMotionX = this.motionX;
        this.tempMotionZ = this.motionZ;
        this.tempSpeedMultiplier = this.speedMultiplier;
    }

    @Inject(method = "onUpdate()V", at = @At(value = "FIELD", target = "net.minecraft.entity.Entity.onGround:Z", ordinal = 1))
    public void afterLimitSpeed(CallbackInfo ci) {
        this.motionX = this.tempMotionX;
        this.motionZ = this.tempMotionZ;
        this.speedMultiplier = this.tempSpeedMultiplier;
        double displacement = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (displacement > this.maxSpeed) {
            double ratio = this.maxSpeed / displacement;
            this.motionX *= ratio;
            this.motionZ *= ratio;
            displacement = this.maxSpeed;
        }

        if ((displacement > this.initialDisplacement) && (this.speedMultiplier < this.maxSpeed)) {
            this.speedMultiplier += (this.maxSpeed - this.speedMultiplier) / this.maxSpeed * 100.0;
            this.speedMultiplier = Math.min(this.speedMultiplier, this.maxSpeed);
        } else {
            this.speedMultiplier -= (this.speedMultiplier - 0.07) / this.maxSpeed * 100.0;
            this.speedMultiplier = Math.max(this.speedMultiplier, 0.07);
        }
    }

    @Inject(method = "onUpdate()V",
            at = @At(value = "FIELD", target = "net.minecraft.entity.Entity.riddenByEntity:Lnet/minecraft/entity/Entity;", ordinal = 0))
    public void implementCustomDeceleration(CallbackInfo ci) {
        if (!(this.riddenByEntity instanceof EntityLivingBase)) {
            double decel = this.riddenByEntity == null ? this.unoccupiedDecelerationSpeed : this.occupiedDecelerationSpeed;
            this.motionX *= decel;
            this.motionZ *= decel;

            if (this.motionX < 0.00005) {
                this.motionX = 0.0;
            }

            if (this.motionZ < 0.00005) {
                this.motionZ = 0.0;
            }
        }
    }

    public MixinEntityBoat(World worldIn) {
        super(worldIn);
    }

    @Override
    public double getMaxSpeed() {
        return this.maxSpeed;
    }

    @Override
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public boolean canMoveOnLand() {
        return this.moveOnLand;
    }

    @Override
    public void setMoveOnLand(boolean moveOnLand) {
        this.moveOnLand = moveOnLand;
    }

    @Override
    public double getOccupiedDeceleration() {
        return this.occupiedDecelerationSpeed;
    }

    @Override
    public void setOccupiedDeceleration(double occupiedDeceleration) {
        this.occupiedDecelerationSpeed = occupiedDeceleration;
    }

    @Override
    public double getUnoccupiedDeceleration() {
        return this.unoccupiedDecelerationSpeed;
    }

    @Override
    public void setUnoccupiedDeceleration(double unoccupiedDeceleration) {
        this.unoccupiedDecelerationSpeed = unoccupiedDeceleration;
    }
}
