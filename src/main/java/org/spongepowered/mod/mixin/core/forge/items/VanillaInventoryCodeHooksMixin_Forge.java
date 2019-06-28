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
package org.spongepowered.mod.mixin.core.forge.items;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import javax.annotation.Nullable;

@Mixin(value = VanillaInventoryCodeHooks.class)
public abstract class VanillaInventoryCodeHooksMixin_Forge {

    @Shadow(remap = false) private static ItemStack insertStack(final TileEntity source, final Object destination, final IItemHandler destInventory, final ItemStack stack, final int slot) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow(remap = false) @Nullable private static Pair<IItemHandler, Object> getItemHandler(final IHopper hopper, final EnumFacing side) {
        throw new AbstractMethodError("Shadow");
    }

    // Call Pre Events

    @Inject(method = "insertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;getSizeInventory()I"))
    private static void onTransferItemsOut(final TileEntityHopper hopper, final CallbackInfoReturnable<Boolean> cir, final EnumFacing hopperFacing,
            final Pair<IItemHandler, Object> destinationResult, final IItemHandler itemHandler, final Object destination) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            if (SpongeCommonEventFactory.callTransferPre(InventoryUtil.toInventory(hopper, null), InventoryUtil.toInventory(destination, itemHandler)).isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(remap = false, method = "extractHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getKey()Ljava/lang/Object;"))
    private static void onExtractHook(final IHopper hopper, final CallbackInfoReturnable<Boolean> cir, final Pair<IItemHandler, Object> itemHandlerResult) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            final IItemHandler itemHandler = itemHandlerResult.getKey();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                // Find first item that can be extracted
                if (!itemHandler.extractItem(i, 1, true).isEmpty()) {
                    final Inventory source = InventoryUtil.toInventory(itemHandlerResult.getValue(), itemHandler);
                    if (source.totalItems() != 0) {
                        if (SpongeCommonEventFactory.callTransferPre(source, InventoryUtil.toInventory(hopper, null)).isCancelled()) {
                            cir.setReturnValue(false);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Inject(method = "dropperInsertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private static void onDispense(final World world, final BlockPos pos, final TileEntityDispenser dropper, final int slot, final ItemStack stack,
            final CallbackInfoReturnable<Boolean> cir, final EnumFacing enumFacing, final BlockPos blockPos, final Pair<IItemHandler, Object> destinationResult,
            final IItemHandler itemHandler, final Object destination) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            if (SpongeCommonEventFactory.callTransferPre(InventoryUtil.toInventory(dropper, null), InventoryUtil.toInventory(destination, itemHandler)).isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    // Capture Transactions

    @Redirect(remap = false, method = "putStackInInventoryAllSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/VanillaInventoryCodeHooks;insertStack(Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Object;"
                    + "Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onInsertStack(final TileEntity source, final Object destination, final IItemHandler destInventory, final ItemStack stack, final int slot) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            return SpongeCommonEventFactory.captureTransaction(InventoryUtil.forCapture(source), InventoryUtil.toInventory(destination, destInventory), slot,
                    () -> insertStack(source, destination, destInventory, stack, slot));
        }
        return insertStack(source, destination, destInventory, stack, slot);
    }

    @Redirect(remap = false, method = "extractHook",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/item/ItemStack;", ordinal = 1))
    private static ItemStack onPullItemOut(final IItemHandler handler, final int slot, final int amount, final boolean simulate, final IHopper dest) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            final Object inv = getItemHandler(dest, EnumFacing.UP).getValue();
            final ItemStack origin = handler.getStackInSlot(slot).copy(); // Capture Origin
            final ItemStack result = handler.extractItem(slot, amount, simulate);
            if (!result.isEmpty()) {
                SpongeCommonEventFactory.captureTransaction(InventoryUtil.forCapture(dest), InventoryUtil.toInventory(inv, handler), slot, origin);
            }
            return result;
        }
        return handler.extractItem(slot, amount, simulate);
    }

    @Redirect(method = "extractHook",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/IHopper;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private static void onPullItemIn(final IHopper dest, final int index, final ItemStack stack) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            final ItemStack destStack = dest.getStackInSlot(index).copy(); // Capture Origin
            destStack.shrink(1);
            dest.setInventorySlotContents(index, stack);
            SpongeCommonEventFactory.captureTransaction(InventoryUtil.forCapture(dest), InventoryUtil.toInventory(dest, null), index, destStack);
        } else {
            dest.setInventorySlotContents(index, stack);
        }
    }

    // Post Captured Transactions

    @Inject(method = "insertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
    private static void afterPutStackInSlots(final TileEntityHopper hopper, final CallbackInfoReturnable<Boolean> cir,
            final EnumFacing hopperFacing, final Pair<IItemHandler, Object> destinationResult, final IItemHandler itemHandler,
            final Object destination, final int i, final ItemStack originalSlotContents,
            final ItemStack insertStack, final ItemStack remainder) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            // after putStackInInventoryAllSlots
            if (remainder.isEmpty()) {
                final TrackedInventoryBridge capture = InventoryUtil.forCapture(hopper);
                if (capture == null) {
                    return;
                }
                final Inventory sInv = InventoryUtil.toInventory(hopper, null);
                final Inventory dInv = InventoryUtil.toInventory(destination, itemHandler);
                SpongeCommonEventFactory.captureTransaction(capture, sInv, i, originalSlotContents);
                if (SpongeCommonEventFactory.callTransferPost(capture, sInv, dInv)) {
                    if (originalSlotContents.isEmpty()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "extractHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/IHopper;markDirty()V"))
    private static void onPullItemsDone(final IHopper dest, final CallbackInfoReturnable<Boolean> cir, final Pair<IItemHandler, Object> itemHandlerResult,
            final IItemHandler handler, final int i, final ItemStack extractItem, final int j, final ItemStack destStack) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            SpongeCommonEventFactory.callTransferPost(InventoryUtil.forCapture(dest), InventoryUtil.toInventory(itemHandlerResult.getValue(), handler), InventoryUtil.toInventory(dest, null));
        }
    }

    @Inject(remap = false, method = "dropperInsertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "RETURN", ordinal = 1))
    private static void afterDispense(final World world, final BlockPos pos, final TileEntityDispenser dropper, final int slot, final ItemStack stack,
            final CallbackInfoReturnable<Boolean> cir,
            final EnumFacing enumFacing, final BlockPos blockPos, final Pair<IItemHandler, Object> destinationResult, final IItemHandler itemHandler,
            final Object destination, final ItemStack dispensedStack, final ItemStack remainder) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            // after setInventorySlotContents if return false
            final TrackedInventoryBridge capture = InventoryUtil.forCapture(dropper);
            final Inventory source = InventoryUtil.toInventory(dropper, null);
            final Inventory destInv = InventoryUtil.toInventory(destination, itemHandler);
            SpongeCommonEventFactory.captureTransaction(capture, source, slot, stack);
            SpongeCommonEventFactory.callTransferPost(capture, source, destInv);
        }
    }

}
