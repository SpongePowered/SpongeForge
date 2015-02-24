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

import com.google.common.collect.Lists;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.data.Banner.PatternLayer;
import org.spongepowered.api.block.meta.BannerPatternShape;
import org.spongepowered.api.entity.living.animal.DyeColor;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.block.meta.SpongePatternLayer;

import java.util.List;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntityBanner.class)
@Implements(@Interface(iface = org.spongepowered.api.block.data.Banner.class, prefix = "banner$"))
public abstract class MixinTileEntityBanner extends TileEntity {

    @Shadow
    private int baseColor;

    @Shadow
    private NBTTagList field_175118_f;

    private List<PatternLayer> patterns = Lists.newArrayList();

    @Inject(method = "setItemValues(Lnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void onSetItemValues(ItemStack stack, CallbackInfo ci) {
        updatePatterns();
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void onReadFromNBT(NBTTagCompound tagCompound, CallbackInfo ci) {
        updatePatterns();
    }

    public void markDirtyAndUpdate() {
        super.markDirty();
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.getPos());
        }
    }

    private void updatePatterns() {
        this.patterns.clear();
        if (this.field_175118_f != null) {
            GameRegistry registry = SpongeMod.instance.getGame().getRegistry();
            for (int i = 0; i < this.field_175118_f.tagCount(); i++) {
                NBTTagCompound tagCompound = this.field_175118_f.getCompoundTagAt(i);
                this.patterns.add(new SpongePatternLayer(registry.getBannerPatternShapeById(tagCompound.getString("Pattern")).get(),
                        registry.getDye(EnumDyeColor.func_176766_a(tagCompound.getInteger("Color")).getName()).get()));
            }
        }
        this.markDirtyAndUpdate();
    }

    public DyeColor banner$getBaseColor() {
        return DyeColor.class.cast(EnumDyeColor.func_176766_a(this.baseColor));
    }

    public void banner$setBaseColor(DyeColor color) {
        this.baseColor = EnumDyeColor.valueOf(color.getName().toUpperCase()).getDyeColorDamage();
        this.markDirtyAndUpdate();
    }

    public List<PatternLayer> banner$getPatternList() {
        return this.patterns;
    }

    public void banner$clearPattern() {
        for (int i = this.field_175118_f.tagCount() - 1; i >= 0; i--) {
            this.field_175118_f.removeTag(i);
        }
        this.patterns.clear();
        this.markDirtyAndUpdate();
    }

    public void banner$addPatternLayer(PatternLayer pattern) {
        banner$addPatternLayer(pattern.getId(), pattern.getColor());
    }

    public void banner$addPatternLayer(BannerPatternShape patternShape, DyeColor color) {
        NBTTagCompound nbtPattern = new NBTTagCompound();
        nbtPattern.setInteger("Color", EnumDyeColor.valueOf(color.getName().toUpperCase()).getDyeColorDamage());
        nbtPattern.setString("Pattern", patternShape.getId());
        this.field_175118_f.appendTag(nbtPattern);
        this.markDirtyAndUpdate();
    }

}
