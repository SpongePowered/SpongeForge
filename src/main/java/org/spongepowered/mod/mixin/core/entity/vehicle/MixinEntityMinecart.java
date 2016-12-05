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
import net.minecraft.entity.item.EntityMinecart;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@NonnullByDefault
@Mixin(value = EntityMinecart.class, priority = 1111)
public abstract class MixinEntityMinecart implements Minecart {

    private static final String MINECART_MOTION_X_FIELD = "Lnet/minecraft/entity/item/EntityMinecart;motionX:D";
    private static final String MINECART_MOTION_Y_FIELD = "Lnet/minecraft/entity/item/EntityMinecart;motionY:D";
    private static final String MINECART_MOTION_Z_FIELD = "Lnet/minecraft/entity/item/EntityMinecart;motionZ:D";

    @Shadow(remap = false) public abstract double getMaxSpeed();

    // Added in SpongeCommon
    private Vector3d airborneMod;

    @Override
    public double getPotentialMaxSpeed() {
        // Return result of Forge method
        return getMaxSpeed();
    }

    @Redirect(method = "moveDerailedMinecart", at = @At(value = "FIELD", target = MINECART_MOTION_X_FIELD, opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void onGetDragAirX(EntityMinecart self, double modifier) {
        self.motionX *= this.airborneMod.getX();
    }

    @Redirect(method = "moveDerailedMinecart", at = @At(value = "FIELD", target = MINECART_MOTION_Y_FIELD, opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void onGetDragAirY(EntityMinecart self, double modifier) {
        self.motionY *= this.airborneMod.getY();
    }

    @Redirect(method = "moveDerailedMinecart", at = @At(value = "FIELD", target = MINECART_MOTION_Z_FIELD, opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void onGetDragAirZ(EntityMinecart self, double modifier) {
        self.motionZ *= this.airborneMod.getZ();
    }


}
