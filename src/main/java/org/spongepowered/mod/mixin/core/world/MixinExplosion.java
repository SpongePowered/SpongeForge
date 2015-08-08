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

import com.flowpowered.math.vector.Vector3i;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.WorldOnExplosionEvent;
import org.spongepowered.api.world.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.util.VecHelper;

import java.util.List;

@Mixin(net.minecraft.world.Explosion.class)
public abstract class MixinExplosion implements Explosion {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "doExplosionA", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onExplosionDetonate("
            + "Lnet/minecraft/world/World;"
            + "Lnet/minecraft/world/Explosion;"
            + "Ljava/util/List;D)V", remap = false))
    public void onFireExplosionDetonate(net.minecraft.world.World world, net.minecraft.world.Explosion explosion,
            List<net.minecraft.entity.Entity> list, double diameter) {
        ExplosionEvent.Detonate event = new ExplosionEvent.Detonate(world, explosion, list);
        MinecraftForge.EVENT_BUS.post(event);
        // Convert our Vector3i positions to BlockPos and sync to vanilla list
        List<Vector3i> spongeBlockPositions = ((WorldOnExplosionEvent) event).getAffectedBlockPositions();
        List affectedBlockPositions = explosion.func_180343_e();
        affectedBlockPositions.clear();
        for (Vector3i vec : spongeBlockPositions) {
            affectedBlockPositions.add(VecHelper.toBlockPos(vec));
        }
    }

}
