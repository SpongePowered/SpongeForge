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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.spongepowered.api.plugin.Plugin.ID_PATTERN;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
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
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.inject.plugin.PluginModule;
import org.spongepowered.common.plugin.PluginContainerExtension;

import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// PluginContainer is implemented indirectly through the mixin to ModContainer
public class SpongeModPluginContainer implements ModContainer, PluginContainerExtension {

    // This is the implementation (SpongeForge) injector.
    @Inject private static Injector spongeInjector;
    private final String id;

    private final String className;
    private final ModCandidate candidate;
    private final Map<String, Object> descriptor;

    private ModMetadata metadata;
    private boolean invalid; // Cannot throw until plugin is getting constructed
    private boolean enabled = true;

    private Object instance;

    private DefaultArtifactVersion processedVersion;
    private LoadController controller;

    private Injector injector;

    private PluginContainer pluginContainer = (PluginContainer) (Object) this;
    private static final String ID_WARNING = "Plugin IDs should be lowercase, and only contain characters from "
            + "a-z, dashes or underscores, start with a lowercase letter, and not exceed 64 characters.";

    public SpongeModPluginContainer(String className, ModCandidate candidate, Map<String, Object> descriptor) {
        this.id = checkNotNull((String) descriptor.get("id"), "id");

        this.className = className;
        this.candidate = candidate;
        this.descriptor = descriptor;

        if (!ID_PATTERN.matcher(this.id).matches()) {
            SpongeImpl.getLogger().error("Skipping plugin with invalid plugin ID '{}'. " + ID_WARNING, this.id);
            this.invalid = true;
        }
    }

