/*
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

package org.spongepowered.mod.mixin.core.entity.projectile;

import net.minecraft.entity.boss.EntityWither;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityBlaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.util.SpongeHooks;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;

@Mixin({EntityBlaze.class, EntityGhast.class, EntityPlayer.class, EntitySkeleton.class, EntityWitch.class, EntityWither.class})
public abstract class MixinEntityProjectileSource extends EntityLivingBase implements ProjectileSource {

    public MixinEntityProjectileSource() {
        super(null);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<T> projectileClass) {
        return this.launchProjectile(projectileClass, null);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        double x = this.posX;
        double y = this.getEntityBoundingBox().minY + (double) (this.height / 2.0F);
        double z = this.posZ;
        return SpongeHooks.launchProjectile(this.getEntityWorld(), new Vector3d(x, y, z), ((ProjectileSource) this), projectileClass, velocity);
    }
}
