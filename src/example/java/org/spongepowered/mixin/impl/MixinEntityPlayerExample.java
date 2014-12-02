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
package org.spongepowered.mixin.impl;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.mixin.interfaces.IEntityPlayerConflict;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

/**
 * Mixin demonstrating how "soft implementation" works
 */
@Mixin(EntityPlayer.class)

// This is the soft implementation, the @Implements annotation can actually take multiple @Interface annotations so it is still possible for a mixin
// to implement multiple conflicting interfaces, should such a situation ever arise. The original implements clause is commented out, removing the 
// comment will cause a compiler error
@Implements(@Interface(iface = IEntityPlayerConflict.class, prefix = "entityPlayer$"))
public abstract class MixinEntityPlayerExample extends EntityLivingBase { // implements IEntityPlayerConflict {

    /**
     * ctor, not used
     */
    public MixinEntityPlayerExample(World worldIn) {
        super(worldIn);
    }

    /**
     * Conflicting method, now magically safe to implement because the prefix makes it compile
     */
    public double entityPlayer$getHealth() {
        return this.getHealth();
    }

    /**
     * This non-conflicting method is also prefixed, this is recommended for soft implementations because there is no {@link Override} annotation and
     * thus if the method in the underlying interface changes, there is no compile-time error which indicates this. By using the prefix even on
     * non-conflicting methods, the transformer can verify that the method exists in the target interface at application time.
     */
    public int entityPlayer$thisMethodDoesNotConflict() {
        return 0;
    }

    /**
     * This method doesn't conflict, but is not tagged with the prefix. Whilst this is totally legal, it's a bad idea because there is then no way
     * to detect errors when the underlying interface changes, see the notes on {@link #entityPlayer$thisMethodDoesNotConflict}
     */
    public int norDoesThisOne() {
        return 0;
    } 
}
