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

public final class SchedulerLogMessages {

    public static final String CANNOT_MAKE_TASK_WARNING = "Task cannot be created.";
    public static final String INTERVAL_NEGATIVE_ERROR = "The interval (period) of the Task is negative.";
    public static final String DELAY_NEGATIVE_ERROR = "The delay (offset) of the Task is negative.";
    public static final String PLUGIN_CONTAINER_NULL_WARNING = "The Scheduler could not create the Task because the PluginContainer was null.";
    public static final String PLUGIN_CONTAINER_INVALID_WARNING =
            "The Task cannot be created because the plugin was not derived from a PluginContainer";
    public static final String NULL_RUNNABLE_ARGUMENT_WARNING = "The Task cannot be created because the Runnable argument is null.";
    public static final String NULL_RUNNABLE_ARGUMENT_INVALID_WARNING =
            "The Task could not be created. The Runnable argument is not derived from Runnable.";
    public static final String USER_TASK_FAILED_TO_RUN_ERROR = "The Scheduler tried to run the Task, but the Runnable could not be started.";
    public static final String USER_TASK_TO_RUN_WAS_NULL_WARNING =
            "The Scheduler tried to run the Task, but the Task is null. The Task did not start.";
    public static final String CATASTROPHIC_ERROR_IN_SCHEDULER_SEEK_HELP =
            "The scheduler internal state machine suffered a catastrophic error.  Check #spongedev";

    @Override
    public String toString() {

        return "SchedulerLogMessage";
    }
}
