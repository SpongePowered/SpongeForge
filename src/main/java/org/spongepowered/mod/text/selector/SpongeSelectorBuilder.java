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
package org.spongepowered.mod.text.selector;

import java.util.Collections;
import java.util.Map;

import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorBuilder;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

@NonnullByDefault
public class SpongeSelectorBuilder implements SelectorBuilder {

    private static final ImmutableList<String> LOCATION_DEPENDENT_ARGS = ImmutableList.of("x", "y", "z", "dx", "dy", "dz", "rm", "r");

    private SelectorType type;
    private Map<String, String> arguments = Maps.newHashMap();

    public SpongeSelectorBuilder() {
    }

    public SpongeSelectorBuilder(Selector copyFrom) {
        this.type = copyFrom.getType();
        this.arguments = copyFrom.getArguments();
    }

    @Override
    public SelectorBuilder type(SelectorType type) {
        this.type = type;
        return this;
    }

    @Override
    public SelectorBuilder addArgument(String key, String value) {
        this.arguments.put(key, value);
        return this;
    }

    @Override
    public SelectorBuilder addArgument(String key, int value) {
        addArgument(key, Integer.toString(value));
        return this;
    }

    @Override
    public SelectorBuilder removeArgument(String key) {
        this.arguments.remove(key);
        return this;
    }

    @Override
    public SelectorBuilder center(Vector3i center) {
        addArgument("x", center.getX());
        addArgument("y", center.getY());
        addArgument("z", center.getZ());
        return this;
    }

    @Override
    public SelectorBuilder name(String name) {
        name(name, false);
        return this;
    }

    @Override
    public SelectorBuilder name(String name, boolean invert) {
        addArgument("name", (invert ? "!" : "") + name);
        return this;
    }

    @Override
    public SelectorBuilder team(String team) {
        team(team, false);
        return this;
    }

    @Override
    public SelectorBuilder team(String team, boolean invert) {
        addArgument("team", (invert ? "!" : "") + team);
        return this;
    }

    @Override
    public SelectorBuilder type(EntityType type) {
        type(type, false);
        return this;
    }

    @Override
    public SelectorBuilder type(EntityType type, boolean invert) {
        addArgument("type", (invert ? "!" : "") + type.toString());
        return this;
    }

    @Override
    public Selector build() {
        Preconditions.checkState(this.type != null);
        boolean requiresLocation = Collections.disjoint(this.arguments.keySet(), LOCATION_DEPENDENT_ARGS) || this.type.equals(SelectorTypes.NEAREST_PLAYER) || (this.arguments.containsKey("c") && !this.type.equals(SelectorTypes.RANDOM_PLAYER));
        return new SpongeSelector(this.type, this.arguments, requiresLocation);
    }

}
