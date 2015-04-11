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
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.mod.SpongeMod;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Asynchronous Scheduler
 * </p>
 *
 * <p>
 * The Asynchronous Scheduler is similar to the
 * {@link org.spongepowered.api.service.scheduler.SynchronousScheduler}. They
 * have the same kind of API signature and operate with the same kind of Tasks.
 * The exceptional difference between the SyncScheduler and this Asynchronous
 * Scheduler is that this Scheduler will base timing of Tasks on wall-clock
 * time. The wall-clock time is used to determine when to run a Task.
 * </p>
 *
 * <p>
 * Main Differences
 * </p>
 *
 * <p>
 * {@link SyncScheduler} implements
 * {@link org.spongepowered.api.service.scheduler.SynchronousScheduler}
 * interface and uses Ticks as the time unit. Tasks are created with parameters
 * (if any) that involve delays and periods based on Tick time units.
 * Theoretically a Tick is 50ms, but due to overhead and latency, the monotonous
 * timing of Tick based Tasks is not guaranteed.
 * </p>
 *
 * <p>
 * {@link AsyncScheduler} implements {@link AsynchronousScheduler} interface and
 * uses milliseconds as the time unit. Tasks created with parameters (if any)
 * that involve delays and periods based on milliseconds and relative to the
 * wall-clock of the host system. Caveat: the wall-clock time is used for
 * measuring elapsed time, but the actual date/time of the host system or
 * changes to the host date/time will not affect the scheduling of Tasks.
 * Because the AsyncScheduler is running its own thread all tasks that execute
 * from this scheduler are not synchronous with the game data. In other-words,
 * the Tasks that are executed by the AsyncScheduler are not thread-safe with
 * the game data. Plugin authors should use care when leveraging concurrency in
 * their plugins when those plugins access game data as a result of a
 * Asynchronous {@link Task} running. Several resources can help the plugin
 * author: The Java Concurrency book by Lea and Effective Java by Bloch for
 * information handling concurrency issues in their plugins.
 * </p>
 * *
 * <p>
 * Tasks can be created using the API in the {@link AsynchronousScheduler}
 * interface. The access to this Scheduler through the
 * {@link org.spongepowered.api.Game} interface method AsynchronousScheduler
 * getAsyncScheduler(); Plugin authors never cause the creation of a scheduler,
 * the scheduler (Asynchronous and {@link SyncScheduler} are each Singletons.
 * </p>
 *
 *
 */
public class AsyncScheduler implements AsynchronousScheduler {

    // The simple private map of all pending (and running) ScheduledTasks
    private final Map<UUID, ScheduledTask> taskMap = new ConcurrentHashMap<UUID, ScheduledTask>();
    // Adjustable timeout for pending Tasks
    private long minimumTimeout = Long.MAX_VALUE;
    private long lastProcessingTimestamp;
    // Locking mechanism
    private final Lock lock = new ReentrantLock();
    private final Condition condition = this.lock.newCondition();
    // The dynamic thread pooling executor of asynchronous tasks.
    private ExecutorService executor;
    // Query actor for task information
    private SchedulerHelper schedulerHelper;

    private AsyncScheduler() {
        this.schedulerHelper = new SchedulerHelper(ScheduledTask.TaskSynchroncity.ASYNCHRONOUS);

        new Thread(new Runnable() {

            @Override
            public void run() {
                stateMachineBody();
            }
        }).start();
    }

    private void stateMachineBody() {
        this.executor = Executors.newCachedThreadPool();
        this.lastProcessingTimestamp = System.currentTimeMillis();
        while (true) {
            recalibrateMinimumTimeout();
            processTasks();
        }
    }

    private static class AsynchronousSchedulerSingletonHolder {

        private static final AsynchronousScheduler INSTANCE = new AsyncScheduler();
    }

    /**
     * <p>
     * Returns the instance (handle) to the Asynchronous TaskScheduler.
     * </p>
     *
     * <p>
     * A static reference to the Asynchronous Scheduler singleton is returned by
     * the function getInstance().
     * </p>
     * <p>
     * Singleton based on: <a href
     * ="http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">
     * Initialization on Demand Idiom</a>
     * </p>
     *
     * @return The single interface to the Asynchronous Scheduler
     */
    public static AsynchronousScheduler getInstance() {
        return AsynchronousSchedulerSingletonHolder.INSTANCE;
    }

    private void recalibrateMinimumTimeout() {
        this.lock.lock();
        try {
            for (ScheduledTask task : this.taskMap.values()) {
                // Tasks may have dropped off the list.
                // Whatever Tasks remain, recalibrate:
                //
                // Recalibrate the wait delay for processing tasks before new tasks
                // cause the scheduler to process pending tasks.
                this.minimumTimeout = Long.MAX_VALUE;
                if (task.offset == 0 && task.period == 0) {
                    this.minimumTimeout = 0;
                } else if (task.offset > 0 && task.period == 0) {
                    // task with non-zero offset, zero period
                    this.minimumTimeout = Math.min(task.offset, this.minimumTimeout);
                } else if (task.offset == 0 && task.period > 0) {
                    // task with zero offset, non-zero period
                    this.minimumTimeout = Math.min(task.period, this.minimumTimeout);
                } else if (task.offset > 0 && task.period > 0) {
                    // task with non-zero offset, non-zero period
                    this.minimumTimeout = Math.min(task.offset, this.minimumTimeout);
                    this.minimumTimeout = Math.min(task.period, this.minimumTimeout);
                }
            }

            // If no tasks remain, recalibrate to max timeout
            if (this.taskMap.isEmpty()) {
                this.minimumTimeout = Long.MAX_VALUE;
            } else {
                long latency = System.currentTimeMillis() - this.lastProcessingTimestamp;
                this.minimumTimeout -= (latency <= 0) ? 0 : latency;
                this.minimumTimeout = (this.minimumTimeout < 0) ? 0 : this.minimumTimeout;
            }
        } finally {
            this.lock.unlock();
        }
    }

    private void processTasks() {
        this.lock.lock();
        try {
            try {
                this.condition.await(this.minimumTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // The taskMap has been modified; there is work to do.
                // Continue on without handling the Exception.
            } catch (IllegalMonitorStateException e) {
                SpongeMod.instance.getLogger().error(SchedulerLogMessages.CATASTROPHIC_ERROR_IN_SCHEDULER_SEEK_HELP);
                SpongeMod.instance.getLogger().error(e.toString());
            }

            // We've locked down the taskMap but the lock is short lived if the size of the
            // size of the list is zero.
            //
            // For each task, inspect the state.
            //
            // For the state of CANCELED, remove it and look at the next task, if any.
            //
            // For the state of WAITING, the task has not begun, so check the
            // offset time before starting that task for the first time.
            //
            // Else if the task is already RUNNING, use the period (the time delay until
            // the next moment to run the task).
            //
            for (ScheduledTask task : this.taskMap.values()) {
                // If the task is now slated to be canceled, we just remove it as if it no longer exists.
                if (task.state == ScheduledTask.ScheduledTaskState.CANCELED) {
                    this.taskMap.remove(task.getUniqueId());
                    continue;
                }

                long threshold = Long.MAX_VALUE;

                // Figure out if we start a delayed Task after threshold milliseconds or,
                // start it after the interval (period) of the repeating task parameter.
                if (task.state == ScheduledTask.ScheduledTaskState.WAITING) {
                    threshold = task.offset;
                } else if (task.state == ScheduledTask.ScheduledTaskState.RUNNING) {
                    threshold = task.period;
                }

                // This moment is 'now'
                long now = System.currentTimeMillis();

                // So, if the current time minus the timestamp of the task is greater than
                // the delay to wait before starting the task, then start the task.

                // Repeating tasks get a reset-timestamp each time they are set RUNNING
                // If the task has a period of 0 (zero) this task will not repeat, and is removed
                // after we start it.

                if (threshold <= (now - task.timestamp)) {
                    // startTask is just a utility function within the Scheduler that
                    // starts the task.
                    // If the task is not a one time shot then keep it and
                    // change the timestamp to now.  It is a little more
                    // efficient to do this now than after starting the task.
                    task.timestamp = System.currentTimeMillis();
                    boolean bTaskStarted = startTask(task);
                    if (bTaskStarted) {
                        task.setState(ScheduledTask.ScheduledTaskState.RUNNING);
                        // If task is one time shot, remove it from the list.
                        if (task.period == 0L) {
                            this.taskMap.remove(task.getUniqueId());
                        }
                    }
                }
            }
            this.lastProcessingTimestamp = System.currentTimeMillis();
        } finally {
            this.lock.unlock();
        }
    }

    private Optional<Task> utilityForAddingAsyncTask(ScheduledTask task) {
        Optional<Task> resultTask = Optional.absent();

        task.setTimestamp(System.currentTimeMillis());
        this.lock.lock();
        try {
            this.taskMap.put(task.getUniqueId(), task);
            this.condition.signalAll();
            resultTask = Optional.of((Task) task);
        } finally {
            this.lock.unlock();
        }

        return resultTask;
    }

    /**
     * <p>
     * Runs a Task once immediately.
     * </p>
     *
     * <p>
     * The runTask method is used to run a single Task just once. The Task may
     * persist for the life of the server, however the Task itself will never be
     * restarted. It has no delay offset. This Asynchronous Scheduler will not
     * wait before running the Task.
     * </p>
     *
     * <p>
     * Example code to obtain plugin container argument from User code:
     * </p>
     *
     * <p>
     *
     * <pre>
     * <code>
     *     Optional&lt;PluginContainer&gt; result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </pre>
     *
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param runnableTarget The Runnable object that implements a run() method
     *        to execute the Task desired
     * @return Optional&lt;Task&gt; Either Optional.absent() if invalid or a
     *         reference to the new Task
     */
    @Override
    public Optional<Task> runTask(Object plugin, Runnable runnableTarget) {
        //
        // The intent of this method is to run a single task (non-repeating) and has zero
        // offset (doesn't wait a delay before starting), and a zero period (no repetition)</p>
        Optional<Task> resultTask = Optional.absent();
        final long noDelay = 0L;
        final long noPeriod = 0L;

        ScheduledTask nonRepeatingTask = this.schedulerHelper.taskValidationStep(plugin, runnableTarget, noDelay, noPeriod);

        if (nonRepeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        } else {
            resultTask = utilityForAddingAsyncTask(nonRepeatingTask);
        }

        return resultTask;
    }

    /**
     * <p>
     * Runs a Task once after a specific delay offset.
     * </p>
     *
     * <p>
     * The runTask() method is used to run a single Task just once. The Task may
     * persist for the life of the server, however the Task itself will never be
     * restarted. This Asynchronous Scheduler will not wait before running the
     * Task. <b>The Task will be delayed artificially for delay in the time unit
     * scale.</b>
     * </p>
     *
     * <p>
     * Because the time unit is in milliseconds, this scheduled Task is
     * asynchronous with the game. The timing of when to run a Task is based on
     * wall-clock time. Overhead, network and system latency not withstanding
     * the event will fire after the delay expires.
     * </p>
     *
     * <p>
     * Example code to obtain plugin container argument from User code:
     * </p>
     *
     * <p>
     *
     * <pre>
     * <code>
     *     Optional&lt;PluginContainer&gt; result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </pre>
     *
     * </p>
     *
     * <p>
     * Example for specifying a certain time unit scale:
     * </p>
     *
     * <p>
     *
     * <pre>
     *     <code>
     *         // The task will run with a delay of 500 seconds.
     *         runTaskAfter(somePlugin, someRunnableTarget, TimeUnit.MILLISECONDS, 500);
     *     </code>
     * </pre>
     *
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param runnableTarget The Runnable object that implements a run() method
     *        to execute the Task desired
     * @param delay The offset in scale units before running the task.
     * @return Optional&lt;Task&gt; Either Optional.absent() if invalid or a
     *         reference to the new Task
     */
    @Override
    public Optional<Task> runTaskAfter(Object plugin, Runnable runnableTarget, TimeUnit scale, long delay) {
        Optional<Task> resultTask = Optional.absent();
        final long noPeriod = 0L;

        // The delay passed to this method is converted to the number of milliseconds
        // per the scale of the time unit.
        delay = scale.toMillis(delay);

        ScheduledTask nonRepeatingTask = this.schedulerHelper.taskValidationStep(plugin, runnableTarget, delay, noPeriod);

        if (nonRepeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        } else {
            resultTask = utilityForAddingAsyncTask(nonRepeatingTask);
        }

        return resultTask;
    }

    /**
     * <p>
     * Start a repeating Task with a period in specified time unit The first
     * occurrence will start immediately.
     * </p>
     *
     * <p>
     * The runRepeatingTask() method is used to run a Task that repeats. The
     * Task may persist for the life of the server. This Asynchronous Scheduler
     * will not wait before running the first occurrence of the Task. This
     * Scheduler will not allow a second occurrence of the task to start if the
     * preceding occurrence is is still alive. Be sure to end the Runnable
     * Thread of the Task before anticipating the recurrence of the Task.
     * </p>
     *
     * <p>
     * If this Scheduler detects that two tasks will overlap as such, the 2nd
     * Task will not be started. The next time the Task is due to run, the test
     * will be made again to determine if the previous occurrence of the Task is
     * still alive (running). As long as a previous occurrence is running no new
     * occurrences of that specific Task will start, although this Scheduler
     * will never cease in trying to start it a 2nd time.
     * </p>
     *
     * <p>
     * Because the time unit is in the scale provided, this scheduled Task is
     * asynchronous with the game. The timing of when to run a Task is based on
     * wall-clock time. Overhead, network and system latency not withstanding
     * the event will fire after the delay expires.
     * </p>
     *
     * <p>
     * Example code to obtain plugin container argument from User code:
     * </p>
     *
     * <p>
     *
     * <pre>
     * <code>
     *     Optional&lt;PluginContainer&gt; result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </pre>
     *
     * </p>
     *
     * <p>
     * Example for specifying a certain time unit scale:
     * </p>
     *
     * <p>
     *
     * <pre>
     *     <code>
     *         // The task will run with a period of 15 seconds.
     *         runRepeatingTask(somePlugin, someRunnableTarget, TimeUnit.SECONDS, 15);
     *     </code>
     * </pre>
     *
     * </p>
     *
     * <p>
     * Example for specifying a certain time unit scale:
     * </p>
     *
     * <p>
     *
     * <pre>
     *     <code>
     *         // The task will run with a period of 30 seconds
     *         runTaskAfter(somePlugin, someRunnableTarget, TimeUnit.SECONDS, 30);
     *     </code>
     * </pre>
     *
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param runnableTarget The Runnable object that implements a run() method
     *        to execute the Task desired
     * @param scale The TimeUnit scale of the interval argument.
     * @param interval The period in scale time units of the repeating Task.
     * @return Optional&lt;Task&gt; Either Optional.absent() if invalid or a
     *         reference to the new Task
     */
    @Override
    public Optional<Task> runRepeatingTask(Object plugin, Runnable runnableTarget, TimeUnit scale, long interval) {
        Optional<Task> resultTask = Optional.absent();
        final long noDelay = 0L;

        // The interval passed to this method is converted to the number of milliseconds
        // per the scale of the time unit.
        interval = scale.toMillis(interval);
        ScheduledTask repeatingTask = this.schedulerHelper.taskValidationStep(plugin, runnableTarget, noDelay, interval);

        if (repeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        } else {
            resultTask = utilityForAddingAsyncTask(repeatingTask);
        }

        return resultTask;
    }

    /**
     * <p>
     * Start a repeating Task with a period (interval) of time unit scale. The
     * first occurrence will start after an initial delay in time unit scale.
     * </p>
     *
     * <p>
     * The runRepeatingTask method is used to run a Task that repeats. The Task
     * may persist for the life of the server. This Asynchronous Scheduler
     * <b>will wait</b> before running the first occurrence of the Task. This
     * Scheduler will not allow a second occurrence of the task to start if the
     * preceding occurrence is is still alive. Be sure to end the Runnable
     * Thread of the Task before anticipating the recurrence of the Task.
     * </p>
     *
     * <p>
     * If this Scheduler detects that two tasks will overlap as such, the 2nd
     * Task will not be started. The next time the Task is due to run, the test
     * will be made again to determine if the previous occurrence of the Task is
     * still alive (running). As long as a previous occurrence is running no new
     * occurrences of that specific Task will start, although this Scheduler
     * will never cease in trying to start it a 2nd time.
     * </p>
     *
     * <p>
     * Because the time unit is in milliseconds, this scheduled Task is
     * asynchronous with the game. The timing of when to run a Task is based on
     * wall-clock time. Overhead, network and system latency not withstanding
     * the event will fire after the delay expires.
     * </p>
     *
     * <p>
     * Example code to obtain plugin container argument from User code:
     * </p>
     *
     * <p>
     *
     * <pre>
     * <code>
     *     Optional&lt;PluginContainer&gt; result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </pre>
     *
     * </p>
     *
     * <p>
     * Example for specifying a certain time unit scale:
     * </p>
     *
     * <p>
     *
     * <pre>
     *     <code>
     *         // The task will run with a period of 15 seconds.
     *         runRepeatingTask(somePlugin, someRunnableTarget, TimeUnit.SECONDS, 15);
     *     </code>
     * </pre>
     *
     * </p>
     *
     * <p>
     * Example for specifying a certain time unit scale:
     * </p>
     *
     * <p>
     *
     * <pre>
     *     <code>
     *         // The task will run with a period of 120 milliseconds and delay of 302 milliseconds
     *         // (If the scales are the same for both arguments)
     *         runRepeatingTaskAfter(somePlugin, someRunnableTarget, TimeUnit.MILLISECONDS, 120, 302);
     *
     *         // If the two units are in different scales:
     *
     *         // The task will run with a period of 20 seconds and delay of 500 milliseconds:
     *         Either:
     *         runRepeatingTaskAfter(somePlugin, someRunnableTarget, TimeUnit.MILLISECONDS, TimeUnit.SECOND.toMillis(20), 500);
     *         // OR
     *         runRepeatingTaskAfter(somePlugin, someRunnableTarget, TimeUnit.SECONDS, 20, TimeUnit.MILLISECONDS.toSeconds(500));
     *
     *     </code>
     * </pre>
     *
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param runnableTarget The Runnable object that implements a run() method
     *        to execute the Task desired
     * @param scale Time unit used to specify {@code interval} and {@code delay}
     * @param delay The offset in time unit scale before running the task.
     * @param interval The offset in time unit scale before running the task.
     * @return Optional&lt;Task&gt; Either Optional.absent() if invalid or a
     *         reference to the new Task
     */
    @Override
    public Optional<Task> runRepeatingTaskAfter(Object plugin, Runnable runnableTarget, TimeUnit scale, long interval, long delay) {
        Optional<Task> resultTask = Optional.absent();

        // The interval and delay passed to this method is converted to the number of milliseconds
        // per the scale of the time unit.
        interval = scale.toMillis(interval);
        delay = scale.toMillis(delay);
        ScheduledTask repeatingTask = this.schedulerHelper.taskValidationStep(plugin, runnableTarget, delay, interval);

        if (repeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        } else {
            resultTask = utilityForAddingAsyncTask(repeatingTask);
        }

        return resultTask;
    }

    /**
     *
     * <p>
     * Get the task from the Scheduler identified by the UUID
     * </p>
     *
     * <p>
     * Example code to use the method:
     * </p>
     *
     * <p>
     *
     * <pre>
     * <code>
     *     UUID myID;
     *     // ...
     *     Optional&lt;Task&gt; task;
     *     task = AsyncScheduler.getInstance().getTaskById(myID);
     * </code>
     * </pre>
     *
     * </p>
     *
     * @param id The UUID of the Task to find.
     * @return Optional&lt;Task&gt; Either Optional.absent() if invalid or a
     *         reference to the existing Task.
     */
    @Override
    public Optional<Task> getTaskById(UUID id) {
        Optional<Task> resultTask = Optional.absent();

        Task tmpTask = this.taskMap.get(id);

        if (tmpTask != null) {
            resultTask = Optional.of(tmpTask);
        }
        return resultTask;
    }

    @Override
    public Optional<UUID> getUuidOfTaskByName(String name) {
        return this.schedulerHelper.getUuidOfTaskByName(this.taskMap, name);
    }

    @Override
    public Collection<Task> getTasksByName(String pattern) {
        return this.schedulerHelper.getfTasksByName(this.taskMap, pattern);
    }

    @Override
    public Collection<Task> getScheduledTasks() {
        return this.schedulerHelper.getScheduledTasks(this.taskMap);
    }

    @Override
    public Collection<Task> getScheduledTasks(Object plugin) {
        return this.schedulerHelper.getScheduledTasks(this.taskMap, plugin);
    }

    private boolean startTask(ScheduledTask task) {
        // We'll succeed unless there's an exception found when we try to start the
        // actual Runnable target.
        boolean bRes = true;
        try {
            this.executor.submit(task.runnableBody);
        } catch (Exception ex) {
            SpongeMod.instance.getLogger().error(SchedulerLogMessages.USER_TASK_FAILED_TO_RUN_ERROR);
            SpongeMod.instance.getLogger().error(ex.toString());
            bRes = false;

        }
        return bRes;
    }
}
