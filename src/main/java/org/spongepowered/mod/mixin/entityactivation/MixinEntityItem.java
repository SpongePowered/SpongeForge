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

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinWorld;

@NonnullByDefault
@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends Entity implements Item {

    @Shadow
    public abstract net.minecraft.item.ItemStack getEntityItem();

    @Shadow
    private int delayBeforeCanPickup;

    @Shadow
    private int age;

    @Shadow
    public int lifespan;

    public MixinEntityItem(World worldIn) {
        super(worldIn);
    }

    public void inactiveTick() {
        if (this.delayBeforeCanPickup > 0 && this.delayBeforeCanPickup != 32767)
        {
            --this.delayBeforeCanPickup;
        }

        if (!this.worldObj.isRemote
                && this.age >= ((IMixinWorld) this.worldObj).getWorldConfig().getRootNode().getNode("entity", "item-despawn-rate").getInt()) {
            this.setDead();
        }
    }

}
