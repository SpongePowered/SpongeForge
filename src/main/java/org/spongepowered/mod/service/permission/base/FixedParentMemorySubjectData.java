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
package org.spongepowered.mod.service.permission.base;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.context.Context;

import java.util.List;
import java.util.Set;

/**
 * Implementation that forces a single parent to always be part of the parents.
 */
public class FixedParentMemorySubjectData extends GlobalMemorySubjectData {
    private final Subject forcedParent;

    /**
     * Creates a new subject data instance, using the provided service to request instances of permission subjects.
     *
     * @param service The service to request subjects from
     */
    public FixedParentMemorySubjectData(PermissionService service, Subject parent) {
        super(service);
        this.forcedParent = parent;
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return ImmutableList.<Subject>builder().add(this.forcedParent).addAll(super.getParents(contexts)).build();
    }

    @Override
    public boolean addParent(Set<Context> contexts, Subject parent) {
        if (Objects.equal(this.forcedParent, parent) && GLOBAL_CONTEXT.equals(contexts)) {
            return true;
        }
        return super.addParent(contexts, parent);
    }

    @Override
    public boolean removeParent(Set<Context> contexts, Subject parent) {
        if (Objects.equal(this.forcedParent, parent)) {
            return false;
        }
        return super.removeParent(contexts, parent);
    }
}
