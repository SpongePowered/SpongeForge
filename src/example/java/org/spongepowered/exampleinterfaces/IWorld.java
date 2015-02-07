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
package org.spongepowered.exampleinterfaces;

import net.minecraft.world.EnumSkyBlock;


/**
 * An example mixin interface, this interface describes the contract of the mixin and will be patched onto the mixin's target class at runtime. 
 */
public interface IWorld {

    /**
     * Stupid example
     *
     * @return return value
     */
    public abstract int getAmbientTickCountdown();

    /**
     * Even more tenuous example
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param block The SkyBlock value
     *
     * @return The calculated light
     */
    public abstract int exampleMethodToComputeLightValue(int x, int y, int z, EnumSkyBlock block);

    /**
     * Contrived example to deliberately create a name clash with World
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     *
     * @return The block
     */
    public abstract Object getBlock(int x, int y, int z);
}
