package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Endermite;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityEndermite.class)
@Implements(@Interface(iface = Endermite.class, prefix = "endermite$"))
public abstract class MixinEntityEndermite extends EntityMob {

    @Shadow
    private boolean playerSpawned;

    public MixinEntityEndermite(World worldIn) {
        super(worldIn);
    }

    public boolean endermite$isPlayerCreated() {
        return this.playerSpawned;
    }

    public void endermite$setPlayerCreated(boolean playerCreated) {
       this.playerSpawned = playerCreated;
    }
}
