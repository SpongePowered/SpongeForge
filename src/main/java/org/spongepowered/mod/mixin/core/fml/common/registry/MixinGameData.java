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
package org.spongepowered.mod.mixin.core.fml.common.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameData;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.registry.SpongeGameRegistry;

@Mixin(value = GameData.class, remap = false)
public abstract class MixinGameData {

    // Vanilla

    @Inject(method = "register", at = @At("HEAD"))
    public void onRegister(Object material, String id, int idHint, CallbackInfoReturnable<Integer> cir) {
        if (material instanceof net.minecraft.block.Block) {
            SpongeGameRegistry.blockTypeMappings.put(id, (BlockType) material);
        } else if (material instanceof net.minecraft.item.Item) {
            SpongeGameRegistry.itemTypeMappings.put(id, (ItemType) material);
        }
    }

    // Mods

    @Inject(method = "registerBlock(Lnet/minecraft/block/Block;Ljava/lang/String;I)I", at = @At(value = "HEAD"))
    private void onRegisterBlock(Block block, String name, int idHint, CallbackInfoReturnable<Integer> cir) {
        SpongeGameRegistry.blockTypeMappings.put(name, (BlockType) block);
    }

    @Inject(method = "registerItem(Lnet/minecraft/item/Item;Ljava/lang/String;I)I", at = @At(value = "HEAD"))
    private void onRegisterItem(Item item, String name, int idHint, CallbackInfoReturnable<Integer> cir) {
        SpongeGameRegistry.itemTypeMappings.put(name, (ItemType) item);
    }

}