    @Override
    public String getModId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.metadata.name;
    }

    @Override
    public String getVersion() {
        return this.metadata.version;
    }

    @Override
    public File getSource() {
        return this.candidate.getModContainer();
    }

    @Override
    public ModMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindMetadata(MetadataCollection mc) {
        this.metadata = mc.getMetadataForId(this.id, this.descriptor);

        if (isNullOrEmpty(this.metadata.name)) {
            this.metadata.name = this.id;
        }

        if (this.metadata.version == null) {
            this.metadata.version = "";
        }

        if (this.metadata.autogenerated) {
            if (!this.invalid) {
                SpongeImpl.getLogger().warn("Plugin '{}' seems to be missing a valid mcmod.info metadata file. This is not a problem when testing "
                        + "plugins, however it is recommended to include one in public plugins.\n"
                        + "Please see https://docs.spongepowered.org/master/en/plugin/plugin-meta.html for details.", this.id);
            }

            // Version is set in the dummy automatically (see getMetadataForId)
            this.metadata.description = getDescriptorValue("description");
            this.metadata.url = getDescriptorValue("url");

            Collection<String> authors = (Collection<String>) this.descriptor.get("authors");
            if (authors != null) {
                this.metadata.authorList = new ArrayList<>(authors);
            }

            Object deps = this.descriptor.get("dependencies");
            if (deps != null) {
                Iterable<Map<String, Object>> depDescriptors = (Iterable<Map<String, Object>>) this.descriptor.get("dependencies");
                if (depDescriptors != null) {
                    Set<ArtifactVersion> requirements = this.metadata.requiredMods;
                    List<ArtifactVersion> dependencies = this.metadata.dependencies;

                    for (Map<String, Object> depDescriptor : depDescriptors) {
                        String dep = checkNotNull((String) depDescriptor.get("id"), "dependency id");

                        if (this.id.equals(dep)) {
                            this.invalid = true;
                            SpongeImpl.getLogger().error("Plugin '{}' cannot have a dependency on itself. This is redundant and should be "
                                    + "removed.", this.id);
                            continue;
                        }

                        String depVersion = (String) depDescriptor.get("version");

                        ArtifactVersion dependency;
                        if (isNullOrEmpty(depVersion)) {
                            dependency = new DefaultArtifactVersion(dep, true);
                        } else {
                            dependency = new DefaultArtifactVersion(dep, VersionParser.parseRange(depVersion));
                        }

                        Boolean optional = (Boolean) depDescriptor.get("optional");
                        if (optional == null || !optional) {
                            requirements.add(dependency);
                        }

                        // TODO: Load order
                        dependencies.add(dependency);
                    }
                }
            }
        } else {
            // Check dependencies
            Iterator<ArtifactVersion> itr = this.metadata.requiredMods.iterator();
            while (itr.hasNext()) {
                if (this.id.equals(itr.next().getLabel())) {
                    SpongeImpl.getLogger().warn("Plugin '{}' requires itself to be loaded. This is redundant and can be removed from the "
                            + "dependencies.", this.id);
                    itr.remove();
                }
            }

            if (!this.metadata.dependants.isEmpty()) {
                SpongeImpl.getLogger().error("Invalid dependency with load order AFTER on plugin '{}'. This is currently not supported for Sponge "
                        + "plugins. Requested dependencies: {}", this.id, this.metadata.dependants);
                this.invalid = true;
            }

            this.metadata.dependants = ImmutableList.of();
        }
    }

    private String getDescriptorValue(String key) {
        return (String) this.descriptor.getOrDefault(key, "");
    }

    @Override
    public void setEnabledState(boolean isEnabled) {
        this.enabled = isEnabled;
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        return this.metadata.requiredMods;
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return this.metadata.dependencies;
    }

    @Override
    public List<ArtifactVersion> getDependants() {
        return this.metadata.dependants;
    }

    @Override
    public String getSortingRules() {
        return this.metadata.printableSortingRules();
    }

    @Override
    public boolean matches(Object mod) {
        return this.instance == mod;
    }

    @Override
    public Object getMod() {
        return this.instance;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        if (this.enabled) {
            this.controller = controller;
            bus.register(this);
            return true;
        }
        return false;
    }

    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        try {
            if (this.invalid) {
                throw new InvalidPluginException();
            }

            // Add source file to classloader so we can load it.
            ModClassLoader modClassLoader = event.getModClassLoader();
            modClassLoader.addFile(getSource());
            modClassLoader.clearNegativeCacheFor(this.candidate.getClassList());

            Class<?> pluginClazz = Class.forName(this.className, true, modClassLoader);

            Injector injector = spongeInjector.getParent().createChildInjector(new PluginModule((PluginContainer) this, pluginClazz));
            this.injector = injector;
            this.instance = injector.getInstance(pluginClazz);

            // TODO: Detect Scala or use meta to know if we're scala and use proper adapter here...
            ProxyInjector.inject(this, event.getASMHarvestedData(), FMLCommonHandler.instance().getSide(), new ILanguageAdapter.JavaAdapter());

            Sponge.getEventManager().registerListeners(this, this.instance);
        } catch (Throwable t) {
            this.controller.errorOccurred(this, t);
        }
    }

    @Override
    public ArtifactVersion getProcessedVersion() {
        if (this.processedVersion == null) {
            String version = getVersion();
            if (isNullOrEmpty(version)) {
                this.processedVersion = new DefaultArtifactVersion(this.id, true);
            } else {
                this.processedVersion = new DefaultArtifactVersion(this.id, version);
            }
        }

        return this.processedVersion;
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
        try {
            return Class.forName(getSource().isDirectory() ? "net.minecraftforge.fml.client.FMLFolderResourcePack" :
                    "net.minecraftforge.fml.client.FMLFileResourcePack", true, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public Map<String, String> getSharedModDescriptor() {
        Map<String, String> descriptor = new HashMap<>();

        descriptor.put("modsystem", "Sponge");
        descriptor.put("id", this.id);
        descriptor.put("version", getDisplayVersion());
        descriptor.put("name", getName());
        descriptor.put("url", this.metadata.url);
        descriptor.put("authors", this.metadata.getAuthorList());
        descriptor.put("description", this.metadata.description);

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
        return null;
    }

    @Override
    public List<String> getOwnedPackages() {
        return this.candidate.getContainedPackages();
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
    public void setClassVersion(int classVersion) {

    }

    @Override
    public int getClassVersion() {
        return 0;
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper("Plugin")
                .omitNullValues()
                .add("id", this.pluginContainer.getId())
                .add("name", this.pluginContainer.getName())
                .add("version", this.pluginContainer.getVersion().orElse(null))
                .add("description", this.pluginContainer.getDescription().orElse(null))
                .add("url", this.pluginContainer.getUrl().orElse(null))
                .add("authors", this.pluginContainer.getAuthors().isEmpty() ? null : this.pluginContainer.getAuthors())
                .add("source", this.pluginContainer.getSource().orElse(null))
                .toString();
    }
}
