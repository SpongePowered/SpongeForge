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
package org.spongepowered.mod.mixin.entity.living;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityLiving.class)
@Implements(@Interface(iface = Agent.class, prefix = "agent$"))
public abstract class MixinEntityLiving extends EntityLivingBase {

    @Shadow private boolean canPickUpLoot;

    @Shadow
    public abstract boolean isAIDisabled();

    @Shadow
    protected abstract void setNoAI(boolean p_94061_1_);

    @Shadow
    public abstract net.minecraft.entity.Entity getLeashedToEntity();

    @Shadow
    public abstract void setLeashedToEntity(net.minecraft.entity.Entity entityIn, boolean sendAttachNotification);

    public MixinEntityLiving(World worldIn) {
        super(worldIn);
    }

    public boolean agent$isAiEnabled() {
        return !isAIDisabled();
    }

    public void agent$setAiEnabled(boolean aiEnabled) {
        setNoAI(!aiEnabled);
    }

    public boolean agent$isLeashed() {
        return getLeashedToEntity() != null;
    }

    public Optional<Entity> agent$getLeashHolder() {
        return Optional.fromNullable((Entity) getLeashedToEntity());
    }

    public void agent$setLeashHolder(@Nullable Entity entity) {
        setLeashedToEntity((net.minecraft.entity.Entity) entity, true);
    }

    public boolean agent$getCanPickupItems() {
        return this.canPickUpLoot;
    }

    public void agent$setCanPickupItems(boolean canPickupItems) {
        this.canPickUpLoot = canPickupItems;
    }

}
