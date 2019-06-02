package org.spongepowered.mod.mixin.core.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;

@Mixin(TileEntity.class)
@Implements(@Interface(iface = IMixinTileEntity.class, prefix = "spongeIMixinTile$"))
public abstract class MixinTileEntity_Forge {

    @Shadow private NBTTagCompound customTileData;

    @Intrinsic
    public boolean spongeIMixinTile$hasTileDataCompound() {
        return this.customTileData != null;
    }

}
