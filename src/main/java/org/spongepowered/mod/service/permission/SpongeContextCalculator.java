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
package org.spongepowered.mod.service.permission;

import com.google.common.base.Optional;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.service.permission.context.ContextCalculator;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.LocatedSource;
import org.spongepowered.api.world.World;

import java.util.Set;

/**
 * A context calculator handling world contexts.
 */
public class SpongeContextCalculator implements ContextCalculator {
    @Override
    public void accumulateContexts(Subject subject, Set<Context> accumulator) {
        Optional<CommandSource> subjSource = subject.getCommandSource();
        if (subjSource.isPresent() && subjSource.get() instanceof LocatedSource) {
            World currentExt = ((LocatedSource) subjSource.get()).getWorld();
            accumulator.add(currentExt.getContext());
            accumulator.add((currentExt.getDimension().getContext()));
        }
    }

    @Override
    public boolean matches(Context context, Subject subject) {
        Optional<CommandSource> subjSource = subject.getCommandSource();
        if (subjSource.isPresent() && subjSource.get() instanceof LocatedSource && context.getType().equals(Context.WORLD_KEY)) {
            LocatedSource source = ((LocatedSource) subjSource.get());
            if (context.getType().equals(Context.WORLD_KEY)) {
                return source.getWorld().getContext().equals(context);
            } else if (context.getType().equals(Context.DIMENSION_KEY)) {
                return source.getWorld().getDimension().getContext().equals(context);
            }
        }
        return false;
    }
}
