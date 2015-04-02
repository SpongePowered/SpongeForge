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
package org.spongepowered.granite.launch.server;

import com.google.common.io.Resources;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.granite.launch.GraniteLaunch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public final class VanillaServerTweaker implements ITweaker {

    private static final Logger logger = LogManager.getLogger();

    private String[] args = ArrayUtils.EMPTY_STRING_ARRAY;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        GraniteLaunch.initialize(gameDir != null ? gameDir : new File("."));

        if (args != null && !args.isEmpty()) {
            this.args = args.toArray(new String[args.size()]);
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader loader) {
        logger.info("Initializing Granite...");

        // We shouldn't load these through Launchwrapper as they use native dependencies
        loader.addClassLoaderExclusion("io.netty.");
        loader.addClassLoaderExclusion("jline.");
        loader.addClassLoaderExclusion("org.fusesource.");

        // Don't allow libraries to be transformed
        loader.addTransformerExclusion("joptsimple.");

        // Minecraft Server libraries
        loader.addTransformerExclusion("com.google.gson.");
        loader.addTransformerExclusion("org.apache.commons.codec.");
        loader.addTransformerExclusion("org.apache.commons.io.");
        loader.addTransformerExclusion("org.apache.commons.lang3.");

        // SpongeAPI
        loader.addTransformerExclusion("com.flowpowered.math.");
        loader.addTransformerExclusion("org.slf4j.");

        // Guice
        loader.addTransformerExclusion("com.google.inject.");
        loader.addTransformerExclusion("org.aopalliance.");

        // Configurate
        loader.addTransformerExclusion("ninja.leaping.configurate.");
        loader.addTransformerExclusion("com.typesafe.config.");

        // Granite Launch
        loader.addClassLoaderExclusion("org.spongepowered.tools.");
        loader.addClassLoaderExclusion("org.spongepowered.granite.launch.");
        loader.addTransformerExclusion("org.spongepowered.granite.mixin.");

        // The server GUI won't work if we don't exclude this: log4j2 wants to have this in the same classloader
        loader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");

        // Check if we're running in deobfuscated environment already
        logger.debug("Applying runtime deobfuscation...");
        if (isObfuscated()) {
            logger.info("Deobfuscation mappings are provided by MCP (http://www.modcoderpack.com)");
            Launch.blackboard.put("granite.deobf-srg", Resources.getResource("mappings.srg"));
            loader.registerTransformer("org.spongepowered.granite.launch.transformers.DeobfuscationTransformer");
            logger.debug("Runtime deobfuscation is applied.");
        } else {
            logger.debug("Runtime deobfuscation was not applied. Granite is being loaded in a deobfuscated environment.");
        }

        logger.debug("Applying access transformer...");
        Launch.blackboard.put("granite.at", new URL[] {Resources.getResource("common_at.cfg"), Resources.getResource("granite_at.cfg")});
        loader.registerTransformer("org.spongepowered.granite.launch.transformers.AccessTransformer");

        logger.debug("Initializing Mixin environment...");
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.addConfiguration("mixins.granite.json");
        env.setSide(MixinEnvironment.Side.SERVER);
        loader.registerTransformer(MixinBootstrap.TRANSFORMER_CLASS);

        logger.info("Initialization finished. Starting Minecraft server...");
    }

    private static boolean isObfuscated() {
        try {
            return Launch.classLoader.getClassBytes("net.minecraft.world.World") == null;
        } catch (IOException ignored) {
            return true;
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }

    @Override
    public String[] getLaunchArguments() {
        return this.args;
    }

}
