/**
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

import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin demonstrates how injectors work. Injection methods are merged into the target class just like regular mixin methods, the key difference
 * being that they then inject a call to themselves into another method, designated the "target method". The actual position of the injected callback
 * is determined by the values specified in the "at" parameter of the {@link Inject} annotation. A full explanation of the various {@link At} options
 * is beyond the scope of this example but a number of common examples are provided below.
 */
@Mixin(WorldProvider.class)
public abstract class MixinWorldProviderExample {

    /**
     * <p>This method demonstrates injecting into a constructor in the target class. The method name "<init>" is the bytecode name for a constructor
     * and so unlike Java it is necessary to refer to the constructor using this name instead of the class name</p>
     *
     * <p>Injected callback method signatures must match the target method signature with the distinction of always returning void and taking a
     * single additional parameter which will either be a {@link CallbackInfo} or a {@link CallbackInfoReturnable} depending on the method's return
     * type. Methods which return void will require a {@link CallbackInfo} and methods which return a value will require
     * {@link CallbackInfoReturnable}.</p>
     *
     * <p>For a constructor, the only allowed value for "at" is RETURN, this is because injecting into a partly-initialised object can have unforseen
     * side-effects and for this reason injections are restricted to after the object is guaranteed to be fully initialised.</p>
     *
     * <p>It is recommended that injected methods always begin with "on", this helps them be distinguished in stack traces and makes their purpose
     * immediately clear when reading the mixin code.</p>
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        // Uncomment for science
        // System.err.println("Oh look, I'm being called from the constructor!");
    }

    /**
     * <p>This method demonstrates injecting into a method with a return value. Notice that we take the original method, change the return type to
     * <b>void</b> and add a {@link CallbackInfoReturnable} with the original return type ({@link Vec3}) as the type parameter.</p>
     *
     * <p>This method also demonstrates a more precise syntax for identifying the target method. This is useful if there are several methods in the
     * target class with the same name. We simply append the bytecode descriptor of the target method to the method name. For more details on this
     * syntax see the javadoc in {@link org.spongepowered.asm.mixin.injection.struct.MemberInfo}.</p>
     *
     * <p>The {@link At} specified HEAD will inject the callback at the top of the method (before all other code).</p>
     *
     * @param celestialAngle The celestial angle
     * @param partialTicks The partial ticks
     * @param cir The callback
     */
    @Inject(method = "getFogColor(FF)Lnet/minecraft/util/Vec3;", at = @At("HEAD"))
    @SideOnly(Side.CLIENT)
    public void onGetFogColor(float celestialAngle, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        // Uncomment for science
        // System.err.println("The fog colour! It are being gotten!");
    }

    /**
     * <p>This method demonstrates the use of the <em>cancellable</em> argument for an injection. Specifying that an injection is <em>cancellable</em>
     * allows us to supply our own return values and short-circuit the target method's normal logic.</p>
     *
     * <p>Choosing the appropriate {@link At} is very important when dealing with cancellable callbacks. For example you may with to be able to
     * "short-circuit" a method by injecting at the HEAD and cancelling it if you don't want the method to be executed. However if you want the method
     * to execute but be able to change the result, then injecting at RETURN makes more sense. Injecting at RETURN also allows you to see the value
     * the method was going to return and optionally change it. The {@link CallbackInfoReturnable#getReturnValue} method can be used to get the return
     * value from the stack, but <b>only</b> when using the RETURN injection point.</p>
     *
     * <p>It should be noted that it's perfectly possible to specify <em>cancellable</em> when injecting into a method which returns void, but with
     * the key difference being that it's not possible to fetch the return value (because there isn't one) or set a return value (because there isn't
     * one!) but it is still perfectly possible to short-circuit a method in this way.</p>
     *
     * @param x The x coordinate
     * @param z The z coordinate
     * @param cir The callback
     */
    @Inject(method = "canCoordinateBeSpawn", at = @At("RETURN"), cancellable = true)
    public void onCanCoordinateBeSpawn(int x, int z, CallbackInfoReturnable<Boolean> cir) {

        int coordinateWeDontLike = 666;

        if (x == coordinateWeDontLike || z == coordinateWeDontLike) {
            if (cir.getReturnValue()) {
                System.err.println("WorldProvider " + this + " thought that " + x + ", " + z + " was a good spot for spawn. But I disagree!");
            }

            // Note that calling setReturnValue in a non-cancellable callback will throw a CancellationException! 
            cir.setReturnValue(false);
        }
    }

