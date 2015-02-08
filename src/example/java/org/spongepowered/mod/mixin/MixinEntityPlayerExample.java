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
package org.spongepowered.mod.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.exampleinterfaces.IEntityPlayerConflict;

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
     *
     * @param worldIn The world to spawn the player in
     */
    public MixinEntityPlayerExample(World worldIn) {
        super(worldIn);
    }

    // ===============================================================================================================================================
    // Methods below demonstrate use of the soft implementation declared on this mixin class.
    // ===============================================================================================================================================

    /**
     * Conflicting method, now magically safe to implement because the prefix makes it compile
     *
     * @return The player's health
     */
    public double entityPlayer$getHealth() {
        return this.getHealth();
    }

    /**
     * This non-conflicting method is also prefixed, this is recommended for soft implementations because there is no {@link Override} annotation and
     * thus if the method in the underlying interface changes, there is no compile-time error which indicates this. By using the prefix even on
     * non-conflicting methods, the transformer can verify that the method exists in the target interface at application time.
     *
     * @return The number 0
     */
    public int entityPlayer$thisMethodDoesNotConflict() {
        return 0;
    }

    /**
     * This method doesn't conflict, but is not tagged with the prefix. Whilst this is totally legal, it's a bad idea because there is then no way
     * to detect errors when the underlying interface changes, see the notes on {@link #entityPlayer$thisMethodDoesNotConflict}
     *
     * @return The number 0
     */
    public int norDoesThisOne() {
        return 0;
    }

    // ===============================================================================================================================================
    // Declaring the implementation of a conflicting method 
    // ===============================================================================================================================================

    /**
     * We need this shadow field because it is referenced in the base implementation of isUsingItem()
     */
    @Shadow private ItemStack itemInUse;

    /**
     * This method does something custom, we want to inject a call to this method into isUsingItem
     */
    private void doSomethingCustom() {
        //        System.err.println("I like big butts and I can not lie");
    }

    /**
     * <p>This comes first in the file for a reason, but you should read the javadoc for {@link #isUsingItem} first, then come back and read this...
     * Go on! Do it!</p>
     *
     * <p>Okay, so you understand why we have the method below, it injects our custom code in the target class's method by overwriting the method
     * body with the new code. However in order to preserve that functionality across the obfuscation boundary we need to tag it with
     * {@link Overwrite}.</p>
     *
     * <p>The magic happens here. Because this method is <b>not</b> tagged with {@link Overwrite}, it will <b>not</b> be obfuscated at build time,
     * this means it still implements the interface. At dev time, the method below (because it appears <b>after</b> this one) will be injected and
     * will <em>overwrite <b>this</b> method</em>. This is exactly what we want to happen, because otherwise this method (at dev time) would actually
     * end up just calling itself recursively!</p>
     *
     * <p>However, post-obfuscation, this method magically becomes an accessor for the (now renamed) isUsingItem() in the target class, and thus
     * allows <em>both</em> the custom code to be injected into the original method (by the declaration below) <em>and</em> the interface to be
     * implemented all at once.</p>
     *
     * <p>See the example below for where custom code is <b>not</b> required in the accessor</p>.
     *
     * @return Whether the player is using the item
     */
    public boolean entityPlayer$isUsingItem() {
        return this.isUsingItem();
    }

    /**
     * <p>It should be pretty obvious that because this method exists in target class {@link EntityPlayer} <em>and</em> in the interface
     * {@link IEntityPlayerConflict} that we don't <em>actually</em> need an implementation here at dev time, because the underlying method in the
     * target class already implicitly 'implements' the method in the interface. We only need to {@link Overwrite} it if we need to include some
     * custom functionality as shown here. However of course the problems start when we traverse the obfuscation boundary, since the method ends up
     * actually named "func_71039_bw" and thus no longer implements the interface!</p>
     *
     * <p>We need the {@link Overwrite} annotation in order to have this method renamed, but we don't want to break the interface. So how do we do
     * that? See {@link #entityPlayer$isUsingItem} above for how.</p>
     *
     * @return Whether the player is using the item
     */
    @Overwrite
    public boolean isUsingItem() {
        // Custom thing
        this.doSomethingCustom();

        // Essentially the base implementation of isUsingItem
        return this.itemInUse != null;
    }

    // ===============================================================================================================================================
    // But what if we DON'T want to inject custom code into the target method? We just want essentially an accessor?
    // ===============================================================================================================================================

    /**
     * <p>Then things become laughably simple. In a nutshell, we just copy-paste the target method into our mixin!</p>
     *
     * <p><em>What? It's that simple?</em></p>
     *
     * <p><b>Yes!</b> And it's because of the nature of {@link Overwrite}, notice how our new champion <em>is not</em> annotated with
     * {@link Overwrite}. The {@link Overwrite} annotation is used to indicate to the annotation processor that the method should be obfuscated
     * <em>as if it existed in the target class</em>, by omitting the annotation this method with simply replace the target method at dev time, and
     * exist in tandem with it in production.</p>
     *
     * <p><em>So what's the catch?</em><p>
     *
     * <p>Firstly, unless we're talking about reasonably simple accessor methods like this one, copy-pasting an implementation of an entire method
     * could be pretty overkill and result in a lot of {@link Shadow} fields being required. Secondly, it won't work if the implementation in the
     * target class is actually an override of a method in a parent class. For the former scenario, a design rethink may be required, or use of other
     * bytecode engineering techniques. For the latter, see below.</p>
     * /
     public boolean isUsingItem() {
     // The base implementation of isUsingItem
     return this.itemInUse != null;
     }

     //
     ===============================================================================================================================================
     // So what do we do if the method is an override?
     //
     ===============================================================================================================================================

     /**
     * <p>Naturally, if the method in the target class in an override, we run into the same scenario as using {@link Overwrite}, namely that the
     * methodwill be included in the obfuscation AST (by its relationship to the parent class) and will thus get obfuscated even though we don't want
     * it to.</p>
     *
     * <p>The solution this time is to use only soft implementation, this replicates the behaviour above where the method will be simply replaced at
     * dev time, and co-exist at production time, but <em>won't</em> be obfuscated by virtue of the fact that the prefix decouples the method from its
     * superclass counterpart.</p>
     * /
     public boolean entityPlayer$isUsingItem() {
     // The base implementation of isUsingItem
     return this.itemInUse != null;
     }

     */
}
