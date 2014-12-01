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

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.event.Event;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.event.EventRegistry;
import org.spongepowered.mod.guice.PluginScope;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;

@NonnullByDefault
public class SpongePluginContainer extends FMLModContainer implements PluginContainer {

    private final Map<String, Object> fmlDescriptor;
    private final String className;
    private final ModCandidate container;
    private final File source;
    private Object plugin;

    @Inject
    private PluginScope scope;

    private LoadController controller;
    private boolean enabled = true;
    private Multimap<Class<? extends Event>, Method> stateEventHandlers = ArrayListMultimap.create();

    public SpongePluginContainer(String className, ModCandidate container, Map<String, Object> modDescriptor) {
        // I suggest that you should be instantiating a proxy object, not the real plugin here.
        super("org.spongepowered.mod.plugin.SpongePluginContainer$ProxyMod", container, modDescriptor);
        this.fmlDescriptor = modDescriptor;
        this.className = className;
        this.container = container;
        this.source = container.getModContainer();

        // Allow connections from clients without this plugin
        this.fmlDescriptor.put("acceptableRemoteVersions", "*");

        SpongeMod.instance.getInjector().injectMembers(this);
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
    public String getId() {
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
        scope.setScope(this);

        super.constructMod(event);

        try {
            ModClassLoader modClassLoader = event.getModClassLoader();
            modClassLoader.clearNegativeCacheFor(container.getClassList());

            Class<?> clazz = Class.forName(className, true, modClassLoader);

            findEventHandlers(clazz);

            plugin = SpongeMod.instance.getInjector().getInstance(clazz);
        } catch (Throwable e) {
            controller.errorOccurred(this, e);
            Throwables.propagateIfPossible(e);
        }

        SpongeMod.instance.registerPluginContainer(this, getId(), getInstance());

        scope.setScope(null);
    }

    @Subscribe
    @Override
    @SuppressWarnings("unchecked")
    public void handleModStateEvent(FMLEvent event) {
        scope.setScope(this);

        Class<? extends FMLEvent> eventClass = event.getClass();
        Class<? extends Event> spongeEvent = (Class<? extends Event>) EventRegistry.getAPIClass(eventClass);
        if (stateEventHandlers.containsKey(spongeEvent)) {
            try {
                for (Method m : stateEventHandlers.get(spongeEvent)) {
                    m.invoke(plugin, event);
                }
            } catch (Throwable t) {
                controller.errorOccurred(this, t);
            }
        }

        scope.setScope(null);
    }

    @Override
    public Object getInstance() {
        return plugin;
    }

    @SuppressWarnings("unchecked")
    private void findEventHandlers(Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (a.annotationType().equals(org.spongepowered.api.util.event.Subscribe.class)) {
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 1 && Event.class.isAssignableFrom(paramTypes[0])) {
                        m.setAccessible(true);
                        stateEventHandlers.put((Class<? extends Event>) paramTypes[0], m);
                    }
                }
            }
        }
    }

    // DUMMY proxy class for FML to track
    public static class ProxyMod {

    }
}
