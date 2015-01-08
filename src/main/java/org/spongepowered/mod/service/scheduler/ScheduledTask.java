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

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.scheduler.Task;

import java.util.UUID;

// WIP

public class ScheduledTask implements Task {
    protected long offset;
    protected long period;
    protected PluginContainer owner;
    protected Runnable runnableBody;
    protected long timestamp;
    protected ScheduledTaskState state;
    protected UUID id;

    // Internal Task state. Not for user-service use.
    protected enum ScheduledTaskState {
        WAITING,
        RUNNING,
        CANCELED,
    }

    // No c'tor without arguments.
    private ScheduledTask() {
    }

    // This c'tor is OK.
    protected ScheduledTask(long x, long t) {
        offset = x;
        period = t;
        owner = null;
        runnableBody = null;
        state = ScheduledTaskState.WAITING;
        id = UUID.randomUUID();
    }

    // Builder method
    protected ScheduledTask setState(ScheduledTaskState state) {
        this.state = state;
        return this;
    }

    // Builder method
    protected ScheduledTask setOffset(long x) {
        offset = x;
        return this;
    }

    // Builder method
    protected ScheduledTask setPeriod(long t) {
        period = t;
        return this;
    }

    // Builder method
    protected ScheduledTask setTimestamp(long ts) {
        timestamp = ts;
        return this;
    }

    // Builder method
    protected ScheduledTask setPluginContainer(PluginContainer owner) {
        this.owner = owner;
        return this;
    }

    // Builder method
    protected ScheduledTask setRunnableBody(Runnable body) {
        runnableBody = body;
        return this;
    }

    @Override
    public PluginContainer getOwner() {
        return owner;
    }

    @Override
    public long getDelay() {
        return offset;
    }

    @Override
    public long getInterval() { return period; }

    @Override
    public boolean cancel() {

        boolean bResult = true;

        // When a task is canceled, it is removed from the list
        // Even if the task is a repeating task, by removing it from the list of tasks
        // known in the Scheduler, the task will not repeat.

        state = ScheduledTask.ScheduledTaskState.CANCELED;

        // TODO -- possibly also release other resources (?) like the Runnable thread body?
        // etc..
        // bResult = true;

        return bResult;
    }

    @Override
    public Runnable getRunnable() {
        return runnableBody;
    }

    @Override
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public String getName() {
        return null;
    }
}
