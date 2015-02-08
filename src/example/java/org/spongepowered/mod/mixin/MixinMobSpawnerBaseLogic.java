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
package org.spongepowered.mod.mixin;

import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin which demonstrates the use of the {@link ModifyArg} and {@link Redirect} annotations.
 */
@Mixin(MobSpawnerBaseLogic.class)
public abstract class MixinMobSpawnerBaseLogic {

    /**
     * <p>If you pay a brief visit to {@link MobSpawnerBaseLogic#updateSpawner} you'll notice the following calls in the method body:</p> 
     *
     * <blockquote><pre>
     *     this.getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
     *     this.getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);</pre>
     * </blockquote>
     *
     * <p>The purpose of the {@link ModifyArg} annotation is to modify <b>exactly one<b> argument from a method invokation. Specifically by having
     * the annotated callback method <em>receive</em> and then <em>return</em> the value in question. This allows the method call to be "proxied"
     * in a limited way, modifying a single argument.</p>
     *
     * <p>Two variations of this hook are available:</p>
     * <ul>
     *     <li>The single-argument hook simply accepts <b>only</b> the argument in question. If there is only a single argument of that type then no
     *     further information is required and the hook will receive and then return the modified value. In our example this would be leveraged by
     *     a method with the signature <code>private EnumParticleTypes onSpawnParticle(EnumParticleTypes pt)</code> because there is only a single
     *     argument with the <em>EnumParticleTypes</em> type in the method signature. For methods with multiple args of the same type, the <em>index
     *     </em> property must be specified to identify the target argument.</li>
     *     <li>The multi-argument hook accepts <b>all</b> the original arguments to the method (as in this example) but can only modify the argument
     *     specified by the <em>return type</em> of the hook method. If multiple args of the same type exist, then the <em>index</em> property must
     *     likewise be specified.</li>
     * </ul>
     *
     * <p>This hook does not interrupt the normal execution of the method, it only allows a single parameter to be modified.</p>
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param a a
     * @param b b
     * @param c c
     * @param params params
     */
    @ModifyArg(method = "updateSpawner()V", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private EnumParticleTypes onSpawnParticle(EnumParticleTypes pt, double x, double y, double z, double a, double b, double c, int... params) {
        if (pt == EnumParticleTypes.SMOKE_NORMAL) {
            return EnumParticleTypes.SPELL;
        } else if (pt == EnumParticleTypes.FLAME) {
            return EnumParticleTypes.HEART;
        }
        return pt;
    }

    /**
     * <p>{@link Redirect} annotations allow a method call to be proxied or even completely suppressed by redirecting the original method call to the
     * annotated method.</p>
     *
     * <p>In this example, the {@link MobSpawnerBaseLogic#resetTimer} method is hooked and redirected to this handler. The signature of the hook
     * method must match the redirected method precisely with the addition of a new first argument which must match the type of the invocation's
     * target, in this case {@link MobSpawnerBaseLogic}. This first variable accepts the reference that the method was going to be invoked upon prior
     * to being redirected.</p>
     *
     * <p>The benefit with {@link Redirect} versus ordinary method call injections, is that the call to the method can be conditionally suppressed if
     * required, and also allows a more sophisticated version of {@link ModifyArg} to be enacted since all parameters are available to the hook method
     * and can be altered as required.</p>
     *
     * <p>For <em>static</em> methods the handler must also be <em>static</em>, and the first argument can be omitted.</p>
     *
     * @param this$0 this$0
     */
    @Redirect(method = "updateSpawner()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/MobSpawnerBaseLogic;resetTimer()V"))
    private void onResetTimer(MobSpawnerBaseLogic this$0) {
        // Do some stuff before calling the method
        System.err.println("The timer for " + this + " was reset!");

        // We could execute some logic here if required
        boolean someCondition = true;

        // Call the original method, but only if someCondition
        if (someCondition) {
            this.resetTimer();
        }
    }

    @Shadow
    private void resetTimer() {
    }

    ;
}
