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
package org.spongepowered.mod.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.mod.util.WrappedArrayList;

import java.util.ArrayList;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Entity.class, priority = 1001)
public abstract class EntityMixin_Forge implements EntityBridge, DataCompoundHolder {

    @Shadow public net.minecraft.world.World world;
    @Shadow public boolean isDead;
    @Shadow public int dimension;
    @Shadow(remap = false) @Nullable private NBTTagCompound customEntityData;
    @Shadow(remap = false) public ArrayList<EntityItem> capturedDrops = new WrappedArrayList((Entity) (Object) this, new ArrayList<>());

    @Shadow protected abstract void setSize(float width, float height);
    @Shadow @Nullable public abstract MinecraftServer getServer();
    @Shadow(remap = false) public abstract NBTTagCompound getEntityData();

    @Override
    public boolean data$hasRootCompound() {
        return this.customEntityData != null;
    }

    @Override
    public NBTTagCompound data$getRootCompound() {
        return getEntityData();
    }

    /**
     * @author Zidane - June 2019 - 1.12.2
     * @reason Re-route dimension changes to common hook
     */
    @Nullable
    @Overwrite(remap = false)
    public net.minecraft.entity.Entity changeDimension(int toDimensionId, ITeleporter teleporter) {
        if (!this.world.isRemote && !this.isDead) {

            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension((Entity) (Object) this, toDimensionId)) {
                return (Entity) (Object) this;
            }

            // Sponge Start - Remove the rest of the method and call our common hook.
            final WorldServer world = this.getServer().getWorld(toDimensionId);
            return EntityUtil.transferEntityToWorld((Entity) (Object) this, null, world, (ForgeITeleporterBridge) teleporter, true);
            // Sponge End
        }
        return null;
    }

    /**
     * @author gabizou - May 8th, 2018
     * @reason this re-assigns the capture list at the end of a tick if this entity was considered as "per-entity" captures.
     * This avoids leaking the PhaseContext for the entity and the potentila leakage of world objects, etc.
     */
    @Override
    public void bridge$clearWrappedCaptureList() {
        this.capturedDrops = new ArrayList<>();

    }
}
