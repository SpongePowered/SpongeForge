package org.spongepowered.mixin.impl;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.monster.Enderman;
import org.spongepowered.api.item.ItemBlock;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

import com.google.common.base.Optional;

@NonnullByDefault
@Mixin(EntityEnderman.class)
@Implements(@Interface(iface = Enderman.class, prefix = "enderman$"))
public abstract class MixinEntityEnderman extends EntityMob {

    public MixinEntityEnderman(World worldIn) {
        super(worldIn);
    }

    public Optional<BlockState> enderman$getCarriedBlock() {
        return Optional.fromNullable((BlockState)Block.getStateById(this.dataWatcher.getWatchableObjectShort(16) & 65535));
    }

    public void enderman$setCarriedBlock(BlockState carriedBlock) {
        this.dataWatcher.updateObject(16, Short.valueOf((short)(Block.getStateId(((IBlockState)carriedBlock).getBlock().getDefaultState()) & 65535)));
    }

    public boolean enderman$isScreaming() {
        return this.dataWatcher.getWatchableObjectByte(18) > 0;
    }

    public void enderman$setScreaming(boolean screaming) {
        if (screaming) {
            this.dataWatcher.updateObject(18, Byte.valueOf((byte) 1));
        } else {
            this.dataWatcher.updateObject(18, Byte.valueOf((byte) 0));
        }
    }
}
