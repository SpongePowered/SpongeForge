package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Silverfish;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntitySilverfish.class)
@Implements(@Interface(iface = Silverfish.class, prefix = "silverfish$"))
public abstract class MixinEntitySilverfish extends EntityMob {

    public MixinEntitySilverfish(World worldIn) {
        super(worldIn);
    }

}
