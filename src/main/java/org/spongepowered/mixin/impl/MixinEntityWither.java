package org.spongepowered.mixin.impl;

import java.util.List;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

@NonnullByDefault
@Mixin(EntityWither.class)
@Implements(@Interface(iface = Wither.class, prefix = "wither$"))
public abstract class MixinEntityWither extends EntityMob {

    public MixinEntityWither(World worldIn) {
        super(worldIn);
    }

    public int wither$getInvulnerableTicks() {
        return this.dataWatcher.getWatchableObjectInt(20);
    }

    public void wither$setInvulnerableTicks(int invulnerableTicks) {
        this.dataWatcher.updateObject(20, Integer.valueOf(invulnerableTicks));
    }

    public List<Living> wither$getTargets() {
        throw new UnsupportedOperationException();
    }
}
