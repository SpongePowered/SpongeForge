/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.projectile.EyeOfEnder;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.projectile.source.UnknownProjectileSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.mod.mixin.core.entity.MixinEntity;

@NonnullByDefault
@Mixin(net.minecraft.entity.item.EntityEnderEye.class)
public abstract class MixinEntityEnderEye extends MixinEntity implements EyeOfEnder {

    @Shadow private double targetX;
    @Shadow private double targetY;
    @Shadow private double targetZ;
    @Shadow private boolean shatterOrDrop;

    private ProjectileSource projectileSource = new UnknownProjectileSource();

    public Vector3d getTargetedLocation() {
        return new Vector3d(this.targetX, this.targetY, this.targetZ);
    }

    public void setTargetedLocation(Vector3d vector3d) {
        this.targetX = vector3d.getX();
        this.targetY = vector3d.getY();
        this.targetZ = vector3d.getZ();
    }

    public boolean doesShatterOnDrop() {
        return !this.shatterOrDrop;
    }

    public void setShatterOnDrop(boolean shatterOnDrop) {
        this.shatterOrDrop = !shatterOnDrop;
    }

    @Override
    public ProjectileSource getShooter() {
        return this.projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        this.projectileSource = shooter;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, this);
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.projectileSource, null);
    }

}
