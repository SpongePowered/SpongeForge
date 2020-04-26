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
package org.spongepowered.mod.mixin.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.bridge.block.BlockBridge_Forge;

@Mixin(Block.class)
public abstract class BlockMixin_Forge implements BlockBridge_Forge {

    private boolean requiresLocationCheckForLight;
    private boolean requiresLocationCheckForOpacity;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void forge$setUpMethodsFromReflection(final CallbackInfo ci) {
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood

        this.requiresLocationCheckForLight = true;
        this.requiresLocationCheckForOpacity = true;

        // forgeBridge$requiresLocationCheckForLightValue // Forge added method getLightValue that takes a state, world, and position
        try {
            final Class<?>[] args = {IBlockState.class, IBlockAccess.class, BlockPos.class};
            final Class<?> clazz = this.getClass().getMethod("getLightValue", args).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.requiresLocationCheckForLight = false;
            }
        } catch (final Throwable throwable) {
            // Ignore
        }

        // forgeBridge$requiresLocationCheckForOpacity // Forge added method getLightValue that takes a state, world, and position
        try {
            final Class<?>[] args = {IBlockState.class, IBlockAccess.class, BlockPos.class};
            final Class<?> clazz = this.getClass().getMethod("getLightOpacity", args).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.requiresLocationCheckForOpacity = false;
            }
        } catch (final Throwable throwable) {
            // Ignore
        }
    }

    /**
     * @author gabizou - July 23rd, 2019 - 1.12.2
     * @reason Because of the PhaseTracker, we can capture the block
     * drops much easier, but it also requires that we do not allow
     * for Forge's capturedrops list to actually be used, otherwise
     * we run into the issue of loss of positional information.
     *
     * @param threadLocal The thread local to ignore
     * @return Always false, bypassing forge's capture logic
     */
    @Redirect(method = "spawnAsEntity", at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;get()Ljava/lang/Object;", ordinal = 0))
    private static Object forgeImpl$bypassForgeCapture(final ThreadLocal<Boolean> threadLocal) {
        return false; // We never have to bother with the threadlocal for the list if we return false on the "iscapturing" threadlocal.
    }

    @Override
    public boolean forgeBridge$requiresLocationCheckForLightValue() {
        return this.requiresLocationCheckForLight;
    }

    @Override
    public boolean forgeBridge$requiresLocationCheckForOpacity() {
        return this.requiresLocationCheckForOpacity;
    }

}
