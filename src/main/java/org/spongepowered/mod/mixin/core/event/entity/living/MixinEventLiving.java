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
package org.spongepowered.mod.mixin.core.event.entity.living;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.target.entity.living.TargetLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.mixin.core.event.entity.MixinEventEntity;

@Mixin(value = LivingEvent.class, remap = false)
public abstract class MixinEventLiving extends MixinEventEntity implements TargetLivingEvent {

    @Shadow public EntityLivingBase entityLiving;

    @Override
    public Living getTargetEntity() {
        return (Living) this.entityLiving;
    }

    @SuppressWarnings("unused")
    private static LivingEvent fromSpongeEvent(TargetLivingEvent spongeEvent) {
        LivingEvent event = new LivingEvent((EntityLivingBase) spongeEvent.getTargetEntity());
        ((IMixinEvent) event).setSpongeEvent(spongeEvent);
        return event;
    }
}
