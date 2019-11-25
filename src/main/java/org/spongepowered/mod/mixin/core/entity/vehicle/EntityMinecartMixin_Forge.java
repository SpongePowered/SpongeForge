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

import net.minecraft.entity.item.EntityMinecart;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.item.EntityMinecartBridge;

@NonnullByDefault
@Mixin(value = EntityMinecart.class, priority = 1111)
public abstract class EntityMinecartMixin_Forge {

    @Redirect(method = "moveDerailedMinecart",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityMinecart;motionX:D", opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void onGetDragAirX(final EntityMinecart self, final double modifier) {
        self.motionX *= ((EntityMinecartBridge) this).bridge$getAirboneVelocityModifier().getX();
    }

    @Redirect(method = "moveDerailedMinecart",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityMinecart;motionY:D", opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void onGetDragAirY(final EntityMinecart self, final double modifier) {
        self.motionY *= ((EntityMinecartBridge) this).bridge$getAirboneVelocityModifier().getY();
    }

    @Redirect(method = "moveDerailedMinecart",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityMinecart;motionZ:D", opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void onGetDragAirZ(final EntityMinecart self, final double modifier) {
        self.motionZ *= ((EntityMinecartBridge) this).bridge$getAirboneVelocityModifier().getZ();
    }


}
