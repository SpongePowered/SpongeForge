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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobalMemorySubjectData extends MemorySubjectData {

    /**
     * Creates a new subject data instance, using the provided service to request instances of permission subjects.
     *
     * @param service The service to request subjects from
     */
    public GlobalMemorySubjectData(PermissionService service) {
        super(service);
    }


    @Override
    public Map<Set<Context>, List<Subject>> getAllParents() {
        return ImmutableMap.of(GLOBAL_CONTEXT, getParents(GLOBAL_CONTEXT));
    }

    @Override
    public boolean setPermission(Set<Context> contexts, String permission, Tristate value) {
        if (!GLOBAL_CONTEXT.equals(contexts)) {
            return false;
        }
        return super.setPermission(contexts, permission, value);
    }

    @Override
    public boolean clearPermissions(Set<Context> contexts) {
        if (!GLOBAL_CONTEXT.equals(contexts)) {
            return false;
        }
        return super.clearPermissions(contexts);
    }

    @Override
    public boolean addParent(Set<Context> contexts, Subject parent) {
        if (!GLOBAL_CONTEXT.equals(contexts)) {
            return false;
        }
        return super.addParent(contexts, parent);
    }

    @Override
    public boolean removeParent(Set<Context> contexts, Subject parent) {
        if (!GLOBAL_CONTEXT.equals(contexts)) {
            return false;
        }
        return super.removeParent(contexts, parent);
    }

    @Override
    public boolean clearParents(Set<Context> contexts) {
        if (!GLOBAL_CONTEXT.equals(contexts)) {
            return false;
        }
        return super.clearParents(contexts);
    }
}
