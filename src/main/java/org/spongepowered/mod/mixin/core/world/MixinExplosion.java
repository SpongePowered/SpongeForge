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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.interfaces.IMixinExplosion;

@NonnullByDefault
@Mixin(value = Explosion.class, remap = false)
public class MixinExplosion implements IMixinExplosion {

    @Shadow
    private float explosionSize;

    @Shadow
    private boolean isFlaming;

    private float explosionYield;

    //TODO: Make this work on client
    @SideOnly(Side.SERVER)
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(World worldIn, Entity exploder, double explosionX, double explosionY, double explosionZ,
            float explosionSize, boolean isFlaming, boolean isSmoking, CallbackInfo ci) {
        this.explosionYield = 1.0F / explosionSize;
    }

    @ModifyArg(
            method = "doExplosionB(Z)V",
            at = @At(
                    value = "INVOKE",
                    target =
                    "Lnet/minecraft/block/Block;dropBlockAsItemWithChance(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;FI)V"))
    private
            float onExplosionB(float yield) {
        LogManager.getLogger().info("Explosion getYield: " + this.getYield());
        LogManager.getLogger().info("Explosion yield: " + this.explosionYield);
        return this.getYield();
    }

    @Override
    public float getRadius() {
        return this.explosionSize;
    }

    @Override
    public void setRadius(float radius) {
        this.explosionSize = radius;
    }

    @Override
    public float getYield() {
        return this.explosionYield;
    }

    @Override
    public void setYield(float yield) {
        this.explosionYield = yield;
    }

    @Override
    public boolean isFlammable() {
        return this.isFlaming;
    }

    @Override
    public void setFlammable(boolean flammable) {
        this.isFlaming = flammable;
    }
}
