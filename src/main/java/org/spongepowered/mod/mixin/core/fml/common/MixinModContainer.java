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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.plugin.DependencyHandler;
import org.spongepowered.plugin.meta.PluginDependency;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Make FML mod containers our mod containers.
 */
@Mixin(ModContainer.class)
@Implements(@Interface(iface = PluginContainer.class, prefix = "plugin$"))
public interface MixinModContainer extends ModContainer {

    default String getId() {
        return checkNotNull(emptyToNull(getModId()), "modid");
    }

    default Optional<String> plugin$getVersion() {
        String version = emptyToNull(getVersion());
        if (version != null && (version.equalsIgnoreCase("unknown") || version.equalsIgnoreCase("dev"))) {
            version = null;
        }

        return Optional.ofNullable(version);
    }

    default Optional<String> getDescription() {
        ModMetadata meta = getMetadata();
        return meta != null ? Optional.ofNullable(emptyToNull(meta.description)) : Optional.empty();
    }

    default Optional<String> getUrl() {
        ModMetadata meta = getMetadata();
        return meta != null ? Optional.ofNullable(emptyToNull(meta.url)) : Optional.empty();
    }

    default List<String> getAuthors() {
        ModMetadata meta = getMetadata();
        return meta != null ? ImmutableList.copyOf(meta.authorList) : ImmutableList.of();
    }

    default Set<PluginDependency> plugin$getDependencies() {
        return DependencyHandler.collectDependencies(this);
    }

    default Optional<PluginDependency> getDependency(String id) {
        return Optional.ofNullable(DependencyHandler.findDependency(this, id));
    }

    default Optional<Path> plugin$getSource() {
        File source = getSource();
        if (source != null) {
            return Optional.of(source.toPath());
        }
        return Optional.empty();
    }

    default Optional<?> getInstance() {
        return Optional.ofNullable(getMod());
    }

}
