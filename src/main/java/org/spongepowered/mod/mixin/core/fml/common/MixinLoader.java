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
package org.spongepowered.mod.mixin.core.fml.common;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.PathTokens;
import org.spongepowered.plugin.meta.version.DefaultArtifactVersion;
import org.spongepowered.plugin.meta.version.InvalidVersionSpecificationException;
import org.spongepowered.plugin.meta.version.VersionRange;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

/**
 * MixinLoader adds support for a second, user-defined, mods search directory.
 *
 * <p>As well as supporting fully-qualified paths, the configured value can also
 * contain some pre-defined values which are supplied in the form of ant-style
 * tokens.</p>
 */
@Mixin(value = Loader.class, remap = false)
public abstract class MixinLoader {

    private ModContainer mod;

    @Redirect(method = "identifyMods", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraftforge/fml/common/discovery/ModDiscoverer;findModDirMods(Ljava/io/File;[Ljava/io/File;)V"
    ))
    private void discoverMods(ModDiscoverer modDiscoverer, File modsDir, File[] additionalMods) {
        modDiscoverer.findModDirMods(modsDir, additionalMods);

        File pluginsDir = this.getPluginsDir();
        if (pluginsDir.isDirectory() && !pluginsDir.equals(modsDir)) {
            FMLLog.info("Searching %s for plugins", pluginsDir.getAbsolutePath());
            this.discoverPlugins(modDiscoverer, pluginsDir);
        }
    }

    @Unique
    private void discoverPlugins(ModDiscoverer modDiscoverer, File pluginsDir) {
        modDiscoverer.findModDirMods(pluginsDir, new File[0]);
    }

    @Unique
    private File getPluginsDir() {
        return new File(PathTokens.replace(SpongeImpl.getGlobalConfig().getConfig().getGeneral().pluginsDir()));
    }

    @Redirect(method = "sortModList", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/ModContainer;getDependencies"
            + "()Ljava/util/List;"))
    private List<ArtifactVersion> onGetDependencies(ModContainer mod) {
        this.mod = mod;
        return mod.getDependencies();
    }

    @Redirect(method = "sortModList", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/versioning/ArtifactVersion;containsVersion"
            + "(Lnet/minecraftforge/fml/common/versioning/ArtifactVersion;)Z"))
    private boolean onCheckContainsVersion(ArtifactVersion expected, ArtifactVersion installed) throws InvalidVersionSpecificationException {
        String rangeString = expected.getRangeString();
        String versionString = installed.getVersionString();

        if (!rangeString.equals("any") && !versionString.equals("unknown")) {

            VersionRange range = VersionRange.createFromVersionSpec(rangeString);
            if (range.getRecommendedVersion() instanceof DefaultArtifactVersion) {

                BigInteger majorExpected = ((DefaultArtifactVersion) range.getRecommendedVersion()).getVersion().getFirstInteger();
                if (majorExpected != null) {

                    DefaultArtifactVersion installedVersion = new DefaultArtifactVersion(versionString);
                    BigInteger majorInstalled = installedVersion.getVersion().getFirstInteger();

                    // Show a warning if the major version does not match,
                    // or if the installed version is lower than the recommended version
                    if (majorInstalled != null
                            && (!majorExpected.equals(majorInstalled) || installedVersion.compareTo(range.getRecommendedVersion()) < 0)) {
                        SpongeImpl.getLogger().warn("The mod {} was designed for {} {} but version {} is in use. It may not work properly.",
                                this.mod.getModId(), expected.getLabel(), rangeString, versionString);
                    }
                }
            }

        }
        return expected.containsVersion(installed);
    }

}
