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
package org.spongepowered.mod.mixin.core.entity.living.animal;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import org.spongepowered.api.data.types.HorseColor;
import org.spongepowered.api.data.types.HorseStyle;
import org.spongepowered.api.data.types.HorseVariant;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.entity.SpongeEntityConstants;
import org.spongepowered.mod.entity.SpongeEntityMeta;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityHorse.class)
@Implements(@Interface(iface = Horse.class, prefix = "horse$"))
public abstract class MixinEntityHorse extends MixinEntityAnimal {

    @Shadow private AnimalChest horseChest;
    @Shadow public abstract int getHorseVariant();
    @Shadow public abstract void setHorseVariant(int variant);
    @Shadow public abstract int getHorseType();
    @Shadow public abstract void setHorseType(int type);

    public HorseStyle getStyle() {
        return SpongeEntityConstants.HORSE_STYLE_IDMAP.get(getHorseVariant() & 0xFF);
    }

    public void setStyle(HorseStyle style) {
        setHorseVariant(((SpongeEntityMeta) getColor()).type & 0xFF | ((SpongeEntityMeta) style).type << 8);
    }

    public HorseColor getColor() {
        return SpongeEntityConstants.HORSE_COLOR_IDMAP.get(getHorseVariant() & 0xFF);
    }

    public void setColor(HorseColor color) {
        setHorseVariant(((SpongeEntityMeta) color).type & 0xFF | ((SpongeEntityMeta) getStyle()).type << 8);
    }

    public HorseVariant getVariant() {
        return SpongeEntityConstants.HORSE_VARIANT_IDMAP.get(getHorseType());
    }

    public void setVariant(HorseVariant variant) {
        setHorseType(((SpongeEntityMeta) variant).type);
    }

    public Optional<ItemStack> getSaddle() {
        return Optional.fromNullable((ItemStack) this.horseChest.getStackInSlot(0));
    }

    public void setSaddle(@Nullable ItemStack itemStack) {
        net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) itemStack;
        if (nmsStack != null && nmsStack.getItem() == Items.saddle) {
            this.horseChest.setInventorySlotContents(0, nmsStack);
        }
    }
}
