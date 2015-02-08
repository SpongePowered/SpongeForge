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
package org.spongepowered.mod.mixin.entity.hanging;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityHanging;
import net.minecraft.world.World;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.entity.item.EntityItemFrame.class)
public abstract class MixinEntityItemFrame extends EntityHanging implements ItemFrame {

    @Shadow
    public abstract net.minecraft.item.ItemStack getDisplayedItem();

    @Shadow
    public abstract void setDisplayedItem(net.minecraft.item.ItemStack p_82334_1_);

    @Shadow(prefix = "shadow$")
    public abstract int shadow$getRotation();

    @Shadow
    public abstract void setItemRotation(int p_82336_1_);

    public MixinEntityItemFrame(World worldIn) {
        super(worldIn);
    }

    @Override
    public Optional<ItemStack> getItem() {
        return Optional.fromNullable((ItemStack) getDisplayedItem());
    }

    @Override
    public void setItem(@Nullable ItemStack item) {
        setDisplayedItem((net.minecraft.item.ItemStack) item);
    }

    @Override
    public Rotation getItemRotation() {
        return SpongeMod.instance.getGame().getRegistry().getRotationFromDegree(shadow$getRotation() * 45).get();
    }

    @Override
    public void setRotation(Rotation itemRotation) {
        setItemRotation(itemRotation.getAngle() / 45);
    }
}
