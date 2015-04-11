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
package org.spongepowered.mod.mixin.core.entity.hanging;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.mod.registry.SpongeGameRegistry;

@NonnullByDefault
@Mixin(net.minecraft.entity.EntityHanging.class)
public abstract class MixinEntityHanging extends Entity implements Hanging {

    private MixinEntityHanging super$;

    @Shadow
    public EnumFacing facingDirection;

    @Shadow
    private int tickCounter1;

    @Shadow
    public abstract boolean onValidSurface();

    @Shadow
    public abstract void onBroken(Entity entity);

    public MixinEntityHanging(World worldIn) {
        super(worldIn);
    }

    private boolean ignorePhysics = false;

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    @Overwrite
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.tickCounter1++ == 100 && !this.worldObj.isRemote) {
            this.tickCounter1 = 0;

            if (!this.isDead && !this.onValidSurface() && !this.ignorePhysics) {
                this.setDead();
                this.onBroken((Entity) null);
            }
        }
    }

    public Direction getHangingDirection() {
        return SpongeGameRegistry.directionMap.inverse().get(this.facingDirection);
    }

    public void setHangingDirection(Direction direction, boolean forced) {
        this.ignorePhysics = forced;
        this.facingDirection =
                SpongeGameRegistry.directionMap.get(direction) == null ? EnumFacing.NORTH : SpongeGameRegistry.directionMap.get(direction);
    }

    @SoftOverride
    public void writeToNbt(NBTTagCompound compound) {
        this.super$.writeToNbt(compound);
        compound.setBoolean("ignorePhysics", this.ignorePhysics);
    }

    @SoftOverride
    public void readFromNbt(NBTTagCompound compound) {
        this.super$.readFromNbt(compound);
        if (compound.hasKey("ignorePhysics")) {
            this.ignorePhysics = compound.getBoolean("ignorePhysics");
        }
    }

}
