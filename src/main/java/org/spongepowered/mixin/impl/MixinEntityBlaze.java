package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Blaze;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityBlaze.class)
@Implements(@Interface(iface = Blaze.class, prefix = "blaze$"))
public abstract class MixinEntityBlaze extends EntityMob {

    public MixinEntityBlaze(World worldIn) {
        super(worldIn);
    }

    @Shadow
    public abstract boolean isBurning();

    public boolean blaze$isOnFire() {
        return isBurning();
    }

    void blaze$setOnFire(boolean onFire) {
        if (onFire) {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte) 1));
        } else {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte) 0));
        }
    }
}
