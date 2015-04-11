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
package org.spongepowered.mod.entity.projectile;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.BlockProjectileSource;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.projectile.source.UnknownProjectileSource;
import org.spongepowered.mod.util.VecHelper;

import java.util.UUID;

public class ProjectileSourceSerializer {

    // TODO Revisit when persistent containers are implemented.
    // Note: ProjectileSource itself does not extend DataContainer

    public static NBTBase toNbt(ProjectileSource projectileSource) {
        if (projectileSource instanceof Entity) {
            return new NBTTagString(((Entity) projectileSource).getUniqueId().toString());
        }
        if (projectileSource instanceof BlockProjectileSource) {
            return new NBTTagLong(VecHelper.toBlockPos(((BlockProjectileSource) projectileSource).getBlock().getPosition()).toLong());
        }
        return null;
    }

    public static ProjectileSource fromNbt(World worldObj, NBTBase tag) {
        if (tag instanceof NBTTagString) {
            Entity entity =
                    ((org.spongepowered.api.world.World) worldObj).getEntity(UUID.fromString(((NBTTagString) tag).getString())).orNull();
            if (entity instanceof ProjectileSource) {
                return (ProjectileSource) entity;
            }
        }
        if (tag instanceof NBTTagLong) {
            TileEntity tileEntity = worldObj.getTileEntity(BlockPos.fromLong(((NBTTagLong) tag).getLong()));
            if (tileEntity instanceof ProjectileSource) {
                return (ProjectileSource) tileEntity;
            }
        }
        return new UnknownProjectileSource();
    }

    public static void writeSourceToNbt(NBTTagCompound compound, ProjectileSource projectileSource, net.minecraft.entity.Entity potentialEntity) {
        if (projectileSource == null && potentialEntity instanceof ProjectileSource) {
            projectileSource = (ProjectileSource) potentialEntity;
        }
        NBTBase projectileNbt = toNbt(projectileSource);
        if (projectileNbt != null) {
            compound.setTag("projectileSource", projectileNbt);
        }
    }

    public static void readSourceFromNbt(NBTTagCompound compound, Projectile projectile) {
        if (compound.hasKey("projectileSource")) {
            projectile.setShooter(fromNbt((World) projectile.getWorld(), compound.getTag("projectileSource")));
        }
    }
}
