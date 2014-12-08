package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.CaveSpider;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityCaveSpider.class)
@Implements(@Interface(iface = CaveSpider.class, prefix = "cavespider$"))
public abstract class MixinEntityCaveSpider extends EntitySpider {

    public MixinEntityCaveSpider(World worldIn) {
        super(worldIn);
    }

}
