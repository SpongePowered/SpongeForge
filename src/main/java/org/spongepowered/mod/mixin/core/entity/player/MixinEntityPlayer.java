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
package org.spongepowered.mod.mixin.core.entity.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.Container;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityPlayer.class)
@Implements(@Interface(iface = Human.class, prefix = "human$"))
public abstract class MixinEntityPlayer extends EntityLivingBase {

    @Shadow
    public Container inventoryContainer;

    @Shadow
    public Container openContainer;

    @Shadow
    protected FoodStats foodStats;

    @Shadow
    public int experienceLevel;

    @Shadow
    public int experienceTotal;

	@Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract int xpBarCap();

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    public double human$getExhaustion() {
        return this.foodStats.foodExhaustionLevel;
    }

    public void human$setExhaustion(double exhaustion) {
        this.foodStats.foodExhaustionLevel = (float) exhaustion;
    }

    public double human$getSaturation() {
        return this.foodStats.getSaturationLevel();
    }

    public void human$setSaturation(double saturation) {
        this.foodStats.setFoodSaturationLevel((float) saturation);
    }

    public double human$getFoodLevel() {
        return this.foodStats.getFoodLevel();
    }

    public void human$setFoodLevel(double hunger) {
        this.foodStats.setFoodLevel((int)hunger);
    }

    public int human$getLevel() {
        return this.experienceLevel;
    }

    public void human$setLevel(int level) {
        this.experienceLevel = level;
    }

    public int human$getTotalExperience() {
        return this.experienceTotal;
    }

    public void human$setTotalExperience(int exp) {
        this.experienceTotal = exp;
    }

    public boolean human$isFlying() {
        return this.capabilities.isFlying;
    }

    public void human$setFlying(boolean flying) {
        this.capabilities.isFlying = flying;
    }

    public double human$getFlySpeed() {
        return this.capabilities.getFlySpeed();
    }

    public void human$setFlySpeed(double speed) {
        this.capabilities.setFlySpeed((float)speed);
    }

    public double human$getWalkSpeed() {
        return this.capabilities.getWalkSpeed();
    }

    public void human$setWalkSpeed(double speed) {
        this.capabilities.setPlayerWalkSpeed((float)speed);
    }

    public boolean human$isViewingInventory() {
        return this.openContainer == this.inventoryContainer;
    }

}
