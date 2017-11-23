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

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
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
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import javax.annotation.Nullable;

@Mixin(value = VanillaInventoryCodeHooks.class)
public abstract class MixinVanillaInventoryCodeHooks {

    @Shadow private static ItemStack insertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow @Nullable public static Pair<IItemHandler, Object> getItemHandler(World worldIn, double x, double y, double z, EnumFacing side) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow @Nullable private static Pair<IItemHandler, Object> getItemHandler(IHopper hopper, EnumFacing side) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow protected static boolean isFull(IItemHandler itemHandler) {
        throw new AbstractMethodError("Shadow");
    }

    // Call Pre Events

    @Inject(method = "insertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;getSizeInventory()I"))
    private static void onTransferItemsOut(TileEntityHopper hopper, CallbackInfoReturnable<Boolean> cir, EnumFacing hopperFacing,
            Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler, Object destination) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            if (SpongeCommonEventFactory.callTransferPre(toInventory(hopper), toInventory(destination, itemHandler)).isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(remap = false, method = "extractHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getKey()Ljava/lang/Object;"))
    private static void onExtractHook(IHopper hopper, CallbackInfoReturnable<Boolean> cir, Pair<IItemHandler, Object> itemHandlerResult) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            Inventory source = toInventory(itemHandlerResult.getValue(), itemHandlerResult.getKey());
            if (source.totalItems() != 0) {
                if (SpongeCommonEventFactory.callTransferPre(source, toInventory(hopper)).isCancelled()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "dropperInsertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private static void onDispense(World world, BlockPos pos, TileEntityDispenser dropper, int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir, EnumFacing enumFacing, BlockPos blockPos, Pair<IItemHandler, Object> destinationResult,
            IItemHandler itemHandler, Object destination) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            if (SpongeCommonEventFactory.callTransferPre(toInventory(dropper), toInventory(destination, itemHandler)).isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    // Capture Transactions

    @Redirect(remap = false, method = "putStackInInventoryAllSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/VanillaInventoryCodeHooks;insertStack(Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Object;"
                    + "Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onInsertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            return SpongeCommonEventFactory.captureTransaction(forCapture(source), toInventory(destination, destInventory), slot,
                    () -> insertStack(source, destination, destInventory, stack, slot));
        }
        return insertStack(source, destination, destInventory, stack, slot);
    }

    @Redirect(remap = false, method = "extractHook",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/item/ItemStack;", ordinal = 1))
    private static ItemStack onPullItemOut(IItemHandler handler, int slot, int amount, boolean simulate, IHopper dest) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            Object inv = getItemHandler(dest, EnumFacing.UP).getValue();
            ItemStack origin = handler.getStackInSlot(slot).copy(); // Capture Origin
            ItemStack result = handler.extractItem(slot, amount, simulate);
            SpongeCommonEventFactory.captureTransaction(forCapture(dest), toInventory(inv, handler), slot, origin);
            return result;
        }
        return handler.extractItem(slot, amount, simulate);
    }

    @Redirect(method = "extractHook",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/IHopper;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private static void onPullItemIn(IHopper dest, int index, ItemStack stack) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            ItemStack destStack = dest.getStackInSlot(index).copy(); // Capture Origin
            destStack.shrink(1);
            dest.setInventorySlotContents(index, stack);
            SpongeCommonEventFactory.captureTransaction(forCapture(dest), toInventory(dest), index, destStack);
        } else {
            dest.setInventorySlotContents(index, stack);
        }
    }

    // Post Captured Transactions

    @Inject(method = "insertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
    private static void afterPutStackInSlots(TileEntityHopper hopper, CallbackInfoReturnable<Boolean> cir,
            EnumFacing hopperFacing, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler,
            Object destination, int i, ItemStack originalSlotContents,
            ItemStack insertStack, ItemStack remainder) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            // after putStackInInventoryAllSlots
            if (remainder.isEmpty()) {
                IMixinInventory capture = forCapture(hopper);
                if (capture == null) {
                    return;
                }
                Inventory sInv = toInventory(hopper, null);
                Inventory dInv = toInventory(destination, itemHandler);
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
    private static void onPullItemsDone(IHopper dest, CallbackInfoReturnable<Boolean> cir, Pair<IItemHandler, Object> itemHandlerResult,
            IItemHandler handler, int i, ItemStack extractItem, int j, ItemStack destStack) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            SpongeCommonEventFactory.callTransferPost(forCapture(dest), toInventory(itemHandlerResult.getValue(), handler), toInventory(dest));
        }
    }

    @Inject(remap = false, method = "dropperInsertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "RETURN", ordinal = 1))
    private static void afterDispense(World world, BlockPos pos, TileEntityDispenser dropper, int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir,
            EnumFacing enumFacing, BlockPos blockPos, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler,
            Object destination, ItemStack dispensedStack, ItemStack remainder) {
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            // after setInventorySlotContents if return false
            IMixinInventory capture = forCapture(dropper);
            Inventory source = toInventory(dropper);
            Inventory destInv = toInventory(destination, itemHandler);
            SpongeCommonEventFactory.captureTransaction(capture, source, slot, stack);
            SpongeCommonEventFactory.callTransferPost(capture, source, destInv);
        }
    }

    // Utility
    private static Inventory toInventory(Object te) {
        return toInventory(te, null);
    }

    private static Inventory toInventory(Object te, IItemHandler itemHandler) {
        if (te instanceof TileEntityChest) {
            te = InventoryUtil.getDoubleChestInventory(((TileEntityChest) te)).orElse(((Inventory) te));
        }
        if (te instanceof Inventory) {
            return ((Inventory) te);
        }
        if (itemHandler instanceof Inventory) {
            return ((Inventory) itemHandler);
        }
        // TODO handle IItemHandlers that were not mixed into
        return new EmptyInventoryImpl(null);
    }

    private static IMixinInventory forCapture(Object toCapture) {
        if (toCapture instanceof IMixinInventory) {
            return ((IMixinInventory) toCapture);
        }
        return null;
    }

}
