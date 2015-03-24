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
package org.spongepowered.mod.text.selector;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorBuilder;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@NonnullByDefault
public class SpongeSelector implements Selector {

    protected final SelectorType type;
    protected final ImmutableMap<ArgumentType<?>, Argument<?>> arguments;

    private final String plain;

    public SpongeSelector(SelectorType type, ImmutableMap<ArgumentType<?>, Argument<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
        this.plain = buildString();
    }

    @Override
    public SelectorType getType() {
        return this.type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(ArgumentType<T> type) {
        Argument<T> argument = (Argument<T>) this.arguments.get(type);
        return argument != null ? Optional.of(argument.getValue()) : Optional.<T>absent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Argument<T>> getArgument(ArgumentType<T> type) {
        return Optional.fromNullable((Argument<T>) this.arguments.get(type));
    }

    @Override
    public List<Argument<?>> getArguments() {
        return this.arguments.values().asList();
    }

    @SuppressWarnings("unchecked")
    private List<Entity> resolve(ICommandSender resolver) {
        return (List<Entity>) PlayerSelector.matchEntities(resolver, this.plain, Entity.class);
    }

    @Override
    public List<Entity> resolve(Extent extent) {
        return resolve(new SelectorResolver(extent));
    }

    @Override
    public List<Entity> resolve(Location location) {
        return resolve(new SelectorResolver(location));
    }

    @Override
    public String toPlain() {
        return this.plain;
    }

    @Override
    public SelectorBuilder builder() {
        return null;
    }

    private String buildString() {
        StringBuilder result = new StringBuilder();

        result.append('@').append(this.type.getId());

        if (!this.arguments.isEmpty()) {
            result.append('[');
            Collection<Argument<?>> args = this.arguments.values();
            for (Iterator<Argument<?>> iter = args.iterator(); iter.hasNext();) {
                Argument<?> arg = iter.next();
                result.append(arg.toPlain());
                if (iter.hasNext()) {
                    result.append(',');
                }
            }
            result.append(']');
        }

        return result.toString();
    }

}
