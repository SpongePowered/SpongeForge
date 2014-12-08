package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Giant;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityGiantZombie.class)
@Implements(@Interface(iface = Giant.class, prefix = "giant$"))
public abstract class MixinEntityGiantZombie extends EntityMob {

    public MixinEntityGiantZombie(World worldIn) {
        super(worldIn);
    }

}
