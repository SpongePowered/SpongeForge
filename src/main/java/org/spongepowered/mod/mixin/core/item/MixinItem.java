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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.item.IMixinItem;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Item.class)
public abstract class MixinItem implements ItemType, IMixinItem {

    @Shadow(remap = false) public abstract boolean hasCustomEntity(ItemStack stack);
    @Shadow(remap = false) @Nullable public abstract Entity createEntity(World world, Entity location, ItemStack itemstack);

    @Inject(method = "registerItem(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/Item;)V", at = @At("RETURN"))
    private static void registerMinecraftItem(int id, ResourceLocation name, Item item, CallbackInfo ci) {
        final Item registered;
        final ResourceLocation nameForObject = Item.REGISTRY.getNameForObject(item);
        if (nameForObject == null) {
            registered = checkNotNull(Item.REGISTRY.getObject(name), "Someone replaced a vanilla item with a null item!!!");
        } else {
            registered = item;
        }
        ItemTypeRegistryModule.getInstance().registerAdditionalCatalog((ItemType) registered);
    }


    @Override
    public Optional<Entity> getCustomEntityItem(World world, Entity location, ItemStack itemstack) {
        if (this.hasCustomEntity(itemstack)) {
            return Optional.ofNullable(createEntity(world, location, itemstack));
        }
        return Optional.empty();
    }
}
