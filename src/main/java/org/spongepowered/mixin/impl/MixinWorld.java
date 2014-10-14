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

import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.mixin.interfaces.IWorld;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Overwrite;
import org.spongepowered.mod.mixin.Shadow;


/**
 * <p>This is an example Mixin class. This code will be merged into the class(es) specified in the {@link Mixin} annotation at load time and any
 * interfaces implemented here will be monkey-patched onto the target class as well. Mixin classes <b>must</b> have the same superclass as their
 * target class, thus calls to super.whatever() and any methods in the parent class will be correct.</p>
 * 
 * <p>In order to provide support for referring to fields and methods in the target class which are not accessible, "shadow" methods and fields
 * may be created in order to allow mixin code to reference them. These shadow methods and fields must be annotated with the {@link Shadow}
 * annotation in order for the obfuscator to locate and appropriately transform them when doing a production build. In general, shadow methods will
 * be declared as <i>abstract</i> but this is not a requirement.</p>
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
     * A regular field, this will be merged into the target class and so will its initialiser. Note that the initialiser will run because the field
     * is static. Non-static field initialisers <b>will be ignored</b> so if you add non-static fields be sure to initialise them somewhere else!
     */
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger("sponge");
    
    /**
     * A shadow method, describing a method in the World class which we would like to invoke. The parameter names are not important, only the types
     * so we can change them to prevent checkstyle from bitching
     */
    @Shadow abstract int computeLightValue(int x, int y, int z, EnumSkyBlock block);
    
    /**
     * Another shadow method 
     */
    @Shadow abstract void notifyBlocksOfNeighborChange(int x, int y, int z, Block block);
    
    // ===============================================================================================================================================
    // Methods below demonstrate how to deal with name overlaps when a shadow method is named the same as an interface method
    // ===============================================================================================================================================
    
    /**
     * This shadow method demonstrates use of the "prefix" option in the {@link Shadow} annotation. Since it is not possible to have two methods in a
     * a class which differ only on return type, this can create problems when a shadow method overlaps with a method in an interface being
     * implemented by a mixin. Luckily, the JVM itself actually supports such overlaps, and thus we can work around the problem by renaming the
     * overlapping methods at runtime. Using the "prefix" option allows this behaviour to be leveraged. For more details see {@link Shadow#prefix}.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Shadow(prefix = "shadow$") abstract Block shadow$getBlock(int x, int y, int z);  
    
    // ===============================================================================================================================================
    // Methods below implement the IWorld interface which is applied to the target class at load time
    // ===============================================================================================================================================
    
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
    
    /**
     * This method implements getBlock from the {@link IWorld} interface. However since the method signature overlaps with the "getBlock" method
     * above, it is necessary to use the {@link Shadow#prefix} functionality in the {@link Shadow} annotation to prevent a name clash at compile
     * time.
     * 
     * @see org.spongepowered.mixin.interfaces.IWorld#getBlock(int, int, int)
     */
    @Override
    public Object getBlock(int x, int y, int z) {
        // Invoke the original "getBlock" method in World and return its value
        return this.shadow$getBlock(x, y, z);
    }
    
    // ===============================================================================================================================================
    // Methods below actually overwrite methods in the target class
    // ===============================================================================================================================================
    
    /**
     * <b>Overwrites</b> the <em>NotifyBlockChange</em> method in the target class
     * 
     * @param x
     * @param y
     * @param z
     * @param block
     */
    @Overwrite
    public void notifyBlockChange(int x, int y, int z, Block block) {
        // Uncomment this for spam! (and to check that this is working) notice that we can happily refer to the static field in this class since the
        // mixin transformer will update references to injected fields at load time. Thus qualifiers for private fields should always use the mixin
        // class itself.
        
        // MixinWorld.logger.info("Spam! Block was changed at {}, {}, {} in {}", x, y, z, this);
        
        // This method is called in the original notifyBlockChange method 
        this.notifyBlocksOfNeighborChange(x, y, z, block);
    }
    
    // ===============================================================================================================================================
    // Methods below show things which are NOT ALLOWED in a mixin. Uncomment any of them to experience full derp mode.
    // ===============================================================================================================================================
    
//    /**
//     * This <b>Non-static</b> field has an initialiser. Note that since instance initialisers are not (currently) injected, primitive types will get
//     * their default value (eg. zero in this case) and reference types will be <em>null</em>!
//     */
//    private int someValue = 3;
//    
//    /**
//     * Public, package-private, or protected static members <b>cannot<b> be injected into a target class, there is no point in doing so since there
//     * is no feasible way to invoke the injected method anyway. If you need to inject static methods, make them private.
//     */
//    public static void illegalPublicStaticMethod()
//    {
//        // derp
//    }
//    
//    /**
//     * Attempting to {@link Shadow} or {@link Overwrite} a method which doesn't exist is a detectable error condition. The same applies to shadow
//     * fields.
//     */
//    @Shadow public abstract void methodWhichDoesntExist(); 
}
