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

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import org.spongepowered.plugin.meta.PluginDependency;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public final class DependencyHandler {

    private DependencyHandler() {
    }

    public static Set<PluginDependency> collectDependencies(ModContainer container) {
        Map<String, PluginDependency> result = new HashMap<>();

        addDependencies(result, PluginDependency.LoadOrder.NONE, container.getRequirements(), false);
        addDependencies(result, PluginDependency.LoadOrder.AFTER, container.getDependants(), true);
        addDependencies(result, PluginDependency.LoadOrder.BEFORE, container.getDependencies(), true);

        return ImmutableSet.copyOf(result.values());
    }

    private static void addDependencies(Map<String, PluginDependency> result, PluginDependency.LoadOrder loadOrder,
            Iterable<ArtifactVersion> dependencies, boolean optional) {

        for (ArtifactVersion version : dependencies) {
            String id = version.getLabel();
            if (id == null) {
                continue;
            }

            result.put(id, buildDependency(result.get(id), loadOrder, version, optional));
        }
    }

    @Nullable
    public static PluginDependency findDependency(ModContainer container, String id) {
        PluginDependency current = findDependency(id, null, PluginDependency.LoadOrder.NONE, container.getRequirements(), false);
        current = findDependency(id, current, PluginDependency.LoadOrder.AFTER, container.getDependants(), true);
        return findDependency(id, current, PluginDependency.LoadOrder.BEFORE, container.getDependencies(), true);
    }

    private static PluginDependency findDependency(String id, @Nullable PluginDependency current, PluginDependency.LoadOrder loadOrder,
            Iterable<ArtifactVersion> dependencies, boolean optional) {

        for (ArtifactVersion version : dependencies) {
            String dependencyId = version.getLabel();
            if (dependencyId == null || !dependencyId.equals(id)) {
                continue;
            }

            current = buildDependency(current, loadOrder, version, optional);
        }

        return current;
    }

    private static PluginDependency buildDependency(PluginDependency current, PluginDependency.LoadOrder loadOrder, ArtifactVersion version,
            boolean optional) {
        String versionRange = version.getRangeString();
        if (versionRange.equals("any")) {
            versionRange = null;
        }

        if (optional && current != null && !current.isOptional()) {
            // If current dependency is required, then it should stay required
            optional = false;
        }

        return new PluginDependency(loadOrder, version.getLabel(), versionRange, optional);
    }

}
