/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
