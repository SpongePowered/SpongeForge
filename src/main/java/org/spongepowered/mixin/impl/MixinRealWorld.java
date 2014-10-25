package org.spongepowered.mixin.impl;

import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.Block;
import org.spongepowered.api.world.World;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;
import org.spongepowered.wrapper.BlockWrapper;

@Mixin(net.minecraft.world.World.class)
public abstract class MixinRealWorld implements World {
    @Shadow protected WorldInfo worldInfo;

    //@Shadow(prefix = "shadow$") public abstract net.minecraft.block.Block shadow$getBlock(int x, int y, int z);

    @Override
    public String getName() {
        return worldInfo.getWorldName();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return new BlockWrapper(this, x, y, z);
    }
}
