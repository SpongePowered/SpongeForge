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
    public int experienceLevel;

    @Shadow
    public int experienceTotal;

    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract int xpBarCap();

    @Shadow
    public abstract FoodStats getFoodStats();

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    public double human$getExhaustion() {
        return this.getFoodStats().foodExhaustionLevel;
    }

    public void human$setExhaustion(double exhaustion) {
        this.getFoodStats().foodExhaustionLevel = (float) exhaustion;
    }

    public double human$getSaturation() {
        return this.getFoodStats().getSaturationLevel();
    }

    public void human$setSaturation(double saturation) {
        this.getFoodStats().setFoodSaturationLevel((float) saturation);
    }

    public double human$getFoodLevel() {
        return this.getFoodStats().getFoodLevel();
    }

    public void human$setFoodLevel(double hunger) {
        this.getFoodStats().setFoodLevel((int) hunger);
    }

    // utility method for getting the total experience at an arbitrary level
    // the formulas here are basically (slightly modified) integrals of those of EntityPlayer#xpBarCap()
    private int xpAtLevel(int level) {
        if (level > 30) {
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
        else if (level > 15) {
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        }
        else {
            return (int) (Math.pow(level, 2) + 6 * level);
        }
    }

    public int human$getExperienceSinceLevel() {
        return this.human$getTotalExperience() - xpAtLevel(this.human$getLevel());
    }

    public void human$setExperienceSinceLevel(int experience) {
        this.human$setTotalExperience(xpAtLevel(this.experienceLevel) + experience);
    }

    public int human$getExperienceBetweenLevels() {
        return this.xpBarCap();
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

    public boolean human$isViewingInventory() {
        return this.openContainer != null;
    }

}
