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


/**
 * <p>This is an example Mixin class. This code will be merged into the class(es) specified in the {@link Mixin} annotation at load time and any
 * interfaces implemented here will be monkey-patched onto the target class as well. Mixin classes <b>must</b> have the same superclass as their
 * target class, thus calls to super.whatever() and any methods in the parent class will be correct.</p>
 * 
 * <p>In order to provide support for referring to fields and methods in the target class which are not accessible, "shadow" methods and fields
 * may be created in order to allow mixin code to reference them. These shadow methods and fields must be annotated with the {@link Shadow}
 * annotation in order for the obfuscator to locate and appropriately transform them when doing a production build. In general, shadow methods will
 * be declared as <i>abstract</i> but this is not a requirement.<p>
 * 
 * <p>See below for examples.</p>
 */
@Mixin(World.class)
public abstract class MixinWorld implements IWorld {
    
    /**
     * A shadow field, describing a field in the World class which we want to be able to reference in code 
     */
    @Shadow private int ambientTickCountdown;
    
    /**
     * A shadow method, describing a method in the World class which we would like to invoke. The parameter names are not important, only the types
     * so we can change them to prevent checkstyle from bitching
     */
    @Shadow abstract int computeLightValue(int x, int y, int z, EnumSkyBlock block);
    
    /* (non-Javadoc)
     * @see org.spongepowered.mixin.interfaces.IWorld#getAmbientTickCountdown()
     */
    @Override
    public int getAmbientTickCountdown() {
        // reference the shadow field, at runtime this will reference the "real" field in the target class
        return this.ambientTickCountdown;
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.mixin.interfaces.IWorld#exampleMethodToComputeLightValue(int, int, int, net.minecraft.world.EnumSkyBlock)
     */
    @Override
    public int exampleMethodToComputeLightValue(int x, int y, int z, EnumSkyBlock block) {
        // invoke the shadow method, at runtime this will invoke the "real" method in the target class
        return this.computeLightValue(x, y, z, block);
    }
}
