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
package org.spongepowered.mod.mixin.core.block.data;

import com.google.common.base.Optional;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.api.block.data.Furnace;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.data.FurnaceSmeltItemEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.mod.SpongeMod;

@NonnullByDefault
@Implements(@Interface(iface = Furnace.class, prefix = "furnace$"))
@Mixin(net.minecraft.tileentity.TileEntityFurnace.class)
public abstract class MixinTileEntityFurnace extends TileEntityLockable implements IUpdatePlayerListBox, ISidedInventory {

    @Override
    @Shadow
    public abstract int getField(int id);

    @Override
    @Shadow
    public abstract void setField(int id, int value);

    @Shadow
    public abstract boolean canSmelt();

    @Shadow
    private ItemStack[] furnaceItemStacks;

    @Inject(method = "update()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityFurnace;isBurning()Z", ordinal = 5, shift = At.Shift.BY, by = 2))
    private void onOnUpdate(CallbackInfo ci) {
        ItemStack burned = this.furnaceItemStacks[1];
        ItemStack remaining = burned.copy();
        if (--remaining.stackSize == 0) {
            remaining = burned.getItem().getContainerItem(furnaceItemStacks[1]);
        }
        SpongeMod.instance.getGame().getEventManager().post(
                SpongeEventFactory.createFurnaceConsumeFuelEvent(SpongeMod.instance.getGame(), (Furnace) this,
                                                                 (org.spongepowered.api.item.inventory.ItemStack) burned,
                                                                 (org.spongepowered.api.item.inventory.ItemStack) remaining));
    }

    @Overwrite
    private void smeltItem() {
        if (this.canSmelt()) {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
            ItemStack result = null;
            ItemStack fuelSlot = null;

            if (this.furnaceItemStacks[2] == null)  {
                result = itemstack.copy();
            } else if (this.furnaceItemStacks[2].getItem() == itemstack.getItem()) {
                result = this.furnaceItemStacks[2].copy();
                result.stackSize += itemstack.stackSize;
            }

            if (this.furnaceItemStacks[0].getItem() == Item.getItemFromBlock(Blocks.sponge) && this.furnaceItemStacks[0].getMetadata() == 1 && this.furnaceItemStacks[1] != null && this.furnaceItemStacks[1].getItem() == Items.bucket) {
                fuelSlot = new ItemStack(Items.water_bucket);
            }

            FurnaceSmeltItemEvent event = SpongeEventFactory.createFurnaceSmeltItemEvent(SpongeMod.instance.getGame(), (Furnace) this,
                                                                                         (org.spongepowered.api.item.inventory.ItemStack) result,
                                                                                         (org.spongepowered.api.item.inventory.ItemStack) this.furnaceItemStacks[0]);
            if (!SpongeMod.instance.getGame().getEventManager().post(event)) {
                this.furnaceItemStacks[2] = (ItemStack) event.getCookedItem();
                if (fuelSlot != null) {
                    this.furnaceItemStacks[1] = fuelSlot;
                }

                --this.furnaceItemStacks[0].stackSize;

                if (this.furnaceItemStacks[0].stackSize <= 0)
                {
                    this.furnaceItemStacks[0] = null;
                }
            }
        }

    }

    public int furnace$getRemainingBurnTime() {
        return getField(0);
    }

    public void furnace$setRemainingBurnTime(int time) {
        setField(0, time);
    }

    public int furnace$getRemainingCookTime() {
        return getField(3) - getField(2);
    }

    public void furnace$setRemainingCookTime(int time) {
        setField(2, getField(3) - time);
    }

}
