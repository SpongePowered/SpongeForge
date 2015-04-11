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
package org.spongepowered.mod.mixin.plugin;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.mod.configuration.SpongeConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CoreMixinPlugin implements IMixinConfigPlugin {

    private List<String> mixins = new ArrayList<String>();
    private static SpongeConfig<SpongeConfig.GlobalConfig> GLOBAL_CONFIG;
    private static File SPONGE_CONFIG_DIR = new File("." + File.separator + "config" + File.separator + "sponge" + File.separator);

    @Override
    public void onLoad(String mixinPackage) {
        try {
            GLOBAL_CONFIG = new SpongeConfig<SpongeConfig.GlobalConfig>(SpongeConfig.Type.GLOBAL, new File(SPONGE_CONFIG_DIR, "global" + ".conf"),
                    "sponge");
        } catch (Throwable t) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(t));
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName,
            String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return this.mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
            String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
            String mixinClassName, IMixinInfo mixinInfo) {
    }

    public static SpongeConfig<SpongeConfig.GlobalConfig> getGlobalConfig() {
        return GLOBAL_CONFIG;
    }

    public static File getConfigDir() {
        return SPONGE_CONFIG_DIR;
    }

}
