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
import net.minecraftforge.event.entity.EntityEvent;
import org.spongepowered.api.event.entity.living.LivingDeathEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {net.minecraftforge.event.entity.living.LivingDeathEvent.class}, remap = false)
public abstract class MixinEventLivingDeath extends EntityEvent implements LivingDeathEvent {

    public MixinEventLivingDeath(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public Location getLocation() {
        return getEntity().getLocation();
    }

    /*
    private static net.minecraftforge.event.entity.living.LivingDeathEvent fromSpongeEvent(org.spongepowered.api.event.entity.living.LivingDeathEvent spongeEvent) {
        LivingDeathEvent event = new LivingDeathEvent((EntityLivingBase) spongeEvent.getEntity(), Dam);
        ((IMixinEvent) event).setSpongeEvent(spongeEvent);
        return event;
    }
    */
}
