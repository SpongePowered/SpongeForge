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
package org.spongepowered.mod.mixin.entityactivation;

import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.mixin.plugin.entityactivation.ActivationRange;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld_Activation implements IMixinWorld {

    @Inject(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;canEntityUpdate(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BY, by = 3, ordinal = 0, remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onUpdateEntityWithOptionalForce(net.minecraft.entity.Entity entity, boolean forceUpdate, CallbackInfo ci, int i, int j,
            boolean isForced, int k, boolean canUpdate) {
        if (forceUpdate && !ActivationRange.checkIfActive(entity)) {
            entity.ticksExisted++;
            ((IModData_Activation) entity).inactiveTick();
            ci.cancel();
        }
    }

}