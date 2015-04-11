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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.mod.service.permission.base.GlobalMemorySubjectData;
import org.spongepowered.mod.service.permission.base.SpongeSubject;
import org.spongepowered.mod.service.permission.base.SpongeSubjectCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpLevelCollection extends SpongeSubjectCollection {
    private final Map<String, OpLevelSubject> levels;

    public OpLevelCollection(SpongePermissionService service) {
        super(PermissionService.SUBJECTS_GROUP);
        ImmutableMap.Builder<String, OpLevelSubject> build = ImmutableMap.builder();
        for (int i = 0; i <= 4; ++i) {
            build.put("op_" + i, new OpLevelSubject(service, i)); // TODO: Add subject data
        }
        this.levels = build.build();
    }

    @Override
    public Subject get(String identifier) {
        if (this.levels.containsKey(identifier)) {
            return this.levels.get(identifier);
        } else {
            throw new IllegalArgumentException(identifier + " is not a valid op level group name (op_{0,4})");
        }
    }

    @Override
    public boolean hasRegistered(String identifier) {
        return this.levels.containsKey(identifier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Subject> getAllSubjects() {
        return (Collection) this.levels.values();
    }

    public static class OpLevelSubject extends SpongeSubject {
        private final int level;
        private final MemorySubjectData data;

        public OpLevelSubject(final SpongePermissionService service, final int level) {
            super(service);
            this.level = level;
            this.data = new GlobalMemorySubjectData(service) {
                @Override
                public List<Subject> getParents(Set<Context> contexts) {
                    if (!GLOBAL_CONTEXT.equals(contexts)) {
                        return Collections.emptyList();
                    }
                    if (level == 0) {
                        return super.getParents(contexts);
                    } else {
                        return ImmutableList.<Subject>builder().add(service.getGroupForOpLevel(level - 1)).addAll(super.getParents(contexts)).build();
                    }
                }
            };
        }

        public int getOpLevel() {
            return this.level;
        }

        @Override
        public String getIdentifier() {
            return "op_" + this.level;
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            return Optional.absent();
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return getService().getGroupSubjects();
        }

        @Override
        public MemorySubjectData getData() {
            return this.data;
        }
    }
}
