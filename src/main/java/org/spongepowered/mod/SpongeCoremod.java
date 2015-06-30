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
package org.spongepowered.mod;

import net.minecraftforge.fml.common.Loader;

import net.minecraftforge.common.ForgeVersion;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.launch.SpongeLaunch;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8")
public class SpongeCoremod implements IFMLLoadingPlugin {

    public static final class TokenProvider implements IEnvironmentTokenProvider {

        @Override
        public int getPriority() {
            return IEnvironmentTokenProvider.DEFAULT_PRIORITY;
        }

        @Override
        public Integer getToken(String token, MixinEnvironment env) {
            if ("FORGE".equals(token)) {
                return Integer.valueOf(ForgeVersion.getBuildVersion());
            } else if ("FML".equals(token)) {
                String fmlVersion = Loader.instance().getFMLVersionString();
                int build = Integer.parseInt(fmlVersion.substring(fmlVersion.lastIndexOf('.') + 1));
                return Integer.valueOf(build);
            }
            return null;
        }

    }

    public SpongeCoremod() {

        // These exclusions aren't added by FML on the server side.
        // Adding them prevents Optional from being loaded by the LaunchClassLoader,
        // which results in an issue with events that extend CauseTracked.
        Launch.classLoader.addClassLoaderExclusion("com.google.common.");
        Launch.classLoader.addClassLoaderExclusion("org.apache.");

        // Let's get this party started
        MixinBootstrap.init();

        // Add pre-init mixins
        MixinEnvironment.getEnvironment(Phase.PREINIT)
                .addConfiguration("mixins.forge.base.json")
                .registerTokenProviderClass("org.spongepowered.mod.SpongeCoremod$TokenProvider");

        SpongeLaunch.initialize(null, null, null);
        Sponge.getGlobalConfig(); // Load config

        // Add default mixins
        MixinEnvironment.getDefaultEnvironment()
                .addConfiguration("mixins.common.api.json")
                .addConfiguration("mixins.common.core.json")
                .addConfiguration("mixins.forge.core.json")
                .addConfiguration("mixins.forge.entityactivation.json")
                .registerTokenProviderClass("org.spongepowered.mod.SpongeCoremod$TokenProvider");

        // Classloader exclusions - TODO: revise when event pkg refactor reaches impl
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.cause.CauseTracked");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.Cancellable");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.util.event.callback.CallbackList");
        Launch.classLoader.addTransformerExclusion("org.spongepowered.mod.interfaces.IMixinEvent");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.Event");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.util.annotation.ImplementedBy");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.AbstractEvent");

        // Transformer exclusions
        Launch.classLoader.addTransformerExclusion("ninja.leaping.configurate.");
        Launch.classLoader.addTransformerExclusion("org.apache.commons.lang3.");
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return "org.spongepowered.mod.SpongeMod";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if ((Boolean)data.get("runtimeDeobfuscationEnabled")) {
            MixinEnvironment.getDefaultEnvironment()
                    .registerErrorHandlerClass("org.spongepowered.mod.mixin.handler.MixinErrorHandler");
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return "org.spongepowered.mod.asm.transformers.SpongeAccessTransformer";
    }

}
