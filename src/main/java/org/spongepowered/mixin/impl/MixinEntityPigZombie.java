package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.ZombiePigman;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityPigZombie.class)
@Implements(@Interface(iface = ZombiePigman.class, prefix = "pigzombie$"))
public abstract class MixinEntityPigZombie extends EntityZombie {

    @Shadow
    private int angerLevel;

    public MixinEntityPigZombie(World worldIn) {
        super(worldIn);
    }

    public int pigzombie$getAngerLevel() {
        return this.angerLevel;
    }

    public void pigzombie$setAngerLevel(int angerLevel) {
        this.angerLevel = angerLevel;
    }

}
