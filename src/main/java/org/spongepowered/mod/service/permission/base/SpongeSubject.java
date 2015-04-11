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

import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.mod.service.permission.SpongePermissionService;

import java.util.List;
import java.util.Set;

public abstract class SpongeSubject implements Subject {
    private final SpongePermissionService service;

    protected SpongeSubject(SpongePermissionService service) {
        this.service = service;
    }

    protected SpongePermissionService getService() {
        return this.service;
    }

    @Override
    public SubjectData getTransientData() {
        return getData();
    }

    @Override
    public abstract MemorySubjectData getData();

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return getPermissionValue(contexts, permission) == Tristate.TRUE;
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasPermission(getActiveContexts(), permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return getDataPermissionValue(getData(), permission);
    }

    protected Tristate getDataPermissionValue(MemorySubjectData subject, String permission) {
        Tristate res = getData().getNodeTree(SubjectData.GLOBAL_CONTEXT).get(permission);

        if (res == Tristate.UNDEFINED) {
            for (Subject parent : subject.getParents(SubjectData.GLOBAL_CONTEXT)) {
                Tristate tempRes = parent.getPermissionValue(SubjectData.GLOBAL_CONTEXT, permission);
                if (tempRes != Tristate.UNDEFINED) {
                    res = tempRes;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public boolean isChildOf(Subject parent) {
        return isChildOf(getActiveContexts(), parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return getData().getParents(contexts).contains(parent);
    }

    @Override
    public List<Subject> getParents() {
        return getParents(getActiveContexts());
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return getData().getParents(contexts);
    }

    @Override
    public Set<Context> getActiveContexts() {
        return SubjectData.GLOBAL_CONTEXT;
    }
}
