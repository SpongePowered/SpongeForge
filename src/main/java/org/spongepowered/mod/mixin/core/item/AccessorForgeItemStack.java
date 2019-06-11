package org.spongepowered.mod.mixin.core.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public interface AccessorForgeItemStack {

    @Nullable
    @Accessor(value = "capabilities", remap = false)
    CapabilityDispatcher accessor$getCapabilities();

}
