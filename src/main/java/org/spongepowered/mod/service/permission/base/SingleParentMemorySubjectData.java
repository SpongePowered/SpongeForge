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

import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.mod.service.permission.OpLevelCollection;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SingleParentMemorySubjectData extends GlobalMemorySubjectData {
    private Subject parent;

    /**
     * Creates a new subject data instance, using the provided service to request instances of permission subjects.
     *

     * @param service The service to request subjects from
     */
    public SingleParentMemorySubjectData(PermissionService service) {
        super(service);
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        final Subject parent = getParent();
        return GLOBAL_CONTEXT.equals(contexts) && parent != null ? Collections.singletonList(parent) : Collections.<Subject>emptyList();
    }

    @Override
    public boolean addParent(Set<Context> contexts, Subject parent) {
        if (!(parent instanceof OpLevelCollection.OpLevelSubject)) {
            return false;
        }

        if (!GLOBAL_CONTEXT.equals(contexts)) {
            return false;
        }
        return setParent(parent);
    }

    @Override
    public boolean removeParent(Set<Context> contexts, Subject parent) {
        if (parent == this.parent) {
            return setParent(null);
        }
        return false;
    }

    @Override
    public boolean clearParents() {
        return removeParent(GLOBAL_CONTEXT, this.parent);
    }

    @Override
    public boolean clearParents(Set<Context> contexts) {
        return GLOBAL_CONTEXT.equals(contexts) && clearParents();
    }

    public boolean setParent(Subject parent) {
        this.parent = parent;
        return true;
    }

    public Subject getParent() {
        return this.parent;
    }
}
