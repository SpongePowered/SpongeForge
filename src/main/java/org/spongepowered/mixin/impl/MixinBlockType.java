package org.spongepowered.mixin.impl;

import net.minecraft.block.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

@Mixin(Block.class)
public abstract class MixinBlockType implements BlockType {
    @Shadow private String unlocalizedName;

    @Override
    public String getId() {
        return unlocalizedName;
    }
}
