package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Zombie;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntityZombie.class)
@Implements(@Interface(iface = Zombie.class, prefix = "zombie$"))
public abstract class MixinEntityZombie extends EntityMob {

    public MixinEntityZombie(World worldIn) {
        super(worldIn);
    }

   public boolean zombie$isVillagerZombie() {
       return this.getDataWatcher().getWatchableObjectByte(13) == 1;
   }

   public void zombie$setVillagerZombie(boolean villagerZombie) {
       this.getDataWatcher().updateObject(13, Byte.valueOf((byte)(villagerZombie ? 1 : 0)));
   }
}
