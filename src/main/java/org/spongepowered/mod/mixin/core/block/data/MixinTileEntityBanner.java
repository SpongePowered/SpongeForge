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
package org.spongepowered.mod.mixin.core.block.data;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Lists;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.tile.TileEntityType;
import org.spongepowered.api.block.tile.TileEntityTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulators.tileentities.BannerData;
import org.spongepowered.api.data.types.BannerPatternShape;
import org.spongepowered.api.data.types.DyeColor;
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
@Implements(@Interface(iface = org.spongepowered.api.block.tile.Banner.class, prefix = "banner$"))
public abstract class MixinTileEntityBanner extends MixinTileEntity {

    @Shadow
    private int baseColor;

    @Shadow
    private NBTTagList patterns;

    private List<BannerData.PatternLayer> patternLayers = Lists.newArrayList();

    @Inject(method = "setItemValues(Lnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void onSetItemValues(ItemStack stack, CallbackInfo ci) {
        updatePatterns();
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        updatePatterns();
    }

    public void markDirtyAndUpdate() {
        this.markDirty();
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.getPos());
        }
    }

    private void updatePatterns() {
        this.patternLayers.clear();
        if (this.patterns != null) {
            GameRegistry registry = SpongeMod.instance.getGame().getRegistry();
            for (int i = 0; i < this.patterns.tagCount(); i++) {
                NBTTagCompound tagCompound = this.patterns.getCompoundTagAt(i);
                this.patternLayers.add(new SpongePatternLayer(registry.getType(BannerPatternShape.class, tagCompound.getString("Pattern")).get(),
                        registry.getType(DyeColor.class, EnumDyeColor.byDyeDamage(tagCompound.getInteger("Color")).getName()).get()));
            }
        }
        this.markDirtyAndUpdate();
    }

    @Override
    public TileEntityType getType() {
        return TileEntityTypes.BANNER;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(of("Patterns"), Lists.newArrayList(this.patternLayers));
        container.set(of("Base"), this.baseColor);
        return container;
    }

}
