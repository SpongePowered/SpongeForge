package org.spongepowered.mixin.impl;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityMob.class)
@Implements(@Interface(iface = Monster.class, prefix = "monster$"))
public abstract class MixinEntityMob extends EntityCreature {

    public MixinEntityMob(World worldIn) {
        super(worldIn);
    }

}
