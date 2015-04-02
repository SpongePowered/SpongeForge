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
package org.spongepowered.mod.mixin.core.entity.weather;

import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NonnullByDefault
@Mixin(net.minecraft.entity.effect.EntityLightningBolt.class)
public abstract class MixinEntityLightningBolt extends EntityWeatherEffect implements Lightning {

    private MixinEntityLightningBolt super$;

    private boolean effect = false;

    public MixinEntityLightningBolt(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isEffect() {
        return this.effect;
    }

    @Override
    public void setEffect(boolean effect) {
        this.effect = effect;
    }

    @Inject(method = "onUpdate()V", at = {@At(value = "NEW", args = "class=net.minecraft.util.BlockPos"),
            @At(value = "NEW", args = "class=net.minecraft.util.AxisAlignedBB")}, cancellable = true
        )
    public void onOnUpdate(CallbackInfo ci) {
        if (this.effect) {
            ci.cancel();
        }
    }

    @SoftOverride
    public void readFromNbt(NBTTagCompound compound) {
        this.super$.readFromNbt(compound);
        if (compound.hasKey("effect")) {
            this.effect = compound.getBoolean("effect");
        }
    }

    @SoftOverride
    public void writeToNbt(NBTTagCompound compound) {
        this.super$.writeToNbt(compound);
        compound.setBoolean("effect", this.effect);
    }
}
