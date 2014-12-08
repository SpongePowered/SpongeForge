package org.spongepowered.mixin.impl;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.meta.SkeletonType;
import org.spongepowered.api.entity.living.meta.SkeletonTypes;
import org.spongepowered.api.entity.living.monster.Skeleton;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.entity.SpongeSkeletonType;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(EntitySkeleton.class)
@Implements(@Interface(iface = Skeleton.class, prefix = "skeleton$"))
public abstract class MixinEntitySkeleton extends EntityMob {

    public MixinEntitySkeleton(World worldIn) {
        super(worldIn);
    }

    public SkeletonType skeleton$getSkeletonType() {
        int type = this.dataWatcher.getWatchableObjectByte(13);
        if (type == 0) {
            return SkeletonTypes.NORMAL;
        } else {
            return SkeletonTypes.WITHER;
        }
    }

    public void skeleton$setSkeletonType(SkeletonType skeletonType) {
        int type = ((SpongeSkeletonType)skeletonType).type;
        this.dataWatcher.updateObject(13, Byte.valueOf((byte)type));
        this.isImmuneToFire = type == 1;

        if (type == 1) {
            this.setSize(0.72F, 2.535F);
        } else {
            this.setSize(0.6F, 1.95F);
        }
    }

}
