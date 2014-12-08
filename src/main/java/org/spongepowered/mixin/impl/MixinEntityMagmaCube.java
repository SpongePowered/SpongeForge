package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.MagmaCube;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityMagmaCube.class)
@Implements(@Interface(iface = MagmaCube.class, prefix = "magma$"))
public abstract class MixinEntityMagmaCube extends EntitySlime {

    public MixinEntityMagmaCube(World worldIn) {
        super(worldIn);
    }

}
