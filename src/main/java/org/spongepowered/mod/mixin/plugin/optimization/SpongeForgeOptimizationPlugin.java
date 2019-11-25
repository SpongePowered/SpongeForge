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
package org.spongepowered.mod.mixin.plugin.optimization;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.category.OptimizationCategory;
import org.spongepowered.common.config.type.GlobalConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SpongeForgeOptimizationPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(final String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        final GlobalConfig globalConfig = SpongeImpl.getGlobalConfigAdapter().getConfig();
        if (globalConfig.getModules().useOptimizations()) {
            final Function<OptimizationCategory, Boolean> optimizationCategoryBooleanFunction = mixinEnabledMappings.get(mixinClassName);
            if (optimizationCategoryBooleanFunction == null) {
                new PrettyPrinter(50).add("Could not find function for optimization patch").centre().hr()
                    .add("Missing function for class: " + mixinClassName)
                    .trace();
                return false;
            }
            return optimizationCategoryBooleanFunction.apply(globalConfig.getOptimizations());
        }
        return false;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {

    }

    private static final Map<String, Function<OptimizationCategory, Boolean>> mixinEnabledMappings = ImmutableMap.<String, Function<OptimizationCategory, Boolean >> builder()
        .put("org.spongepowered.mod.mixin.optimization.threadchecks.MinecraftMixin_ForgeThreadChecks",
            OptimizationCategory::useFastThreadChecks)
            .put("org.spongepowered.mod.mixin.optimization.threadchecks.MinecraftServerMixin_ForgeThreadChecks",
            OptimizationCategory::useFastThreadChecks)
        .put("org.spongepowered.mod.mixin.optimization.threadchecks.SpongeImplHooksMixin_ForgeThreadChecks",
            OptimizationCategory::useFastThreadChecks)
        .put("org.spongepowered.mod.mixin.optimization.logspam.ForgeHooksMixin_DisableLogSpamFromAdvancements",
            OptimizationCategory::disableFailingAdvancementDeserialization)
        .build();

}
