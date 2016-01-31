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

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;
import org.spongepowered.common.launch.SpongeLaunch;
import org.spongepowered.launch.JavaVersionCheckUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
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
        // This MUST be the first line in the constructor, to prevent
        // JavaVersionCheckUtils from being passed through the transformer
        // chain, and thereby triggering Mixin to switch to PREINIT.
        Launch.classLoader.addTransformerExclusion("org.spongepowered.launch.JavaVersionCheckUtils");

        try {
            JavaVersionCheckUtils.ensureJava8();
        } catch (Exception e) {
            e.printStackTrace();
            this.clearSecurityManager();
            Runtime.getRuntime().exit(1);
        }

        Launch.classLoader.addTransformerExclusion("org.spongepowered.common.launch.");

        // Let's get this party started
        SpongeLaunch.setupMixinEnvironment()
                .addConfiguration("mixins.forge.core.json")
                .addConfiguration("mixins.forge.entityactivation.json")
                .addConfiguration("mixins.forge.bungeecord.json")
                .registerTokenProviderClass("org.spongepowered.mod.SpongeCoremod$TokenProvider");

        // Add pre-init mixins
        MixinEnvironment.getEnvironment(Phase.PREINIT)
                .addConfiguration("mixins.forge.preinit.json")
                .registerTokenProviderClass("org.spongepowered.mod.SpongeCoremod$TokenProvider");

        MixinEnvironment.getEnvironment(Phase.INIT)
                .addConfiguration("mixins.forge.init.json")
                .registerTokenProviderClass("org.spongepowered.mod.SpongeCoremod$TokenProvider");

        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.cause.Cause");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.cause.NamedCause");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.event.Cancellable");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.eventgencore.annotation.PropertySettings");
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.api.util.ResettableBuilder");

        // Transformer exclusions
        Launch.classLoader.addTransformerExclusion("ninja.leaping.configurate.");
        Launch.classLoader.addTransformerExclusion("org.apache.commons.lang3.");
        Launch.classLoader.addTransformerExclusion("org.spongepowered.mod.interfaces.IMixinEvent");

        SpongeLaunch.setupSuperClassTransformer();
    }

    private void clearSecurityManager() {
        // Nice try, FML
        try {
            Field field = System.class.getDeclaredField("security");
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                SpongeLaunch.SUPERCLASS_TRANSFORMER
        };
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
        SpongeJava6Bridge.modFile = (File) data.get("coremodLocation");
        if (SpongeJava6Bridge.modFile == null) {
            SpongeJava6Bridge.modFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return "org.spongepowered.mod.asm.transformer.SpongeAccessTransformer";
    }

}
