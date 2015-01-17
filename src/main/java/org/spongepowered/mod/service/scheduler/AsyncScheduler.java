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
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.mod.SpongeMod;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Asynchronous Scheduler</p>
 *
 * <p>The Asynchronous Scheduler is similar to the {@Link SyncScheduler}.  They have the same kind of API signature
 * and operate with the same kind of Tasks.  The exceptional difference between the SyncScheduler and this
 * Asynchronous Scheduler is that this Scheduler will base timing of Tasks on wall-clock time.  The wall-clock
 * time is used to determine when to run a Task.</p>
 *
 * <p>Main Differences</p>
 *
 * <p>{@Link SyncScheduler} implements {@Link Scheduler} interface and uses Ticks as the time unit. Tasks are
 * created with parameters (if any) that involve delays and periods based on Tick time units. Theoretically a Tick
 * is 50ms, but due to overhead and latency, the monotonous timing of Tick based Tasks is not guaranteed.</p>
 *
 * <p> {@Link AsyncScheduler} implements {@Link AsynchronousScheduler} interface and uses milliseconds as the time
 * unit.  Tasks created with parameters (if any) that involve delays and periods based on milliseconds and relative
 * to the wall-clock of the host system.  Caveat: the wall-clock time is used for measuring elapsed time, but the
 * actual date/time of the host system or changes to the host date/time will not affect the scheduling of Tasks.
 * Because the AsyncScheduler is running its own thread all tasks that  execute from this scheduler are not
 * synchronous with the game data.  In other-words, the Tasks that are executed by the AsyncScheduler are not
 * thread-safe with the game data.  Plugin authors should use care when leveraging concurrency in their plugins
 * when those plugins access game data as a result of a Asynchronous {@Link Task} running.  Several resources can
 * help the plugin author:  The Java Concurrency book by Lea and Effective Java by Bloch for information handling
 * concurrency issues in their plugins.</p>
 * *
 * <p>Tasks can be created using the API in the {@Link AsynchronousScheduler} interface. The access to this Scheduler
 * through the {@Link Game} interface method    AsynchronousScheduler getAsyncScheduler();  Plugin authors never
 * cause the creation of a scheduler, the scheduler (Asynchronous and {@Link SyncScheduler} are each Singletons.</p>
 *
 *
 */
public class AsyncScheduler implements AsynchronousScheduler {

    // Singleton reference for this Scheduler.
    private final static AtomicReference<AsynchronousScheduler> instance = new AtomicReference<AsynchronousScheduler>();
    // Simple Moore State machine state. (http://en.wikipedia.org/wiki/Moore_machine)
    private MachineState sm;
    // The simple private queue of all pending (and running) ScheduledTasks
    private final Queue<ScheduledTask> taskList = new ConcurrentLinkedQueue<ScheduledTask>();
    // Adjustable timeout for pending Tasks
    private long minimumTimeout = Long.MAX_VALUE;
    private long lastProcessingTimestamp;
    // Locking mechanism
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();


    private AsyncScheduler() {
        new Thread(new Runnable() {
            public void run() {
                SMachine();
            }
        }).start();
    }

    /**
     * <p>Unlike the State of a ScheduledTask, the @MachineState of this Asynchronous Scheduler is private and not an
     * enumeration of values that have any user facing purpose.  This is why the enumeration is
     * defined within the class and inaccessible to user code.</p>
     */
    private enum MachineState {
        PRE_INIT,
        INIT,
        RUN,
        NOT_RUNNING,
    }

    // The Moore State Machine for this Scheduler
    private void SMachine() {

        sm = MachineState.PRE_INIT;
        while ( sm != MachineState.NOT_RUNNING ) {
            switch ( sm ) {
                case PRE_INIT: {
                    // TODO - Pre-Initialization ?
                    sm = MachineState.INIT;
                    break;
                }
                case INIT: {
                    // TODO - Initialization ?
                    lastProcessingTimestamp = System.currentTimeMillis();
                    sm = MachineState.RUN;
                    break;
                }
                case RUN: {
                    recalibrateMinimumTimeout();
                    // We're in the RUN state -- process all the tasks, forever.
                    ProcessTasks();
                    break;
                }
            }
        }
    }

    /**
     * <p>Returns the instance (handle) to the Asynchronous TaskScheduler.</p>
     *
     * <p>
     * A static reference to the TaskScheduler singleton is returned by
     * the function getInstance().  The implementation of getInstance follows the usage
     * of the AtomicReference idiom.</p>
     *
     * @return The single interface to the Asynchronous Scheduler
     */
    public static AsynchronousScheduler getInstance() {
        AsynchronousScheduler inst = instance.get();
        if (inst == null) {
            instance.set(inst = new AsyncScheduler());
        }
        return inst;
    }


    private void recalibrateMinimumTimeout() {
        lock.lock();
        try {
            for (ScheduledTask task : this.taskList) {
                // Tasks may have dropped off the list.
                // Whatever Tasks remain, recalibrate:
                //
                // Recalibrate the wait delay for processing tasks before new tasks
                // cause the scheduler to process pending tasks.
                minimumTimeout = Long.MAX_VALUE;
                if (task.offset == 0 && task.period == 0) {
                    minimumTimeout = 0;
                }
                // task with non-zero offset, zero period
                else if (task.offset > 0 && task.period == 0) {
                    minimumTimeout = (task.offset < minimumTimeout) ? task.offset : minimumTimeout;
                }
                // task with zero offset, non-zero period
                else if (task.offset == 0 && task.period > 0) {
                    minimumTimeout = (task.period < minimumTimeout) ? task.period : minimumTimeout;
                }
                // task with non-zero offset, non-zero period
                else if (task.offset > 0 && task.period > 0) {
                    minimumTimeout = (task.offset < minimumTimeout) ? task.offset : minimumTimeout;
                    minimumTimeout = (task.period < minimumTimeout) ? task.period : minimumTimeout;
                }
            }

            // If no tasks remain, recalibrate to max timeout
            if (taskList.isEmpty()) {
                minimumTimeout = Long.MAX_VALUE;
            }
            else {
                long latency = System.currentTimeMillis() - lastProcessingTimestamp;
                minimumTimeout -= (latency <= 0) ? 0 : latency;
                minimumTimeout = (minimumTimeout < 0) ? 0 : minimumTimeout;
            }
        } finally {
            lock.unlock();
        }
    }

    private void ProcessTasks() {
        lock.lock();
        try {
            try {
                condition.await(minimumTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // The taskList has been modified; there is work to do.
                // Continue on without handling the Exception.
            } catch (IllegalMonitorStateException e) {
                SpongeMod.instance.getLogger().error(SchedulerLogMessages.CATASTROPHIC_ERROR_IN_SCHEDULER_SEEK_HELP);
                SpongeMod.instance.getLogger().error(e.toString());
            }

            // We've locked down the taskList but the lock is short lived if the size of the
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
            for (ScheduledTask task : this.taskList) {
                // If the task is now slated to be canceled, we just remove it as if it no longer exists.
                if (task.state == ScheduledTask.ScheduledTaskState.CANCELED) {
                    this.taskList.remove(task);
                    continue;
                }

                long threshold = Long.MAX_VALUE;

                // Figure out if we start a delayed Task after threshold ticks or,
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

                if  (threshold <= (now - task.timestamp)) {
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
                            this.taskList.remove(task);
                        }
                    }
                }
            }
            lastProcessingTimestamp = System.currentTimeMillis();
        } finally {

            lock.unlock();
        }
    }

    private Optional<Task> utilityForAddingTask(ScheduledTask task) {
        Optional<Task> result = Optional.absent();

        if ( task != null ) {
            task.setTimestamp(System.currentTimeMillis());
            lock.lock();
            try {
                this.taskList.add(task);
                condition.signalAll();
                result = Optional.of((Task) task);
            }
            finally {
                lock.unlock();
            }
        }
        return result;
    }

    /**
     * <p>Runs a Task once immediately.</p>
     *
     * <p>
     * The runTask method is used to run a single Task just once.  The Task
     * may persist for the life of the server, however the Task itself will never
     * be restarted.  It has no delay offset.  This Asynchronous Scheduler will not wait before
     * running the Task.<p>
     *
     * <p>Example code to obtain plugin container argument from User code:</p>
     *
     * <p>
     * <code>
     *     Optional<PluginContainer> result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param task  The Runnable object that implements a run() method to execute the Task desired
     * @return Optional<Task> Either Optional.absent() if invalid or a reference to the new Task
     */
    @Override
    public Optional<Task> runTask(Object plugin, Runnable task) {
        /**
         * <p>
         * The intent of this method is to run a single task (non-repeating) and has zero
         * offset (doesn't wait a delay before starting), and a zero period (no repetition)</p>
         */
        Optional<Task> result = Optional.absent();
        final long NODELAY = 0L;
        final long NOPERIOD = 0L;

        ScheduledTask nonRepeatingTask = taskValidationStep(plugin, task, NODELAY, NOPERIOD);

        if (nonRepeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        }
        else {
            result = utilityForAddingTask(nonRepeatingTask);
        }

        return result;
    }

    /**
     * <p>Runs a Task once after a specific delay offset.</p>
     *
     * <p>
     * The runTask() method is used to run a single Task just once.  The Task
     * may persist for the life of the server, however the Task itself will never
     * be restarted.  This Asynchronous Scheduler will not wait before running the Task.
     * <b>The Task will be delayed artificially for delay Ticks.</b>  </p>
     *
     * <p>Because the time unit is in milliseconds, this scheduled Task is asynchronous with the game.
     * The timing of when to run a Task is based on wall-clock time.
     * Overhead, network and system latency not
     * withstanding the event will fire after the delay expires.</p>
     *
     * <p>Example code to obtain plugin container argument from User code:</p>
     *
     * <p>
     * <code>
     *     Optional<PluginContainer> result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param task  The Runnable object that implements a run() method to execute the Task desired
     * @param delay  The offset in ticks before running the task.
     * @return Optional<Task> Either Optional.absent() if invalid or a reference to the new Task
     */
    @Override
    public Optional<Task> runTaskAfter(Object plugin, Runnable task, long delay) {
        Optional<Task> result = Optional.absent();
        final long NOPERIOD = 0L;

        ScheduledTask nonRepeatingTask = taskValidationStep(plugin, task, delay, NOPERIOD);

        if (nonRepeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        }
        else {
            result = utilityForAddingTask(nonRepeatingTask);
        }

        return result;
    }

    /**
     * <p>Start a repeating Task with a period (interval) of Ticks.  The first occurrence will start immediately.</p>
     *
     * <p>
     * The runRepeatingTask() method is used to run a Task that repeats.  The Task
     * may persist for the life of the server. This Asynchronous Scheduler will not wait before running
     * the first occurrence of the Task. This Scheduler will not allow a second occurrence of
     * the task to start if the preceding occurrence is is still alive.  Be sure to end
     * the Runnable Thread of the Task before anticipating the recurrence of the Task.</p>
     *
     * <p>
     * If this Scheduler detects that two tasks will overlap as such, the 2nd Task will not
     * be started.  The next time the Task is due to run, the test will be made again to determine
     * if the previous occurrence of the Task is still alive (running).  As long as a previous occurrence
     * is running no new occurrences of that specific Task will start, although this Scheduler will
     * never cease in trying to start it a 2nd time.</p>
     *
     * <p>Because the time
     * unit is in milliseconds, this scheduled Task is asynchronous with the game.
     * The timing of when to run a Task is based on wall-clock time.
     * Overhead, network and system latency not
     * withstanding the event will fire after the delay expires.</p>
     *
     * <p>Example code to obtain plugin container argument from User code:</p>
     *
     * <p>
     * <code>
     *     Optional<PluginContainer> result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param task  The Runnable object that implements a run() method to execute the Task desired
     * @param interval The period in ticks of the repeating Task.
     * @return Optional<Task> Either Optional.absent() if invalid or a reference to the new Task
     */
    @Override
    public Optional<Task> runRepeatingTask(Object plugin, Runnable task, long interval) {
        Optional<Task> result = Optional.absent();
        final long NODELAY = 0L;

        ScheduledTask repeatingTask = taskValidationStep(plugin, task, NODELAY, interval);

        if (repeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        } else {
            result = utilityForAddingTask(repeatingTask);
        }

        return result;
    }

    /**
     * <p>
     * Start a repeating Task with a period (interval) of Ticks.
     * The first occurrence will start after an initial delay.</p>
     *
     * <p>
     * The runRepeatingTask method is used to run a Task that repeats.  The Task
     * may persist for the life of the server. This Asynchronous Scheduler <b>will wait</b> before running
     * the first occurrence of the Task. This Scheduler will not allow a second occurrence of
     * the task to start if the preceding occurrence is is still alive.  Be sure to end
     * the Runnable Thread of the Task before anticipating the recurrence of the Task.</p>
     *
     * <p>
     * If this Scheduler detects that two tasks will overlap as such, the 2nd Task will not
     * be started.  The next time the Task is due to run, the test will be made again to determine
     * if the previous occurrence of the Task is still alive (running).  As long as a previous occurrence
     * is running no new occurrences of that specific Task will start, although this Scheduler will
     * never cease in trying to start it a 2nd time.</p>
     *
     * <p>Because the time unit is in milliseconds, this scheduled Task is asynchronous with the game.
     * The timing of when to run a Task is based on wall-clock time.
     * Overhead, network and system latency not
     * withstanding the event will fire after the delay expires.</p>
     *
     * <p>Example code to obtain plugin container argument from User code:</p>
     *
     * <p>
     * <code>
     *     Optional<PluginContainer> result;
     *     result = evt.getGame().getPluginManager().getPlugin("YOUR_PLUGIN");
     *     PluginContainer pluginContainer = result.get();
     * </code>
     * </p>
     *
     * @param plugin The plugin container of the Plugin that initiated the Task
     * @param task  The Runnable object that implements a run() method to execute the Task desired
     * @param delay  The offset in ticks before running the task.
     * @param interval The offset in ticks before running the task.
     * @return Optional<Task> Either Optional.absent() if invalid or a reference to the new Task
     */
    @Override
    public Optional<Task> runRepeatingTaskAfter(Object plugin, Runnable task, long interval, long delay) {
        Optional<Task> result = Optional.absent();

        ScheduledTask repeatingTask = taskValidationStep(plugin, task, delay, interval);

        if (repeatingTask == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.CANNOT_MAKE_TASK_WARNING);
        } else {
            result = utilityForAddingTask(repeatingTask);
        }

        return result;
    }

    /**
     * <p>
     * Start a repeating Task with a period (interval) of Ticks.  The first occurrence
     * will start after an initial delay.</p>
     *
     * <p>Example code to use the method:</p>
     *
     * <p>
     * <code>
     *     UUID myID;
     *     // ...
     *     Optional<Task> task;
     *     task = SyncScheduler.getInstance().getTaskById(myID);
     * </code>
     * </p>
     *
     * @param id The UUID of the Task to find.
     * @return Optional<Task> Either Optional.absent() if invalid or a reference to the existing Task.
     */
    @Override
    public Optional<Task> getTaskById(UUID id) {
        Optional<Task> result = Optional.absent();

        for (ScheduledTask t : taskList) {
            if ( id.equals ( t.id) ) {
                return Optional.of ( (Task) t);
            }
        }
        return result;
    }

    /**
     * <p>Determine the list of Tasks that the TaskScheduler is aware of.</p>
     *
     * @return Collection of all known Tasks in the TaskScheduler
     */
    @Override
    public Collection<Task> getScheduledTasks() {

        Collection<Task> taskCollection;
        synchronized(this.taskList) {
            taskCollection = new ArrayList<Task>(this.taskList);
        }
        return taskCollection;
    }

    /**
     * <p>The query for Tasks owned by a target Plugin owner is found by testing
     * the list of Tasks by testing the ID of each PluginContainer.<p>
     *
     * <p>If the PluginContainer passed to the method is not correct (invalid
     * or null) then return a null reference.  Else, return a Collection of Tasks
     * that are owned by the Plugin.</p>
     * @param plugin The plugin that may own the Tasks in the TaskScheduler
     * @return Collection of Tasks owned by the PluginContainer plugin.
     */
    @Override
    public Collection<Task> getScheduledTasks(Object plugin) {

        // The argument is an Object so we have due diligence to perform...

        // Owner is not a PluginContainer derived class
        if (!PluginContainer.class.isAssignableFrom(plugin.getClass())) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.PLUGIN_CONTAINER_INVALID_WARNING);

            // The plugin owner was not valid, so the "Collection" is empty.
            //(TODO) Perhaps we move this into using Optional<T> to make it explicit that
            // Eg., the resulting Collection is NOT present vs. empty.

            return null;
        }

        // The plugin owner is OK, so let's figure out which Tasks (if any) belong to it.
        // The result Collection represents the Tasks that are owned by the plugin.  The list
        // is non-null.  If no Tasks exists owned by the Plugin, return an empty Collection
        // else return a Collection of Tasks.

        PluginContainer testedOwner = (PluginContainer) plugin;
        String testOwnerID = testedOwner.getId();
        Collection<Task> subsetCollection;

        synchronized(this.taskList) {
            subsetCollection = new ArrayList<Task>(this.taskList);
        }

        Iterator<Task> it = subsetCollection.iterator();

        while (it.hasNext()) {
            String pluginId = ((PluginContainer) it.next()).getId();
            if (!testOwnerID.equals(pluginId)) it.remove();
        }

        return subsetCollection;
    }

    /**
     * The taskValidationStep is an internal method to validate the requested task.
     *
     * <p>The taskValidationStep looks at the requested Task, the parameters passed, and
     * then if everything is OK, will proceed with creating a non-timestamped task.  The task
     * isn't added to the List of tasks yet.  After the validation step, the Task merely exists
     * loose unattached to any list.  Only the specific method to run a task will bind the task
     * to the list and give it a timestamp.</p>
     * @param plugin The plugin that may own the Tasks in the TaskScheduler
     * @param task  The Runnable task authored by the developer using this Scheduler
     * @param offset The time (in milliseconds) from when the Task is scheduled until it first runs, if any
     * @param period The time between repeated scheduled invocations of the task, if any
     * @return ScheduledTask  The ScheduledTask is the internal implementation of the Task managed by this Scheduler
     */
    private ScheduledTask taskValidationStep(Object plugin, Runnable task, long offset, long period) {

        // No owner
        if (plugin == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.PLUGIN_CONTAINER_NULL_WARNING);
            return null;
        }

        // Owner is not a PluginContainer derived class
        else if (!PluginContainer.class.isAssignableFrom(plugin.getClass())) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.PLUGIN_CONTAINER_INVALID_WARNING);
            return null;
        }

        // Is task a Runnable task?
        if (task == null) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.NULL_RUNNABLE_ARGUMENT_WARNING);
            return null;
        }

        // task is not derived from a Runnable class.
        else if (!Runnable.class.isAssignableFrom(task.getClass())) {
            SpongeMod.instance.getLogger().warn(SchedulerLogMessages.NULL_RUNNABLE_ARGUMENT_INVALID_WARNING);
            return null;
        }

        if ( offset < 0L ) {
            SpongeMod.instance.getLogger().error(SchedulerLogMessages.DELAY_NEGATIVE_ERROR);
            return null;
        }

        if ( period < 0L ) {
            SpongeMod.instance.getLogger().error(SchedulerLogMessages.INTERVAL_NEGATIVE_ERROR);
            return null;
        }

        // plugin is a PluginContainer
        PluginContainer plugincontainer = (PluginContainer) plugin;

        // The caller provided a valid PluginContainer owner and a valid Runnable task.
        // Convert the arguments and store the Task for execution the next time the task
        // list is checked. (this task is firing immediately)
        // A task has at least three things to keep track:
        //   The container that owns the task (pcont)
        //   The Thread Body (Runnable) of the Task (task)
        //   The Task Period (the time between firing the task.)   A default TaskTiming is zero (0) which
        //    implies a One Time Shot (See Task interface).  Non zero Period means just that -- the time
        //    in milliseconds between firing the event.   The "Period" argument to making a new
        //    ScheduledTask is a Period interface intentionally so that

        return new ScheduledTask(offset, period)
                .setTimestamp(System.currentTimeMillis())
                .setPluginContainer(plugincontainer)
                .setRunnableBody(task)
                .setState(ScheduledTask.ScheduledTaskState.WAITING);
    }

    private  boolean startTask(ScheduledTask task) {
        boolean bRes = true;
        if (task != null) {
            Runnable taskRunnableBody = task.runnableBody;
            try {
                taskRunnableBody.run();
            } catch (Exception ex) {
                SpongeMod.instance.getLogger().error(SchedulerLogMessages.USER_TASK_FAILED_TO_RUN_ERROR);
                SpongeMod.instance.getLogger().error(ex.toString());
                bRes = false;

            }
        } else {
            SpongeMod.instance.getLogger().error(SchedulerLogMessages.USER_TASK_TO_RUN_WAS_NULL_WARNING);
            bRes = false;
        }
        return bRes;
    }
}
