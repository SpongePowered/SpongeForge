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
package org.spongepowered.mod.mixin.core.forge;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.event.source.entity.living.player.PlayerPlaceBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = net.minecraftforge.event.ForgeEventFactory.class, remap = false)
public class MixinForgeEventFactory {

    @Inject(method = "onPlayerBlockPlace", at = @At(value = "RETURN", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onPlayerPlaceBlock(EntityPlayer player, BlockSnapshot blockSnapshot, EnumFacing direction,
            CallbackInfoReturnable<PlaceEvent> cir, IBlockState state, PlaceEvent event) {
        PlayerPlaceBlockEvent spongeEvent = (PlayerPlaceBlockEvent) event;
        if (!spongeEvent.isCancelled()) {
            for (BlockTransaction transaction : spongeEvent.getTransactions()) {
                if (transaction.isValid() && transaction.getCustomReplacement().isPresent()) {
                    ((net.minecraftforge.common.util.BlockSnapshot) transaction.getCustomReplacement().get()).restore(true, false);
                }
            }
        }
    }
}
