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
package org.spongepowered.mod.mixin.core.entity.vehicle;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinMinecart;

@NonnullByDefault
@Mixin(EntityMinecart.class)
public abstract class MixinEntityMinecart extends Entity implements Minecart, IMixinMinecart {

    private double maxSpeed;
    private Vector3d airborneMod;

    public MixinEntityMinecart(World worldIn) {
        super(worldIn);
    }

    @Shadow(remap = false)
    public abstract double getDragAir();
    @Shadow(remap = false)
    public abstract double getMaxSpeed();

    // this method overwrites the vanilla accessor for maximum speed
    @Overwrite
    public double getMaximumSpeed() {
        return this.maxSpeed;
    }

    @Override
    public double getMaximumMinecartSpeed() {
        return getMaximumSpeed();
    }

    // this method overwrites vanilla behavior to allow for a custom deceleration rate on all three axes when airborne
    @Inject(method = "moveDerailedMinecart()V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityMinecart;onGround:Z", ordinal = 2))
    public void implementCustomAirborneDeceleration(CallbackInfo ci) {
        if (!this.isOnGround()) {
            this.motionX /= this.getDragAir();
            this.motionY /= this.getDragAir();
            this.motionZ /= this.getDragAir();
            this.motionX *= this.airborneMod.getX();
            this.motionY *= this.airborneMod.getY();
            this.motionZ *= this.airborneMod.getZ();
        }
    }

}
