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
package org.spongepowered.mod.mixin.core.forge.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.mod.mixin.core.block.BlockMixin_Forge;

import java.util.Map;

@Mixin(BlockFluidBase.class)
public abstract class BlockFluidBaseMixin_Forge extends BlockMixin_Forge implements BlockBridge {

    @Shadow(remap = false) @Final public static PropertyInteger LEVEL;
    @Shadow(remap = false) protected int tickRate;

    @Redirect(method = "canDisplace",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
            remap = false
        )
    )
    private Object getDisplacementWithSponge(final Map<?, ?> map, final Object key, final IBlockAccess world, final BlockPos pos) {
        if (!ShouldFire.CHANGE_BLOCK_EVENT_PRE || ((WorldBridge) world).bridge$isFake()) {
            return map.get(key);
        }
        if (!((Boolean) map.get(key))) {
            return Boolean.FALSE;
        }
        final ChangeBlockEvent.Pre event = SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos);
        if (event.isCancelled()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Inject(method = "canDisplace",
        cancellable = true,
        remap = false,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/BlockFluidBase;getDensity(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
            remap = false
        )
    )
    private void onSpongeInjectFailEvent(final IBlockAccess world, final BlockPos pos, final CallbackInfoReturnable<Boolean> cir,
        final IBlockState state, final Block block) {
        if (!ShouldFire.CHANGE_BLOCK_EVENT_PRE || ((WorldBridge) world).bridge$isFake()) {
            return;
        }
        final ChangeBlockEvent.Pre event = SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
    

}
