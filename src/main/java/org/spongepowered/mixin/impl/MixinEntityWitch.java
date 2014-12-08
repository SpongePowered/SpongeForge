package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Witch;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityWitch.class)
@Implements(@Interface(iface = Witch.class, prefix = "witch$"))
public abstract class MixinEntityWitch extends EntityMob {

    public MixinEntityWitch(World worldIn) {
        super(worldIn);
    }

    public boolean witch$isAggressive() {
        return this.getDataWatcher().getWatchableObjectByte(21) == 1;
    }

    public void witch$setAggressive(boolean aggressive) {
        this.getDataWatcher().updateObject(21, Byte.valueOf((byte)(aggressive ? 1 : 0)));
    }

}
