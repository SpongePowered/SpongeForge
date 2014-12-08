package org.spongepowered.mixin.impl;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Guardian;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityGuardian.class)
@Implements(@Interface(iface = Guardian.class, prefix = "guardian$"))
public abstract class MixinEntityGuardian extends EntityMob {

    public MixinEntityGuardian(World worldIn) {
        super(worldIn);
    }

    public boolean guardian$isElder() {
        return (this.dataWatcher.getWatchableObjectInt(16) & 4) != 0;
    }

    public void guardian$setElder(boolean elder) {
        int j = this.dataWatcher.getWatchableObjectInt(16);

        if (elder) {
            this.dataWatcher.updateObject(16, Integer.valueOf(j | 4));
        } else {
            this.dataWatcher.updateObject(16, Integer.valueOf(j & ~4));
        }
    }
}
