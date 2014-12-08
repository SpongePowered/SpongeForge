package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Spider;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntitySpider.class)
@Implements(@Interface(iface = Spider.class, prefix = "spider$"))
public abstract class MixinEntitySpider extends EntityMob {

    public MixinEntitySpider(World worldIn) {
        super(worldIn);
    }

    public boolean spider$isClimbing() {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

}
