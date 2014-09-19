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

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import org.spongepowered.mixin.interfaces.IWorld;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;


@Mixin(World.class)
public abstract class MixinWorld implements IWorld {
    
    @Shadow private int ambientTickCountdowns;
    
    @Shadow abstract int computeLightValue(int p_98179_1_, int p_98179_2_, int p_98179_3_, EnumSkyBlock p_98179_4_);
    
    @Override
    public int getAmbientTickCountdown() {
        return this.ambientTickCountdowns;
    }
    
    @Override
    public int exampleMethodToComputeLightValue(int x, int y, int z, EnumSkyBlock block) {
        return this.computeLightValue(x, y, z, block);
    }
}
