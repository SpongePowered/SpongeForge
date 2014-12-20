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

package org.spongepowered.mod.service.scheduler;


import com.google.common.base.Optional;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.service.scheduler.Task;

import java.util.Collection;
import java.util.UUID;

// WIP  Lots left to do.

public class SyncScheduler implements SynchronousScheduler {

    @Override
    public void step() {

    }

    @Override
    public Optional<Task> runTask(Object plugin, Runnable task) {
        return null;
    }

    @Override
    public Optional<Task> runTaskAfter(Object plugin, Runnable task, long delay) {
        return null;
    }

    @Override
    public Optional<Task> runRepeatingTask(Object plugin, Runnable task, long interval) {
        return null;
    }

    @Override
    public Optional<Task> runRepeatingTaskAfter(Object plugin, Runnable task, long interval, long delay) {
        return null;
    }

    @Override
    public Optional<Task> getTaskById(UUID id) {
        return null;
    }

    @Override
    public Collection<Task> getScheduledTasks() {
        return null;
    }

    @Override
    public Collection<Task> getScheduledTasks(Object plugin) {
        return null;
    }
}
