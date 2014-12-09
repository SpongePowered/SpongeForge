/**
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
package org.spongepowered.mod.mixin.entity.living.monster;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;


@NonnullByDefault
@Mixin(EntityCreeper.class)
@Implements(@Interface(iface = Creeper.class, prefix = "creeper$"))
public abstract class MixinEntityCreeper extends EntityMob {

    @Shadow
    private int explosionRadius;

    @Shadow
    public abstract boolean getPowered();

    public MixinEntityCreeper(World worldIn) {
        super(worldIn);
    }

    public boolean creeper$isPowered() {
        return getPowered();
    }

    public void creeper$setPowered(boolean powered) {
        if (powered) {
            this.dataWatcher.updateObject(17, Byte.valueOf((byte) 1));
        } else {
            this.dataWatcher.updateObject(17, Byte.valueOf((byte) 0));
        }
    }

    public int creeper$getExplosionRadius() {
        return this.explosionRadius;
    }

    public void creeper$setExplosionRadius(int radius) {
        this.explosionRadius = radius;
    }
}
