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
package org.spongepowered.mixin.interfaces;

import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.mod.mixin.Implements;


/**
 * An example mixin interface which contains a method whose signature conflicts with a method in the target class
 */
public interface IEntityPlayerConflict extends IWorld {

    /**
     * In {@link EntityLivingBase}, this same method exists but returns a float. Whilst java bytecode would actually allow both methods to exist, the
     * java compiler doesn't support this. This conflict is deliberately here to demostrate the use of the {@link Implements} annotation in
     * {@link MixinEntityPlayerExample}
     */
    public abstract double getHealth();
    
    /**
     * Additional method which doesn't conflict
     */
    public abstract int thisMethodDoesNotConflict();
    
    /**
     * Additional method with no conflicts
     */
    public abstract int norDoesThisOne();
}
