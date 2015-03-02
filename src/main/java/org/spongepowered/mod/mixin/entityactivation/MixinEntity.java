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
package org.spongepowered.mod.mixin.entityactivation;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.entity.SpongeEntityType;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.mixin.plugin.entityactivation.ActivationRange;

@NonnullByDefault
@Mixin(net.minecraft.entity.Entity.class)
public abstract class MixinEntity implements Entity, IMixinEntity {

    public final byte activationType = ActivationRange.initializeEntityActivationType((net.minecraft.entity.Entity) (Object) this);
    public boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;
    private EntityType entityType;

    @Shadow
    public boolean onGround;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onEntityConstruction(net.minecraft.world.World world, CallbackInfo ci) {
        if (world != null) {
            this.defaultActivationState = ActivationRange.initializeEntityActivationState((net.minecraft.entity.Entity) (Object) this);
            if (!this.defaultActivationState && this.entityType != null) { // if not excluded
                ActivationRange.addEntityToConfig(world, (SpongeEntityType) this.entityType, this.activationType);
            }
        } else {
            this.defaultActivationState = false;
        }
    }

    @Override
    public void inactiveTick() {
    }

    @Override
    public byte getActivationType() {
        return this.activationType;
    }

    @Override
    public long getActivatedTick() {
        return this.activatedTick;
    }

    @Override
    public boolean getDefaultActivationState() {
        return this.defaultActivationState;
    }

    @Override
    public void setActivatedTick(long tick) {
        this.activatedTick = tick;
    }
}
