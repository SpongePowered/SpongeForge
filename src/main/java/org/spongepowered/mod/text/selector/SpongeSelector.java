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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import net.minecraft.command.PlayerSelector;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorBuilder;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@NonnullByDefault
public class SpongeSelector implements Selector {

    private final SelectorType type;
    private final ImmutableMap<String, String> arguments;
    private final boolean requiresLocation;

    public SpongeSelector(SelectorType type, Map<String, String> arguments, boolean requiresLocation) {
        this.type = type;
        this.arguments = ImmutableMap.copyOf(arguments);
        this.requiresLocation = requiresLocation;
    }

    @Override
    public SelectorType getType() {
        return this.type;
    }

    @Override
    public Map<String, String> getArguments() {
        return this.arguments;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Entity> resolve(Location location) throws IllegalArgumentException {
        checkArgument(!this.requiresLocation || location != null);
        return PlayerSelector.func_179656_b(new FakeCommandSender(location), this.asString(), net.minecraft.entity.Entity.class);
    }

    @Override
    public boolean requiresLocation() {
        return this.requiresLocation;
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : this.arguments.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        return "@" + this.type.getId() + "[" + sb.deleteCharAt(sb.length() - 1).toString() + "]";
    }

    @Override
    public SelectorBuilder builder() {
        return new SpongeSelectorBuilder(this);
    }

}