    /**
     * <p>What's this? A parameterised {@link At}? Surely not!</p>
     *
     * <p>{@link org.spongepowered.asm.mixin.injection.points.MethodHead HEAD} and
     * {@link org.spongepowered.asm.mixin.injection.points.BeforeReturn RETURN} are only two of the available values for {@link At} types and are the
     * most straightforward to understand. HEAD only ever makes a single injection (at the head of the method) and RETURN injects before <em>every
     * RETURN opcode</em> in a method. Other injection types are available however:</p>
     *
     * <dl>
     *   <dt>{@link org.spongepowered.asm.mixin.injection.points.BeforeInvoke INVOKE}</dt>
     *   <dd>searches for method invocations matching its parameters and injects immediately prior to any matching invocations</dd>
     *   <dt>{@link org.spongepowered.asm.mixin.injection.points.BeforeFieldAccess FIELD}</dt>
     *   <dd>searches for field accesses (get or set) matching its parameters and injects immediately prior to any matching access</dd>
     *   <dt>{@link org.spongepowered.asm.mixin.injection.points.BeforeNew NEW}</dt>
     *   <dd>searches for object instantiation (<b>new</b> keywords) matching its parameters and injects prior to the NEW opcode</dd>
     *   <dt>{@link org.spongepowered.asm.mixin.injection.points.BeforeStringInvoke INVOKE_STRING}</dt>
     *   <dd>is a specialised version of INVOKE which searches for a method invocation of a method which accepts a single String argument and also
     *     matches the specified string literal. This is very useful for finding calls to Profiler::startSection() with a particular argument.</dd>
     *   <dt>{@link org.spongepowered.asm.mixin.injection.points.JumpInsnPoint JUMP}</dt>
     *   <dd>searches for specific JUMP opcodes</dd>
     *   <dt><em>Fully-qualified class name</em></dt>
     *   <dd>Allows you to specify a custom class which extends {@link org.spongepowered.asm.mixin.injection.InjectionPoint} to implement any custom
     *     logic you wish</dd>
     * </dl>
     *
     * <p>The specific arguments accepted by each type of invokation are described in each class's javadoc. This example shows a simple use of the
     * INVOKE type.</p>
     *
     * <p>This is what the code in the target method looks like:</p>
     * <blockquote><pre>
     *     this.worldObj = worldIn;
     *     this.terrainType = worldIn.getWorldInfo().getTerrainType();
     *     this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
     *     // we want to inject a callback to our method here, immediately prior to calling registerWorldChunkManager
     *     this.registerWorldChunkManager();
     *     this.generateLightBrightnessTable();
     * </pre></blockquote>
     * <p>Having identified the target method, we simply supply the method name as the <em>target</em> argument to the {@link At} annotation. Note
     * that
     * unlike the <em>method</em> parameter (which <b>must</b> refer to a method in the target class) the <em>target</em> parameter for the {@link At}
     * <b>must</b> be a <em>fully-qualified</em> member reference (include both the owner and signature) because the obfuscation processor requires
     * this information in order to look up the target member in the obfuscation tables.</p>
     *
     * @param worldIn The world to register
     * @param ci The callback on register
     */
    @Inject(method = "registerWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;registerWorldChunkManager()V"))
    public void onRegisterWorld(World worldIn, CallbackInfo ci) {
        // I will be called immediately before the call to registerWorldChunkManager. Just like magic.
    }
}
