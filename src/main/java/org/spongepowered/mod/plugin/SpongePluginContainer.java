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
package org.spongepowered.mod.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.event.Event;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.event.EventRegistry;
import org.spongepowered.mod.guice.SpongePluginGuiceModule;

import javax.annotation.Nullable;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@NonnullByDefault
public class SpongePluginContainer extends FMLModContainer implements PluginContainer {

    private final Map<String, Object> fmlDescriptor;
    @SuppressWarnings("unused")
    private final File source;

    private Injector selfInjector;

    @SuppressWarnings("unused")
    private boolean enabled = true;
    private Multimap<Class<? extends Event>, Method> stateEventHandlers = ArrayListMultimap.create();

    public SpongePluginContainer(String className, ModCandidate container, Map<String, Object> modDescriptor) {
        super(className, container, modDescriptor);
        this.fmlDescriptor = modDescriptor;
        this.source = container.getModContainer();
        try {
            Class<?> c = FMLModContainer.class;
            Field langAdapter = c.getDeclaredField("languageAdapter");
            langAdapter.setAccessible(true);
            langAdapter.set(this, GuiceJavaAdapter.INSTANCE);
        } catch (Throwable t) {
            SpongeMod.instance.getController().errorOccurred(this, t);
        }

        // Allow connections from clients without this plugin
        this.fmlDescriptor.put("acceptableRemoteVersions", "*");

        this.selfInjector = SpongeMod.instance.getInjector().createChildInjector(new SpongePluginGuiceModule(this));
    }

    @Override
    public String getModId() {
        return (String) this.fmlDescriptor.get("id");
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
        super.constructMod(event);
        SpongeMod.instance.getGame().getEventManager().register(getInstance(), getInstance());
    }

    @Subscribe
    @Override
    @SuppressWarnings("unchecked")
    public void handleModStateEvent(FMLEvent event) {
        Class<? extends FMLEvent> eventClass = event.getClass();
        Class<? extends Event> spongeEvent = (Class<? extends Event>) EventRegistry.getAPIClass(eventClass);
        if (this.stateEventHandlers.containsKey(spongeEvent)) {
            try {
                for (Method m : this.stateEventHandlers.get(spongeEvent)) {
                    m.invoke(this.getMod(), event);
                }
            } catch (Throwable t) {
                SpongeMod.instance.getController().errorOccurred(this, t);
            }
        }
    }

    @Override
    public Object getInstance() {
        return getMod();
    }

    @SuppressWarnings("unchecked")
    protected @Nullable Method gatherAnnotations(Class<?> clazz) throws Exception {
        for (Method m : clazz.getDeclaredMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (a.annotationType().equals(org.spongepowered.api.util.event.Subscribe.class)) {
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 1 && Event.class.isAssignableFrom(paramTypes[0])) {
                        m.setAccessible(true);
                        this.stateEventHandlers.put((Class<? extends Event>) paramTypes[0], m);
                    }
                }
            }
        }
        return null;
    }

    protected void processFieldAnnotations(ASMDataTable asm) throws Exception {}

    private static class GuiceJavaAdapter extends ILanguageAdapter.JavaAdapter {
        public static final GuiceJavaAdapter INSTANCE = new GuiceJavaAdapter();

        private GuiceJavaAdapter() {
        }

        @Override
        public Object getNewInstance(FMLModContainer container, Class<?> objectClass, ClassLoader classLoader, Method factoryMarkedMethod) throws Exception {
            if (!(container instanceof SpongePluginContainer)) {
                throw new IllegalArgumentException("Guice adapter only supports Sponge containers");
            }
            Object mod;
            if (factoryMarkedMethod != null) {
                mod = factoryMarkedMethod.invoke(null);
                ((SpongePluginContainer) container).selfInjector.injectMembers(mod);
            } else {
                mod = ((SpongePluginContainer) container).selfInjector.getInstance(objectClass);
            }
            return mod;
        }
    }

    @Override
    public String toString() {
        return "SpongePlugin:" + getName() + "{" + getVersion() + "}";
    }

}
