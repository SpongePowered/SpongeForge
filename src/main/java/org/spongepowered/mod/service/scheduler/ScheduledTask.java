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
package org.spongepowered.mod.service.scheduler;

import com.google.common.base.Optional;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.scheduler.Task;

import java.util.UUID;

/**
 * <p>
 * ScheduledTask is an internal representation of a Task created by the Plugin
 * through one of the Scheduler interfaces.
 * </p>
 */
public class ScheduledTask implements Task {

    protected long offset;
    protected long period;
    protected PluginContainer owner;
    protected Runnable runnableBody;
    protected long timestamp;
    protected ScheduledTaskState state;
    protected UUID id;
    protected String name;
    protected TaskSynchroncity syncType;

    // Internal Task state. Not for user-service use.
    public enum ScheduledTaskState {
        WAITING,
        RUNNING,
        CANCELED,
    }

    // No c'tor without arguments.  This prevents internal Sponge code from accidentally trying to
    // instantiate a ScheduledTask incorrectly.
    @SuppressWarnings("unused")
    private ScheduledTask() {
    }

    // This c'tor is OK for internal Sponge use. APIs do not expose the c'tor.
    protected ScheduledTask(long x, long t, TaskSynchroncity syncType) {
        // All tasks begin waiting.
        this.state = ScheduledTaskState.WAITING;

        // Values assigned to offset and period are always interpreted by the internal
        // Sponge implementation as in milliseconds for scaleDecriptors that are not Tick based.
        this.offset = x;
        this.period = t;
        this.owner = null;
        this.runnableBody = null;
        this.id = UUID.randomUUID();
        this.syncType = syncType;
    }

    // Builder method
    protected ScheduledTask setState(ScheduledTaskState state) {
        this.state = state;
        return this;
    }

    // Builder method
    protected ScheduledTask setOffset(long x) {
        this.offset = x;
        return this;
    }

    // Builder method
    protected ScheduledTask setPeriod(long t) {
        this.period = t;
        return this;
    }

    // Builder method
    protected ScheduledTask setTimestamp(long ts) {
        this.timestamp = ts;
        return this;
    }

    // Builder method
    protected ScheduledTask setPluginContainer(PluginContainer owner) {
        this.owner = owner;
        return this;
    }

    // Builder method
    protected ScheduledTask setRunnableBody(Runnable body) {
        this.runnableBody = body;
        return this;
    }

    @Override
    public PluginContainer getOwner() {

        return this.owner;
    }

    @Override
    public Optional<Long> getDelay() {
        Optional<Long> result = Optional.absent();
        if (this.offset > 0) {
            result = Optional.of(this.offset);

        }
        return result;
    }

    @Override
    public Optional<Long> getInterval() {
        Optional<Long> result = Optional.absent();

        if (this.period > 0) {
            result = Optional.of(this.period);
        }
        return result;
    }

    @Override
    public boolean cancel() {

        boolean bResult = false;

        // When a task is canceled, it is removed from the map
        // Even if the task is a repeating task, by removing it from the map of tasks
        // known in the Scheduler, the task will not repeat.
        //
        // A successful cancel() occurs when the opportunity is present where
        // the task can be canceled.  If it is, then the result is true.
        // If the task is already canceled, or already running, the task cannot
        // be canceled.

        if (this.state == ScheduledTask.ScheduledTaskState.WAITING) {
            bResult = true;
        }

        this.state = ScheduledTask.ScheduledTaskState.CANCELED;

        return bResult;
    }

    @Override
    public Optional<Runnable> getRunnable() {
        Optional<Runnable> result = Optional.absent();
        if (this.runnableBody != null) {
            result = Optional.of(this.runnableBody);
        }
        return result;
    }

    @Override
    public UUID getUniqueId() {

        return this.id;
    }

    @Override
    public Optional<String> getName() {
        Optional<String> result = Optional.absent();
        if (this.name != null) {
            result = Optional.of(this.name);
        }
        return result;
    }

    @Override
    public boolean isSynchronous() {
        return this.syncType == TaskSynchroncity.SYNCHRONOUS;
    }

    @Override
    public String setName(String name) {

        this.name = name;
        return this.name;
    }

    public enum TaskSynchroncity {
        SYNCHRONOUS,
        ASYNCHRONOUS
    }
}
