package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;


@NonnullByDefault
@Mixin(EntityCreeper.class)
@Implements(@Interface(iface = Creeper.class, prefix = "creeper$"))
public abstract class MixinEntityCreeper extends EntityMob {

    @Shadow
    private int explosionRadius;

    @Shadow
    public abstract boolean getPowered();

    public MixinEntityCreeper(World worldIn) {
        super(worldIn);
    }

    public boolean creeper$isPowered() {
        return getPowered();
    }

    public void creeper$setPowered(boolean powered) {
        if (powered) {
            this.dataWatcher.updateObject(17, Byte.valueOf((byte) 1));
        } else {
            this.dataWatcher.updateObject(17, Byte.valueOf((byte) 0));
        }
    }

    public int creeper$getExplosionRadius() {
        return this.explosionRadius;
    }

    public void creeper$setExplosionRadius(int radius) {
        this.explosionRadius = radius;
    }
}
