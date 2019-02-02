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
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
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
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = VanillaInventoryCodeHooks.class)
public abstract class MixinVanillaInventoryCodeHooks {

    @Shadow(remap = false) private static ItemStack insertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow(remap = false) @Nullable private static Pair<IItemHandler, Object> getItemHandler(IHopper hopper, EnumFacing side) {
        throw new AbstractMethodError("Shadow");
    }

    // Call Pre Events

    @Inject(method = "insertHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;getSizeInventory()I"))
    private static void onTransferItemsOut(TileEntityHopper hopper, CallbackInfoReturnable<Boolean> cir, EnumFacing hopperFacing,
            Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler, Object destination) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            if (SpongeCommonEventFactory.callTransferPre(InventoryUtil.toInventory(hopper, null), InventoryUtil.toInventory(destination, itemHandler)).isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(remap = false, method = "extractHook", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getKey()Ljava/lang/Object;"))
    private static void onExtractHook(IHopper hopper, CallbackInfoReturnable<Boolean> cir, Pair<IItemHandler, Object> itemHandlerResult) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            IItemHandler itemHandler = itemHandlerResult.getKey();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                // Find first item that can be extracted
                if (!itemHandler.extractItem(i, 1, true).isEmpty()) {
                    Inventory source = InventoryUtil.toInventory(itemHandlerResult.getValue(), itemHandler);
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
    private static void onDispense(World world, BlockPos pos, TileEntityDispenser dropper, int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir, EnumFacing enumFacing, BlockPos blockPos, Pair<IItemHandler, Object> destinationResult,
            IItemHandler itemHandler, Object destination) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            if (SpongeCommonEventFactory.callTransferPre(InventoryUtil.toInventory(dropper, null), InventoryUtil.toInventory(destination, itemHandler)).isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    // Capture Push item Slot

    @Redirect(remap = false, method = "putStackInInventoryAllSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/VanillaInventoryCodeHooks;insertStack(Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Object;"
                    + "Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onInsertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot) {
        ItemStack result = insertStack(source, destination, destInventory, stack, slot);
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            if (result.isEmpty()) {
                Inventory inv = InventoryUtil.toInventory(destination, destInventory);
                Optional<org.spongepowered.api.item.inventory.Slot> sl = inv.getSlot(SlotIndex.of(slot));
                if (sl.isPresent()) {
                    // We don't actually use this transaction we just have to save the modified Slot somewhere
                    InventoryUtil.forCapture(source).getCapturedTransactions().add(new SlotTransaction(sl.get(), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE));
                }
            }
        }
        return result;
    }

    // Call Post Transfer Events

    @Inject(remap = false, method = "insertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
    private static void onPushItem(TileEntityHopper hopper, CallbackInfoReturnable<Boolean> cir,
            EnumFacing hopperFacing, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler, Object destination, int i,
            ItemStack originalSlotContents, ItemStack insertStack, ItemStack remainder) {
        if (remainder.isEmpty()) {
            if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
                List<SlotTransaction> list = InventoryUtil.forCapture(hopper).getCapturedTransactions();
                if (!list.isEmpty()) {
                    Slot dSlot = list.get(0).getSlot();
                    list.clear();
                    Inventory sInv = InventoryUtil.toInventory(hopper, null);
                    Optional<Slot> sSlot = sInv.getSlot(SlotIndex.of(i));
                    if (sSlot.isPresent()) {
                        Inventory dInv = InventoryUtil.toInventory(destination, itemHandler);
                        SpongeCommonEventFactory.callTransferPost(sInv, dInv, sSlot.get(), dSlot, insertStack);
                    }
                }
            }
        }
    }

    @Inject(remap = false, method = "extractHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/IHopper;markDirty()V"))
    private static void afterPullItem(IHopper dest, CallbackInfoReturnable<Boolean> cir,
            Pair<IItemHandler, Object> itemHandlerResult, IItemHandler handler, int i, ItemStack extractItem, int j, ItemStack destStack) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            Inventory sInv = InventoryUtil.toInventory(itemHandlerResult.getValue(), handler);
            Inventory dInv = InventoryUtil.toInventory(dest, null);
            Optional<Slot> sSlot = sInv.getSlot(SlotIndex.of(i));
            Optional<Slot> dSlot = dInv.getSlot(SlotIndex.of(j));
            if (sSlot.isPresent() && dSlot.isPresent()) {
                SpongeCommonEventFactory.callTransferPost(sInv, dInv, sSlot.get(), dSlot.get(), extractItem);
            }
        }
    }

    @Inject(remap = false, method = "dropperInsertHook", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "RETURN", ordinal = 1))
    private static void afterPushItemDropper(World world, BlockPos pos, TileEntityDispenser dropper, int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir,
            EnumFacing enumFacing, BlockPos blockPos, Pair<IItemHandler, Object> destinationResult, IItemHandler itemHandler,
            Object destination, ItemStack dispensedStack, ItemStack remainder) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            IMixinInventory capture = InventoryUtil.forCapture(dropper);

            List<SlotTransaction> list = capture.getCapturedTransactions();
            if (!list.isEmpty()) {
                Slot dSlot = list.get(0).getSlot();
                list.clear();
                Inventory sInv = InventoryUtil.toInventory(dropper, null);

                Optional<Slot> sSlot = sInv.getSlot(SlotIndex.of(slot));
                if (sSlot.isPresent()) {
                    Inventory dInv = InventoryUtil.toInventory(destination, itemHandler);
                    SpongeCommonEventFactory.callTransferPost(sInv, dInv, sSlot.get(), dSlot, dispensedStack);
                }
            }
        }
    }

}
