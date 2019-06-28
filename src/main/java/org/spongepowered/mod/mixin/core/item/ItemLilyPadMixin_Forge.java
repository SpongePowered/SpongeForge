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
package org.spongepowered.mod.mixin.core.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemLilyPad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.mixin.core.item.ItemMixin;

import javax.annotation.Nullable;

@Mixin(ItemLilyPad.class)
public abstract class ItemLilyPadMixin_Forge extends ItemMixin {

    @Redirect(method = "onItemRightClick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/util/BlockSnapshot;getBlockSnapshot(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraftforge/common/util/BlockSnapshot;",
            remap = false
        )
    )
    private BlockSnapshot sponge$IgnoreSnapshotCreationDuetoTracking(final World world, final BlockPos pos) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Redirect(method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerBlockPlace(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraftforge/common/util/BlockSnapshot;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/EnumHand;)Lnet/minecraftforge/event/world/BlockEvent$PlaceEvent;",
            remap = false
        )
    )
    @Nullable
    private BlockEvent.PlaceEvent sponge$IgnoreForgeEventDueToTracker(final EntityPlayer player, final BlockSnapshot blockSnapshot,
        final EnumFacing direction, final EnumHand hand) {
        return null;
    }

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @reason Because we null out the event creation so that we can ignore the cancellation, we also
     * need to redirect the actual event call for isCancelled so it doesn't NPE. This always returns false.
     * In essence though, this will effectively turn Forge's event changes to three method calls
     * and one of them being always constant. Certainly better than performing an overwrite that needs to be maintained.
     *
     * @param placeEvent The place event
     * @return Always false, we handle this in the Sponge implementation for interactions
     */
    @Redirect(method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/world/BlockEvent$PlaceEvent;isCanceled()Z",
            remap = false)
    )
    @SuppressWarnings("deprecation")
    private boolean sponge$IgnoreEventCancellation(final BlockEvent.PlaceEvent placeEvent) {
        return false;
    }
}
