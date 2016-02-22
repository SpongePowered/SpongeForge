/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ILanguageAdapter;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.ProxyInjector;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.guice.SpongePluginGuiceModule;
import org.spongepowered.common.plugin.SpongePluginContainer;

import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public class SpongeModPluginContainer extends SpongePluginContainer implements ModContainer {

    private String pluginClassName;
    private ModCandidate modCandidate;
    private Map<String, Object> pluginDescriptor;
    private ModMetadata modMetadata;
    private boolean enabled = true;
    private EventBus fmlEventBus;
    private LoadController fmlController;
    private Injector injector;

    private Optional<Object> pluginInstance = Optional.empty();

    public SpongeModPluginContainer(String className, ModCandidate candidate, Map<String, Object> descriptor) {
        this.pluginClassName = className;
        this.modCandidate = candidate;
        this.pluginDescriptor = descriptor;
    }

    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        try {
            // Add source file to classloader so we can load it.
            ModClassLoader modClassLoader = event.getModClassLoader();
            modClassLoader.addFile(this.modCandidate.getModContainer());
            modClassLoader.clearNegativeCacheFor(this.modCandidate.getClassList());

            Class<?> pluginClazz = Class.forName(this.pluginClassName, true, modClassLoader);

            Injector injector = SpongeImpl.getInjector().createChildInjector(new SpongePluginGuiceModule(this, pluginClazz));
            this.injector = injector;
            this.pluginInstance = Optional.of(injector.getInstance(pluginClazz));

            SpongeEventManager spongeBus = (SpongeEventManager) SpongeImpl.getGame().getEventManager();
            // TODO Detect Scala or use meta to know if we're scala and use proper adapter here...
            ProxyInjector.inject(this, event.getASMHarvestedData(), FMLCommonHandler.instance().getSide(), new ILanguageAdapter.JavaAdapter());
            spongeBus.registerListener(this, this.pluginInstance.get());
        } catch (Throwable t) {
            this.fmlController.errorOccurred(this, t);
            //Throwables.propagateIfPossible(t);
        }
    }

    @Override
    public String getModId() {
        return (String) this.pluginDescriptor.get("id");
    }

    @Override
    @Nonnull
    public String getName() {
        return (String) this.pluginDescriptor.get("name");
    }

    @Override
    @Nonnull
    public String getVersion() {
        String annotationVersion = (String) this.pluginDescriptor.get("version");
        return (annotationVersion != null) ? annotationVersion : "unknown";
    }

    @Override
    public File getSource() {
        return this.modCandidate.getModContainer();
    }

    @Override
    public ModMetadata getMetadata() {
        return this.modMetadata;
    }

    @Override
    public void bindMetadata(MetadataCollection mc) {
        // Note: Much simpler than FML's, since I'm assuming there's no useful information.
        // All information given here is from mcmod.info which we haven't documented as part of plugin 'API'
        this.modMetadata = mc.getMetadataForId(getModId(), this.pluginDescriptor);

        String annotationDependencies = (String) this.pluginDescriptor.get("dependencies");

        Set<ArtifactVersion> requirements = Sets.newHashSet();
        List<ArtifactVersion> dependencies = Lists.newArrayList();
        List<ArtifactVersion> dependants = Lists.newArrayList();

        Loader.instance().computeDependencies(annotationDependencies, requirements, dependencies, dependants);

        this.modMetadata.requiredMods = requirements;
        this.modMetadata.dependencies = dependencies;
        this.modMetadata.dependants = dependants;
    }

    @Override
    public void setEnabledState(boolean isEnabled) {
        this.enabled = isEnabled;
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        return this.modMetadata.requiredMods;
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return this.modMetadata.dependencies;
    }

    @Override
    public List<ArtifactVersion> getDependants() {
        return this.modMetadata.dependants;
    }

    @Override
    public String getSortingRules() {
        return (String) this.pluginDescriptor.get("dependencies");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        if (this.enabled) {
            this.fmlEventBus = bus;
            this.fmlController = controller;
            this.fmlEventBus.register(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(Object mod) {
        return this.pluginInstance.isPresent() && this.pluginInstance.get() == mod;
    }

    @Override
    public Object getMod() {
        return this.pluginInstance.orElse(null);
    }

    @Override
    public ArtifactVersion getProcessedVersion() {
        // Note: FML caches this.
        return new DefaultArtifactVersion(getModId(), getVersion());
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public String getDisplayVersion() {
        return getVersion();
    }

    @Override
    public VersionRange acceptableMinecraftVersionRange() {
        return Loader.instance().getMinecraftModContainer().getStaticVersionRange();
    }

    @Override
    public Certificate getSigningCertificate() {
        return null;
    }

    @Override
    public Map<String, String> getCustomModProperties() {
        return EMPTY_PROPERTIES;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        // Note: Has meaning only on client side, so skipping for now.
        return null;
    }

    @Override
    public Map<String, String> getSharedModDescriptor() {
        Map<String, String> descriptor = Maps.newHashMap();

        descriptor.put("modsystem", "Sponge");
        descriptor.put("id", getModId());
        descriptor.put("version", getDisplayVersion());
        descriptor.put("name", getName());

        // Note: FML puts url, authors, description here as well, but that comes from mcmod.info instead of annotations.
        // So far we haven't really documented anything about mcmod.info for plugin use, so I've been assuming no info.

        return descriptor;
    }

    @Override
    public Disableable canBeDisabled() {
        // Note: No flag to indicate if a plugin allows it, can only happen while server is not running.
        // (e.g., main menu in SSP). Defaulting to on restart only.
        return Disableable.RESTART;
    }

    @Override
    public String getGuiClassName() {
        // Note: Not needed, client-side only
        return "";
    }

    @Override
    public List<String> getOwnedPackages() {
        return this.modCandidate.getContainedPackages();
    }

    @Override
    @Nonnull
    public String getId() {
        return getModId();
    }

    @Override
    @Nonnull
    public Optional<Object> getInstance() {
        return this.pluginInstance;
    }

    @Override
    public boolean shouldLoadInEnvironment() {
        return true;
    }

    @Override
    public URL getUpdateUrl() {
        return null;
    }

    @Override
    public Injector getInjector() {
        return injector;
    }
    
}
