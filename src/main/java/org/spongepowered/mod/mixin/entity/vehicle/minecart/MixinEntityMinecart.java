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
package org.spongepowered.mod.mixin.entity.vehicle.minecart;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@NonnullByDefault
@Mixin(EntityMinecart.class)
@Implements(@Interface(iface = Minecart.class, prefix = "minecart$"))
public abstract class MixinEntityMinecart extends Entity {

    @Shadow
    public abstract double getDragAir();

    @Shadow
    public abstract boolean isOnGround();

    private double maxSpeed = 0.4D;
    private boolean slowWhenEmpty = true;
    private Vector3d airborneMod = new Vector3d(0.5D, 0.5D, 0.5D);
    private Vector3d derailedMod = new Vector3d(0.5D, 0.5D, 0.5D);

    // this method overwrites the vanilla accessor for maximum speed
    @Overwrite
    public double func_174898_m() {
        return this.maxSpeed;
    }

    // this method overwrite vanilla behavior to allow for a custom deceleration rate on all three axes when airborne
    @Inject(method = "func_180459_n", at = @At(value = "FIELD", target = "net.minecraft.entity.Entity.onGround:Z", ordinal = 2))
    public void implementCustomAirborneDeceleration(CallbackInfo ci) {
        if (!this.isOnGround()) {
            this.motionX /= this.getDragAir();
            this.motionY /= this.getDragAir();
            this.motionZ /= this.getDragAir();
            this.motionX *= airborneMod.getX();
            this.motionY *= airborneMod.getY();
            this.motionZ *= airborneMod.getZ();
        }
    }

    // this method overwrites vanilla behavior to allow for a custom deceleration rate when derailed
    @Inject(method = "func_180459_n", at = @At(value = "INVOKE", target = "net.minecraft.entity.Entity.moveEntity(DDD)V"))
    public void implementCustomDerailedDeceleration(CallbackInfo ci) {
        if (this.isOnGround()) {
            this.motionX /= 0.5D;
            this.motionY /= 0.5D;
            this.motionZ /= 0.5D;
            this.motionX *= derailedMod.getX();
            this.motionY *= derailedMod.getY();
            this.motionZ *= derailedMod.getZ();
        }
    }

    // this method overwrites vanilla behavior to allow the cart not to slow when empty
    @Overwrite
    protected void applyDrag() {
        if (this.riddenByEntity != null || !this.slowWhenEmpty) {
            this.motionX *= 1.0D;
            this.motionY *= 0.0D;
            this.motionZ *= 1.0D;
        }
        else {
            this.motionX *= 0.96D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.96D;
        }
    }

    public MixinEntityMinecart(World worldIn) {
        super(worldIn);
    }

    public double minecart$getMaxSpeed() {
        return this.maxSpeed;
    }

    public void minecart$setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public boolean minecart$doesSlowWhenEmpty() {
        return this.slowWhenEmpty;
    }

    public void minecart$setSlowWhenEmpty(boolean slowWhenEmpty) {
        this.slowWhenEmpty = slowWhenEmpty;
    }

    public Vector3d minecart$getAirborneVelocityMod() {
        return this.airborneMod;
    }

    public void minecart$setAirborneVelocityMod(Vector3d airborneMod) {
        this.airborneMod = airborneMod;
    }

    public Vector3d minecart$getDerailedVelocityMod() {
        return this.derailedMod;
    }

    public void minecart$setDerailedVelocityMod(Vector3d derailedVelocityMod) {
        this.derailedMod = derailedVelocityMod;
    }
}