package org.spongepowered.mixin.impl;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Ghast;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityGhast.class)
@Implements(@Interface(iface = Ghast.class, prefix = "ghast$"))
public abstract class MixinEntityGhast extends EntityFlying {

    public MixinEntityGhast(World worldIn) {
        super(worldIn);
    }

}
