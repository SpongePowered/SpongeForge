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
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.PathTokens;
import org.spongepowered.plugin.meta.version.ComparableVersion;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

/**
 * LoaderMixin_Forge adds support for a second, user-defined, mods search directory.
 *
 * <p>As well as supporting fully-qualified paths, the configured value can also
 * contain some pre-defined values which are supplied in the form of ant-style
 * tokens.</p>
 */
@Mixin(value = Loader.class, remap = false)
public abstract class LoaderMixin_Forge {

    @Shadow private static File minecraftDir;

    private ModContainer forgeImpl$mod;

    @Redirect(method = "identifyMods",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/relauncher/libraries/LibraryManager;gatherLegacyCanidates(Ljava/io/File;)Ljava/util/List;",
            remap = false
        ),
        remap = false
    )
    private List<File> discoverAndAddPluginsBeforeIterator(final File mcDir) {
        final List<File> files = LibraryManager.gatherLegacyCanidates(minecraftDir);

        final File modsFolder = new File(minecraftDir, "mods");
        final File pluginsDir = new File(PathTokens.replace(SpongeImpl.getGlobalConfigAdapter().getConfig().getGeneral().pluginsDir()));
        if (pluginsDir.isDirectory() && !pluginsDir.equals(modsFolder)) {
            FMLLog.log.info("Searching %s for plugins", pluginsDir.getAbsolutePath());
            final File[] pluginFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar") || name.endsWith(".zip"));
            if (pluginFiles != null) {
                for (final File pluginFile : pluginFiles) {
                    if (!files.contains(pluginFile)) {
                        FMLLog.log.debug("  Adding {} to the plugin list", pluginFile.getName());
                        files.add(pluginFile);
                    }
                }
            }
        }
        return files;
    }

    @Redirect(method = "sortModList", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/ModContainer;getDependencies"
            + "()Ljava/util/List;", remap = false))
    private List<ArtifactVersion> forgeImpl$AssignModBeforeGettingDependencies(final ModContainer mod) {
        this.forgeImpl$mod = mod;
        return mod.getDependencies();
    }

    @Redirect(method = "sortModList", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/versioning/ArtifactVersion;containsVersion"
            + "(Lnet/minecraftforge/fml/common/versioning/ArtifactVersion;)Z", remap = false))
    private boolean forgeImpl$sortVersion(final ArtifactVersion expected, final ArtifactVersion installed) {
        final String installedVersion = installed.getVersionString();

        if (!"unknown".equals(installedVersion) && expected instanceof DefaultArtifactVersion) {
            final VersionRange range = ((DefaultArtifactVersion) expected).getRange();

            if (range != null && range.getRecommendedVersion() != null
                    && !installedVersion.equals(range.getRecommendedVersion().getVersionString())) {

                final BigInteger majorExpected = new ComparableVersion(range.getRecommendedVersion().getVersionString()).getFirstInteger();
                if (majorExpected != null) {

                    final BigInteger majorInstalled = new ComparableVersion(installedVersion).getFirstInteger();

                    // Show a warning if the major version does not match,
                    // or if the installed version is lower than the recommended version
                    if (majorInstalled != null
                            && (!majorExpected.equals(majorInstalled) || installed.compareTo(range.getRecommendedVersion()) < 0)) {
                        SpongeImpl.getLogger().warn("The mod {} was designed for {} {} but version {} is in use. It may not work properly.",
                                this.forgeImpl$mod.getModId(), expected.getLabel(), expected.getRangeString(), installed.getVersionString());
                    }
                }
            }

        }

        return expected.containsVersion(installed);
    }

}
