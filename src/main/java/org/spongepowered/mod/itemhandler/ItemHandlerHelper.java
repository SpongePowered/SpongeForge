package org.spongepowered.mod.itemhandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nonnull;

/**
 * All method calls to {@link IItemHandler} or {@link IItemHandlerModifiable}
 * will be delegated through this {@link ItemHandlerHelper}.
 */
public final class ItemHandlerHelper {

    // DO NOT MODIFY THE SIGNATURES OF THE FOLLOWING METHODS!
    ///////////////////////// START /////////////////////////

    @ItemHandlerMethod
    public static int getSlots(IItemHandler itemHandler) {
        SpongeImpl.getLogger().info("BEFORE: getSlots");
        // Do things
        try {
            return itemHandler.getSlots();
        } finally {
            SpongeImpl.getLogger().info("AFTER: getSlots");
        }
    }

    @ItemHandlerMethod
    public static ItemStack getStackInSlot(IItemHandler itemHandler, int slot) {
        SpongeImpl.getLogger().info("BEFORE: getStackInSlot");
        // Do things
        try {
            return itemHandler.getStackInSlot(slot);
        } finally {
            SpongeImpl.getLogger().info("AFTER: getStackInSlot");
        }
    }

    @ItemHandlerMethod
    public static ItemStack insertItem(IItemHandler itemHandler, int slot, @Nonnull ItemStack stack, boolean simulate) {
        SpongeImpl.getLogger().info("BEFORE: insertItem");
        // Do things
        try {
            return itemHandler.insertItem(slot, stack, simulate);
        } finally {
            SpongeImpl.getLogger().info("AFTER: insertItem");
        }
    }

    @ItemHandlerMethod
    public static ItemStack extractItem(IItemHandler itemHandler, int slot, int amount, boolean simulate) {
        SpongeImpl.getLogger().info("BEFORE: extractItem");
        // Do things
        try {
            return itemHandler.extractItem(slot, amount, simulate);
        } finally {
            SpongeImpl.getLogger().info("AFTER: extractItem");
        }
    }

    @ItemHandlerMethod
    public static int getSlotLimit(IItemHandler itemHandler, int slot) {
        SpongeImpl.getLogger().info("BEFORE: setStackInSlot");
        // Do things
        try {
            return itemHandler.getSlotLimit(slot);
        } finally {
            SpongeImpl.getLogger().info("AFTER: setStackInSlot");
        }
    }

    @ItemHandlerMethod
    public static void setStackInSlot(IItemHandlerModifiable itemHandler, int slot, @Nonnull ItemStack stack) {
        SpongeImpl.getLogger().info("BEFORE: setStackInSlot");
        // Do things
        itemHandler.setStackInSlot(slot, stack);
        SpongeImpl.getLogger().info("AFTER: setStackInSlot");
    }

    ////////////////////////// END //////////////////////////
    // Put whatever you like under here.

    private ItemHandlerHelper() {
    }
}
