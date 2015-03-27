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

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.mod.mixin.MixinWorldProviderExample;


/**
 * An example mixin interface which contains a method whose signature conflicts with a method in the target class
 */
public interface IEntityPlayerConflict {

    /**
     * In {@link EntityLivingBase}, this same method exists but returns a float. Whilst java bytecode would actually allow both methods to exist, the
     * java compiler doesn't support this. This conflict is deliberately here to demostrate the use of the {@link Implements} annotation in
     * {@link MixinWorldProviderExample}
     *
     * @return return value
     */
    public abstract double getHealth();

    /**
     * This method conflicts with a method in the target class and has precisely the same signature, this is to demonstrate how we deal with a method
     * which would ordinarily fall foul of reobfuscation and thus break our (non-obfuscated) interface in a production environment
     *
     * @return return value
     */
    public abstract boolean isUsingItem();

    /**
     * Additional method which doesn't conflict
     *
     * @return return value
     */
    public abstract int thisMethodDoesNotConflict();

    /**
     * Additional method with no conflicts
     *
     * @return return value
     */
    public abstract int norDoesThisOne();
}
