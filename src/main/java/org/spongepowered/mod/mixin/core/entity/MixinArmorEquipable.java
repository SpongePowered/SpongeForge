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
package org.spongepowered.mod.mixin.core.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

// All implementors of ArmorEquipable
@Mixin({EntityArmorStand.class, EntityGiantZombie.class, EntitySkeleton.class, EntityPlayerMP.class, EntityZombie.class})
public abstract class MixinArmorEquipable extends EntityLivingBase implements ArmorEquipable {

    public MixinArmorEquipable(World worldIn) {
        super(worldIn);
    }

    private static final int SLOT_HAND = 0;
    private static final int SLOT_BOOTS = 1;
    private static final int SLOT_LEGGINGS = 2;
    private static final int SLOT_CHESTPLATE = 3;
    private static final int SLOT_HELMET = 4;

    @Override
    public Optional<ItemStack> getHelmet() {
        return Optional.fromNullable((ItemStack) this.getEquipmentInSlot(SLOT_HELMET));
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        this.setCurrentItemOrArmor(SLOT_HELMET, (net.minecraft.item.ItemStack) helmet);
    }

    @Override
    public Optional<ItemStack> getChestplate() {
        return Optional.fromNullable((ItemStack) this.getEquipmentInSlot(SLOT_CHESTPLATE));
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        this.setCurrentItemOrArmor(SLOT_CHESTPLATE, (net.minecraft.item.ItemStack) chestplate);
    }

    @Override
    public Optional<ItemStack> getLeggings() {
        return Optional.fromNullable((ItemStack) this.getEquipmentInSlot(SLOT_LEGGINGS));
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        this.setCurrentItemOrArmor(SLOT_LEGGINGS, (net.minecraft.item.ItemStack) leggings);
    }

    @Override
    public Optional<ItemStack> getBoots() {
        return Optional.fromNullable((ItemStack) this.getEquipmentInSlot(SLOT_BOOTS));
    }

    @Override
    public void setBoots(ItemStack boots) {
        this.setCurrentItemOrArmor(SLOT_BOOTS, (net.minecraft.item.ItemStack) boots);
    }

    @Override
    public Optional<ItemStack> getItemInHand() {
        return Optional.fromNullable((ItemStack) this.getEquipmentInSlot(SLOT_HAND));
    }

    @Override
    public void setItemInHand(ItemStack itemInHand) {
        this.setCurrentItemOrArmor(SLOT_HAND, (net.minecraft.item.ItemStack) itemInHand);
    }
}
