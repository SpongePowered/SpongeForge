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
package org.spongepowered.mod.global;

/**
 * <p>Sponge Global Mod Variables</p>
 */
public class ModVariables {
    
    /**
     * <p>Sponge API Mod</p>
     */
    public static final String NAME = "SpongeAPIMod";
    
    /**
     * <p>Sponge Mod Path</p>
     */
    public static final String PATH = "org.spongepowered.mod.SpongeMod";
    
    /**
     * <p>Sponge Proxy Mod Path</p>
     */
    public static final String PROXY = "org.spongepowered.mod.plugin.SpongePluginContainer$ProxyMod";
    
    /**
     * <p>Event Transformer Path</p>
     */
    public static final String EVENT_TRANSFORMER = "org.spongepowered.mod.asm.transformers.EventTransformer";
    
    /**
     * <p>Event Priority Path</p>
     */
    public static final String EVENT_PRIORITY = "cpw.mods.fml.common.eventhandler.EventPriority";
    
    
    /**
     * <p>Yeah... We don't touch this... and we like it that way, yeah? :P</p>
     */
    private ModVariables() {
        
    }
}
