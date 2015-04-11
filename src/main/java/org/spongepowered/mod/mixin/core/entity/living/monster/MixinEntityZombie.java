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
package org.spongepowered.mod.mixin.core.entity.living.monster;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.api.entity.living.monster.Zombie;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(EntityZombie.class)
@Implements(@Interface(iface = Zombie.class, prefix = "zombie$"))
public abstract class MixinEntityZombie extends MixinEntityMob {

    @Shadow private static IAttribute reinforcementChance;
    @Shadow public abstract boolean isVillager();
    @Shadow public abstract void setVillager(boolean villagerZombie);
    @Shadow public abstract boolean isChild();
    @Shadow public abstract void setChild(boolean childZombie);
    @Shadow public abstract void setChildSize(boolean isChild);

    public boolean isVillagerZombie() {
        return this.isVillager();
    }

    public void setVillagerZombie(boolean villagerZombie) {
        this.setVillager(villagerZombie);
    }

    public void setAge(int age) {
        this.setChild(age < 0);
    }

    public void setBaby() {
        this.setChild(true);
    }

    public void setAdult() {
        this.setChild(false);
    }

    public boolean isBaby() {
        return this.isChild();
    }

    public boolean canBreed() {
        return this.getEntityAttribute(reinforcementChance).getAttributeValue() > 0;
    }

    public void setBreeding(boolean breeding) {
        this.getEntityAttribute(reinforcementChance).setBaseValue(breeding ? this.rand.nextDouble() * 0.10000000149011612D : 0);
    }

    public void setScaleForAge() {
        this.setChildSize(this.isChild());
    }
}
