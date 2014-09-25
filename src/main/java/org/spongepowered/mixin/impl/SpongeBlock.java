package org.spongepowered.mixin.impl;

import org.spongepowered.api.block.Block;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

/**
 * Created by thomas on 25/09/14.
 */
@Mixin(net.minecraft.block.Block.class)
public abstract class SpongeBlock implements Block {

    @Shadow abstract String getUnlocalizedName();

    @Override
    public String getID() {
        return getUnlocalizedName();
    }
}
