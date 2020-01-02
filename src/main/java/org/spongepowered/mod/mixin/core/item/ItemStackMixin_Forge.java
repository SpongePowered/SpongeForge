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

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.mod.bridge.item.ItemStackBridge_Forge;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class ItemStackMixin_Forge implements ItemStackBridge_Forge {

    @Shadow(remap = false) private CapabilityDispatcher capabilities;

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @reason Since we handle placement logic already in common, we override
     * the {@code world.isRemote} to return {@code true} so that forge's hook
     * is never called on. This will allow us to essentially keep/retain vanilla
     * logic.
     *
     * @param world The world being used
     * @return True, always true, we handle the logic in our mixins in common
     */
    @Redirect(
        method = "onItemUse",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;isRemote:Z",
            opcode = Opcodes.GETFIELD
        ),
        slice = @Slice(
            from = @At("HEAD"),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraftforge/common/ForgeHooks;onPlaceItemIntoWorld(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFFLnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;",
                remap = false
            )
        )
    )
    private boolean forgeImpl$ReturnTrueForWorldSoForgeEventIsIgnored(World world) {
        return true;
    }

    @Override
    public CapabilityDispatcher forgeBridge$getCapabilities() {
        return this.capabilities;
    }
}
