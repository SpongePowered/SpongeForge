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
    private String name;

    // Internal Task state. Not for user-service use.
    protected enum ScheduledTaskState {
        WAITING,
        RUNNING,
        CANCELED,
    }

    // No c'tor without arguments.
    @SuppressWarnings("unused")
    private ScheduledTask() {
    }

    // This c'tor is OK.
    protected ScheduledTask(long x, long t) {
        this.offset = x;
        this.period = t;
        this.owner = null;
        this.runnableBody = null;
        this.state = ScheduledTaskState.WAITING;
        this.id = UUID.randomUUID();

        //TBD
        this.name = this.id.toString();
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
        if ( this.offset > 0 ) {
            result = Optional.of(new Long(this.offset));

        }
        return result;
    }

    @Override
    public Optional<Long> getInterval() {
        Optional<Long> result = Optional.absent();
        if ( this.period > 0 ) {
            result = Optional.of(new Long(this.period));

        }
        return result;
    }

    @Override
    public boolean cancel() {

        boolean bResult = true;

        // When a task is canceled, it is removed from the list
        // Even if the task is a repeating task, by removing it from the list of tasks
        // known in the Scheduler, the task will not repeat.

        this.state = ScheduledTask.ScheduledTaskState.CANCELED;

        // TODO -- possibly also release other resources (?) like the Runnable thread body?
        // etc..
        // bResult = true;

        return bResult;
    }

    @Override
    public Runnable getRunnable() {
        return this.runnableBody;
    }

    @Override
    public UUID getUniqueId() {
        return this.id;
    }

    @Override
    public Optional<String> getName() {
        Optional<String> result = Optional.absent();
        if ( this.name != null)  {
            result = Optional.of(this.name);
        }
        return result;
    }
}
