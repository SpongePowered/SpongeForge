package org.spongepowered.mixin.impl;

import org.spongepowered.api.block.Block;
import org.spongepowered.api.world.World;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Rename;
import org.spongepowered.mod.mixin.Shadow;

@Mixin(net.minecraft.world.World.class)
public abstract class SpongeWorld implements World {
    @Shadow
    @Rename("getBlock")
    public abstract net.minecraft.block.Block NMSgetBlock(int x, int y, int z);

    public Block getBlock(int x, int y, int z){
        return (Block) NMSgetBlock(x,y,z);
    }
}
