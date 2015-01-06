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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NonnullByDefault
@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat extends Entity implements Boat {

    private double maxSpeed = 0.4D;
    private boolean moveOnLand = false;
    private double occupiedDecelerationSpeed;
    private double unoccupiedDecelerationSpeed;

    // this method overwrites vanilla boat behavior to allow for a custom max speed
    @Overwrite
    public void updateRiderPosition() {
        if (this.riddenByEntity != null) {
            double d0 = Math.cos(this.rotationYaw * Math.PI / 180.0D) * maxSpeed;
            double d1 = Math.sin(this.rotationYaw * 3.141592653589793D / 180.0D) * maxSpeed;
            this.riddenByEntity.setPosition(this.posX + d0, this.posY + getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + d1);
        }
    }

    @Inject(method = "onUpdate()V", at = @At(value = "INVOKE", target = "net.minecraft.entity.Entity.moveEntity(DDD)V"))
    public void implementLandBoats(CallbackInfo ci){
        if (this.onGround && this.moveOnLand){
            this.motionX /= 0.5D;
            this.motionY /= 0.5D;
            this.motionZ /= 0.5D;
        }
    }

    @Inject(method = "onUpdate()V", at = @At(value = "FIELD", target = "net.minecraft.entity.Entity.riddenByEntity:Z", ordinal = 0))
    public void implementCustomDeceleration(CallbackInfo ci){
        if (!(this.riddenByEntity instanceof EntityLivingBase)){
            double decel = this.riddenByEntity == null ? unoccupiedDecelerationSpeed : occupiedDecelerationSpeed;
            this.motionX *= decel;
            this.motionZ *= decel;
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
