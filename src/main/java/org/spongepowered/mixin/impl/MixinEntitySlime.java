package org.spongepowered.mixin.impl;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Slime;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

@NonnullByDefault
@Mixin(EntitySlime.class)
@Implements(@Interface(iface = Slime.class, prefix = "slime$"))
public abstract class MixinEntitySlime extends EntityLiving {

    @Shadow
    protected abstract void setSlimeSize(int p_70799_1_);

    public MixinEntitySlime(World worldIn) {
        super(worldIn);
    }

    public int slime$getSize() {
        return this.dataWatcher.getWatchableObjectByte(16);
    }

    public void slime$setSize(int size) {
        setSlimeSize(size);
    }
}
