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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.mod.service.permission.base.SpongeSubject;
import org.spongepowered.mod.service.permission.base.SpongeSubjectCollection;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataFactoryCollection extends SpongeSubjectCollection {
    private final SpongePermissionService service;
    private final ConcurrentMap<String, SpongeSubject> subjects = new ConcurrentHashMap<String, SpongeSubject>();
    private final Function<String, MemorySubjectData> dataFactory;
    private final Function<String, CommandSource> commandSourceFunction;

    protected DataFactoryCollection(String identifier, SpongePermissionService service, Function<String, MemorySubjectData> dataFactory,
            Function<String, CommandSource> commandSourceFunction) {
        super(identifier);
        this.service = service;
        this.dataFactory = dataFactory;
        this.commandSourceFunction = commandSourceFunction;
    }

    @Override
    public Subject get(String identifier) {
        Preconditions.checkNotNull(identifier, "identifier");
        SpongeSubject ret = this.subjects.get(identifier);
        if (ret == null) {
            SpongeSubject newRet = this.subjects.putIfAbsent(identifier, new DataFactorySubject(identifier, this.dataFactory.apply(identifier)));
            if (newRet != null) {
                ret = newRet;
            }
        }
        return ret;
    }

    @Override
    public boolean hasRegistered(String identifier) {
        return this.subjects.containsKey(identifier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Subject> getAllSubjects() {
        return (Iterable) this.subjects.values();
    }

    private class DataFactorySubject extends SpongeSubject {
        private final String identifier;
        private final MemorySubjectData data;

        protected DataFactorySubject(String identifier, MemorySubjectData data) {
            super(DataFactoryCollection.this.service);
            this.identifier = identifier;
            this.data = data;
        }

        @Override
        public String getIdentifier() {
            return this.identifier;
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            return Optional.fromNullable(DataFactoryCollection.this.commandSourceFunction.apply(getIdentifier()));
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return DataFactoryCollection.this;
        }

        @Override
        public MemorySubjectData getData() {
            return this.data;
        }

        @Override
        public Tristate getPermissionValue(Set<Context> contexts, String permission) {
            Tristate ret = super.getPermissionValue(contexts, permission);
            if (ret == Tristate.UNDEFINED) {
                ret = getDataPermissionValue(DataFactoryCollection.this.service.getDefaultData(), permission);
            }
            return ret;

        }
    }
}
