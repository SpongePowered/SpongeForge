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

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MixinLoader adds support for a second, user-defined, mods search directory.
 * 
 * <p>As well as supporting fully-qualified paths, the configured value can also
 * contain some pre-defined values which are supplied in the form of ant-style
 * tokens.</p>
 */
@Mixin(value = Loader.class, remap = false)
public abstract class MixinLoader {
    
    /**
     * Token which contains the fully-qualified path to FML's "mods" folder 
     */
    private static final String PATHTOKEN_CANONICAL_MODS_DIR = "CANONICAL_MODS_DIR";
    
    /**
     * Token which contains the fully-qualified path to the game directory (profile root) 
     */
    private static final String PATHTOKEN_CANONICAL_GAME_DIR = "CANONICAL_GAME_DIR";
    
    /**
     * Token which contains the current minecraft version as a string 
     */
    private static final String PATHTOKEN_MC_VERSION = "MC_VERSION";
    
    // Shadowed for tokens
    @Shadow private static File minecraftDir;
    @Shadow private File canonicalModsDir;
    
    @Redirect(method = "identifyMods", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraftforge/fml/common/discovery/ModDiscoverer;findModDirMods(Ljava/io/File;[Ljava/io/File;)V"
    ))
    private void discoverMods(ModDiscoverer modDiscoverer, File modsDir, File[] additionalMods) {
        modDiscoverer.findModDirMods(modsDir);
        
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
        String pluginsDirName = SpongeImpl.getGlobalConfig().getConfig().getGeneral().pluginsDir();
        Map<String, String> tokens = this.getPathTokens();
        for (Entry<String, String> token : tokens.entrySet()) {
            pluginsDirName = pluginsDirName.replace(token.getKey(), token.getValue());
        }
            
        return new File(pluginsDirName);
    }

    @Unique
    private Map<String, String> getPathTokens() {
        Map<String, String> tokens = Maps.newHashMap();
        tokens.put(MixinLoader.formatToken(MixinLoader.PATHTOKEN_CANONICAL_MODS_DIR), this.canonicalModsDir.getAbsolutePath());
        tokens.put(MixinLoader.formatToken(MixinLoader.PATHTOKEN_CANONICAL_GAME_DIR), MixinLoader.minecraftDir.getAbsolutePath());
        tokens.put(MixinLoader.formatToken(MixinLoader.PATHTOKEN_MC_VERSION), Loader.MC_VERSION);
        return tokens;
    }
    
    @Unique
    private static String formatToken(String name) {
        return String.format("${%s}", name);
    }

}
