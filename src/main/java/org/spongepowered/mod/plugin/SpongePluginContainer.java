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
package org.spongepowered.mod.plugin;

import java.io.File;
import java.util.Map;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.mod.SpongeMod;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.event.FMLConstructionEvent;

public class SpongePluginContainer extends FMLModContainer implements PluginContainer {

    private final Map<String, Object> fmlDescriptor;
    private final String className;
    private final ModCandidate container;
    private final File source;
    private Object plugin;

    private LoadController controller;
    private boolean enabled = true;
    
    public SpongePluginContainer(String className, ModCandidate container, Map<String, Object> modDescriptor) {
        // I suggest that you should be instantiating a proxy object, not the real plugin here.
        super("org.spongepowered.mod.plugin.SpongePluginContainer$ProxyMod", container, modDescriptor);
        this.fmlDescriptor = modDescriptor;
        this.className = className;
        this.container = container;
        this.source = container.getModContainer();
        
        // Allow connections from clients without this plugin
        this.fmlDescriptor.put("acceptableRemoteVersions", "*");
    }

    @Override
    public String getModId() {
        return (String) fmlDescriptor.get("id");
    }

    @Override
    public void bindMetadata(MetadataCollection mc) {
        super.bindMetadata(mc);
    }

    @Override
    public String getID() {
        return getModId();
    }
    
    @Override
    public void setEnabledState(boolean enabled) {
        super.setEnabledState(enabled);
        this.enabled = enabled;
    }

    @Override
    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        super.constructMod(event);

        try {
            ModClassLoader modClassLoader = event.getModClassLoader();
            modClassLoader.clearNegativeCacheFor(container.getClassList());
            
            Class<?> clazz = Class.forName(className, true, modClassLoader);

            plugin = clazz.newInstance();
        } catch (Throwable e) {
            controller.errorOccurred(this, e);
            Throwables.propagateIfPossible(e);
        }
        
        SpongeMod.instance.registerPluginContainer(this, getID(), getInstance());
    }

    @Override
    public Object getInstance() {
        return plugin;
    }

    // DUMMY proxy class for FML to track
    public static class ProxyMod {
    }
}
