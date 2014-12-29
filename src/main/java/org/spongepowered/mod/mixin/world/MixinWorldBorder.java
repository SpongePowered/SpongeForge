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
package org.spongepowered.mod.mixin.world;

import java.util.List;
import java.util.Iterator;

import com.flowpowered.math.vector.Vector3d;

import net.minecraft.world.border.IBorderListener;

import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

@NonnullByDefault
@Mixin(net.minecraft.world.border.WorldBorder.class)
@Implements(@Interface(iface = WorldBorder.class, prefix = "border$"))
public abstract class MixinWorldBorder implements WorldBorder {

    @Shadow
    private int warningTime;

    @Shadow
    private int warningDistance;

    @Shadow
    public abstract double getDamageBuffer();

    @Shadow
    public abstract void setDamageBuffer(double buffer);

    @Shadow
    public abstract double func_177727_n(); // getDamageAmount

    @Shadow
    public abstract void func_177744_c(double amount); // setDamageAmount

    @Shadow
    public abstract int shadow$getWarningTime();

    @Shadow
    public abstract void shadow$setWarningTime(int time);

    @Shadow
    public abstract int shadow$getWarningDistance();

    @Shadow
    public abstract void shadow$setWarningDistance(int distance);

    @Shadow
    public abstract double getCenterX();

    @Shadow
    public abstract double getCenterZ();

    @Override
    @Shadow
    public abstract void setCenter(double x, double z);

    @Shadow
    public abstract double getTargetSize();

    @Shadow
    public abstract double getDiameter();

    @Shadow
    public abstract void setTransition(double newSize);

    @Shadow
    public abstract void setTransition(double oldSize, double newSize, long time);

    @Shadow
    public abstract long getTimeUntilTarget();

    @Shadow
    public abstract List getListeners();

    public int border$getWarningTime() {
        return this.getWarningTime();
    }

    @Override
    @Overwrite
    public int getWarningTime() {
        return this.warningTime;
    }


    public void border$setWarningTime(int time) {
        this.setWarningTime(time);
    }

    @Override
    @Overwrite
    public void setWarningTime(int time) {
        this.warningTime = time;
        Iterator var2 = this.getListeners().iterator();

        while (var2.hasNext()) {
            IBorderListener var3 = (IBorderListener)var2.next();
            var3.onWarningTimeChanged((net.minecraft.world.border.WorldBorder)((Object) this), this.warningTime);
        }
    }

    public int border$getWarningDistance() {
        return this.getWarningDistance();
    }

    @Override
    public int getWarningDistance() {
        return this.warningDistance;
    }

    public void border$setWarningDistance(int distance) {
        this.setWarningDistance(distance);
    }

    @Override
    public void setWarningDistance(int distance) {
        this.warningDistance = distance;
        Iterator var2 = this.getListeners().iterator();

        while (var2.hasNext())
        {
            IBorderListener var3 = (IBorderListener)var2.next();
            var3.onWarningDistanceChanged((net.minecraft.world.border.WorldBorder)((Object) this), this.warningDistance);
        }
    }

    public double border$getNewRadius() {
        return getTargetSize() / 2;
    }

    public double border$getRadius() {
        return getDiameter() / 2;
    }

    public void border$setRadius(double radius) {
        setTransition(radius * 2);
    }

    public void border$setRadius(double radius, long time) {
        setTransition(getRadius() * 2, radius * 2, time);
    }

    public long border$getTimeRemaining() {
        return getTimeUntilTarget();
    }

    public Vector3d border$getCenter() {
        return new Vector3d(getCenterX(), 0, getCenterZ());
    }

    public int border$getBlockBuffer() {
        return ((int) getDamageBuffer());
    }

    public void border$setBlockBuffer(int distance) {
        setDamageBuffer(distance);
    }

    public int border$getDamageAmount() {
        return ((int) func_177727_n());
    }

    public void border$setDamageAmount(int damage) {
        func_177744_c(damage);
    }
}
