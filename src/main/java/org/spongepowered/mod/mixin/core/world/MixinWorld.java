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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.world.IMixinWorld;

@Mixin(value = World.class, priority = 1001)
public abstract class MixinWorld implements IMixinWorld {

    private boolean callingWorldEvent = false;
    @Shadow @Final public WorldProvider provider;
    @Shadow @Final public boolean isRemote;
    @Shadow protected MapStorage mapStorage;

    @Inject(method = "getWorldInfo", at = @At("HEAD"), cancellable = true)
    public void onGetWorldInfo(CallbackInfoReturnable<WorldInfo> cir) {
        if (this.provider.getDimension() != 0 && this.callingWorldEvent) {
            cir.setReturnValue(DimensionManager.getWorld(0).getWorldInfo());
        }
    }

    @Inject(method = "getMapStorage", at = @At("HEAD"), cancellable = true)
    public void onGetMapStorage(CallbackInfoReturnable<MapStorage> cir)
    {
        // Forge only uses a single save handler so we need to always pass overworld's mapstorage here
        if (!this.isRemote && (this.mapStorage == null || this.provider.getDimension() != 0)) {
            cir.setReturnValue(DimensionManager.getWorld(0).getMapStorage());
        }
    }

    @Override
    public void setCallingWorldEvent(boolean flag) {
        this.callingWorldEvent = flag;
    }
}
