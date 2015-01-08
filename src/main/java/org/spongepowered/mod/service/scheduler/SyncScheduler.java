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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.scheduler.Scheduler;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.mod.SpongeMod;

import java.util.*;

/**
 *
 * <p>Synchronous Scheduler Implementation Notes:</p>
 * <p>
 * For the feature/schedulers branch, initial checkin for updates
 * to the API, the core game (where necessary) and added new
 * interfaces for one kind of scheduler - a synchronous scheduler.</p>
 *
 * <p>
 * Synchronous is related to a clock, and by default the relationship
 * of being "synchronous" is to be in step with the Minecraft server tick event.
 * So, to be "asynchronous" means to not run on the time base of the 
 * Server Tick, but rather run on a time base that is relative, 
 * eg., the wall-clock. </p>
 *
 */

public class SyncScheduler implements Scheduler {

    /**
     * <p>
     * The intent is the implementation of the Task Scheduler is 
     * a single instance in the context of the Sponge.</p>
     *
     * <p>
     * There isn't a strong motivation to have multiple instances of the "Scheduler".</p>
     */

    private static volatile Scheduler instance = null;

    // The simple private list of all pending (and running) ScheduledTasks
    private List<ScheduledTask> taskList = null;

    private boolean ready = false;
    private volatile long counter = 0L;

    /**
     * <p>
     * We establish the SyncScheduler when the SyncScheduler is created.  This will
     * happen once.  We simply initialize the first state in the SyncScheduler state
     * machine and let the state machine execute.  Only calls to the SyncScheduler
     * through the control interface (TBD) may control the behavior of the state machine itself.</p>
     *
     * <p>
     * The c'tor of the Scheduler is private.  So to get the scheduler, user code calls game.getScheduler()
     * or directly by SyncScheduler.getInstance().  In time access to the scheduler should be migrated into
     * the Services Manager.</p>
     *
     */
    private SyncScheduler() {
        taskList = new ArrayList<ScheduledTask>();
    }

    /**
     * <p>Returns the instance (handle) to the TaskScheduler.</p>
     *
     * <p>
     * A static reference to the TaskScheduler singleton is returned by
     * the function getInstance().  The implementation of getInstance follows the
     * typical practice of double locking on the instance variable. The instance variable
     * is type modified volatile as part of the Pattern of this kind of Singleton implementation.</p>
     *
     * @return interface to the Scheduler
     */
    public static Scheduler getInstance() {
        if (instance == null) {
            synchronized (SyncScheduler.class) {
                if (instance == null) {
                    instance = (Scheduler) new SyncScheduler();
                }
            }
        }
        return instance;
    }

    /**
     * <p>The hook to update the Ticks known by the SyncScheduler.</p>
     *
     * <p>
     * When a TickEvent occurs, the event handler onTick will accumulate a new value for
     * the counter.  The Phase of the TickEvent used is the TickEvent.ServerTickEvent Phase.START.</p>
     *
     * <p>
     * The counter is equivalent to a clock in that each new value represents a new
     * tick event.  Use of delay (Task.offset), interval (Task.period), timestamp (Task.timestamp) all
     * are based on the time unit of Ticks.  To make it easier to work with in in Plugins, the type
     * is simply a @long but it's never negative.  A better representation would been Number (a cardinal
     * value), but this is what we're using.</p>
     *
     * @param event
     */
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        
       // if (event.phase == TickEvent.Phase.START) {
            counter++;
            ProcessTasks();
       // }
    }




    private void ProcessTasks() {

        synchronized (taskList) {

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

            for (ScheduledTask task : taskList) {

                // If the task is now slated to be canceled, we just remove it as if it no longer exists.

                if (task.state == ScheduledTask.ScheduledTaskState.CANCELED) {
                    taskList.remove(task);
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
                long now = counter;

                // So, if the current time minus the timestamp of the task is greater than
                // the delay to wait before starting the task, then start the task.

                // Repeating tasks get a reset-timestamp each time they are set RUNNING
                // If the task has a period of 0 (zero) this task will not repeat, and is removed
                // after we start it.

                if (threshold < (now - task.timestamp)) {

                    // startTask is just a utility function within the Scheduler that
                    // starts the task.

                    boolean bTaskStarted = startTask(task);
                    if (bTaskStarted) {

                        task.setState(ScheduledTask.ScheduledTaskState.RUNNING);

                        // If task is one time shot, remove it from the list.
                        if (task.period == 0L) {
                            taskList.remove(task);
                            continue;
                        } else {
                            // If the task is not a one time shot then keep it and
                            // change the timestamp to now.
                            task.timestamp = counter;
                        }

                    }
                }
            }
        }
    }


    /**
     * <p>Runs a Task once immediately.</p>
     *
     * <p>
     * The runTask method is used to run a single Task just once.  The Task
     * may persist for the life of the server, however the Task itself will never
     * be restarted.  It has no delay offset.  The Scheduler will not wait before
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

        ScheduledTask nonRepeatingtask = taskValidationStep(plugin, task, NODELAY, NOPERIOD);

        if (nonRepeatingtask == null) {
            SpongeMod.instance.getLogger().warn(LogMessages.CANNOT_MAKE_TASK_WARNING);
        }
        else {
            synchronized(taskList) {
                taskList.add(nonRepeatingtask);
            }
            result = Optional.of  ( (Task) nonRepeatingtask );
        }

        return result;
    }


    /**
     * <p>Runs a Task once after a specific delay offset.</p>
     *
     * <p>
     * The runTask method is used to run a single Task just once.  The Task
     * may persist for the life of the server, however the Task itself will never
     * be restarted.  The Scheduler will not wait before running the Task.
     * <b>The Task will be delayed artifically for delay Ticks.</b>  Because the time
     * unit is in Ticks, this scheduled Task is synchronous (as possible) with the
     * event of the Tick from the game.  Overhead, network and system latency not 
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
            SpongeMod.instance.getLogger().warn(LogMessages.CANNOT_MAKE_TASK_WARNING);
        }
        else {

            synchronized(taskList) {
                nonRepeatingTask.setTimestamp(counter);
                taskList.add(nonRepeatingTask);
            }
            result = Optional.of  ( (Task) nonRepeatingTask );
        }

        return result;
    }

    /**
     * <p>Start a repeating Task with a period (interval) of Ticks.  The first occurance will start immediately.</p>
     *
     * <p>
     * The runRepeatingTask method is used to run a Task that repeats.  The Task
     * may persist for the life of the server. The Scheduler will not wait before running
     * the first occurance of the Task. The Scheduler will not allow a second occurance of
     * the task to start if the preceeding occurance is is still alive.  Be sure to end
     * the Runnable Thread of the Task before anticipating the recurrance of the Task.</p>
     *
     * <p> 
     * If the Scheduler detects that two tasks will overlap as such, the 2nd Task will not
     * be started.  The next time the Task is due to run, the test will be made again to determine
     * if the previous occurance of the Task is still alive (running).  As long as a prevous occurance
     * is running no new occurances of that specific Task will start, although the Scheduler will
     * never cease in trying to start it a 2nd time.</p>
     * 
     * <p>
     * Because the time unit is in Ticks, this scheduled Task is synchronous (as possible) with the
     * event of the Tick from the game.  Overhead, network and system latency not 
     * withstanding the Task will run (and re-run) after the delay expires.</p>
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
        Optional<Task> result = Optional.absent();;
        final long NODELAY = 0L;

        ScheduledTask repeatingTask = taskValidationStep(plugin, task, NODELAY, interval);

        if (repeatingTask == null) {
            SpongeMod.instance.getLogger().warn(LogMessages.CANNOT_MAKE_TASK_WARNING);
        }
        else {

            synchronized(taskList) {
                repeatingTask.setTimestamp(counter);
                taskList.add(repeatingTask);
            }
            result = Optional.of  ( (Task) repeatingTask );
        }

        return result;
    }

    /**
     * <p>
     * Start a repeating Task with a period (interval) of Ticks.  
     * The first occurance will start after an initial delay.</p>
     *
     * <p>
     * The runRepeatingTask method is used to run a Task that repeats.  The Task
     * may persist for the life of the server. The Scheduler <b>will wait</b> before running
     * the first occurance of the Task. The Scheduler will not allow a second occurance of
     * the task to start if the preceeding occurance is is still alive.  Be sure to end
     * the Runnable Thread of the Task before anticipating the recurrance of the Task.</p>
     *
     * <p> 
     * If the Scheduler detects that two tasks will overlap as such, the 2nd Task will not
     * be started.  The next time the Task is due to run, the test will be made again to determine
     * if the previous occurance of the Task is still alive (running).  As long as a prevous occurance
     * is running no new occurances of that specific Task will start, although the Scheduler will
     * never cease in trying to start it a 2nd time.</p>
     * 
     * <p>
     * Because the time unit is in Ticks, this scheduled Task is synchronous (as possible) with the
     * event of the Tick from the game.  Overhead, network and system latency not 
     * withstanding the Task will run (and re-run) after the delay expires.</p>
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
        Optional<Task> result = Optional.absent();;

        ScheduledTask repeatingTask = taskValidationStep(plugin, task, delay, interval);

        if (repeatingTask == null) {
            SpongeMod.instance.getLogger().warn(LogMessages.CANNOT_MAKE_TASK_WARNING);
        }
        else {
            synchronized(taskList) {
                repeatingTask.setTimestamp(counter);
                taskList.add(repeatingTask);
            }
            result = Optional.of  ( (Task) repeatingTask );
        }

        return result;
    }

    /**
     * <p>
     * Start a repeating Task with a period (interval) of Ticks.  The first occurance
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

        for (Iterator<ScheduledTask> itr = taskList.iterator(); itr.hasNext(); ) {
            ScheduledTask task = itr.next();
            if (id.equals(task.id)) {
                result = Optional.of( (Task) task);
            }
        }
        return result;
    }

    @Override
    public Collection<Task> getScheduledTasks() {

        // TODO -- make copy of the collection (?)
        // TODO -- Not Implemented
        return null;
    }

    @Override
    public Collection<Task> getScheduledTasks(Object plugin) {

        // TODO -- make copy of the collection (?)
        // TODO -- Not Implemented
        return null;
    }

    private ScheduledTask taskValidationStep(Object plugin, Runnable task, long offset, long period) {

        // No owner
        if (plugin == null) {
            if (plugin == null) {
                SpongeMod.instance.getLogger().warn(LogMessages.PLUGIN_CONTAINER_NULL_WARNING);
            } 
            return null;
        }

        // Owner is not a PluginContainer derived class
        else if (!PluginContainer.class.isAssignableFrom(plugin.getClass())) {
            SpongeMod.instance.getLogger().warn(LogMessages.PLUGIN_CONTAINER_INVALID_WARNING);
            return null;
        }

        // Is task a Runnable task?
        if (task == null) {
            SpongeMod.instance.getLogger().warn(LogMessages.NULL_RUNNABLE_ARGUMENT_WARNING);
            return null;
        }
        // task is not derived from a Runnable class.
        else if (!Runnable.class.isAssignableFrom(task.getClass())) {
            SpongeMod.instance.getLogger().warn(LogMessages.NULL_RUNNABLE_ARGUMENT_INVALID_WARNING);
            return null;
        }


        if ( offset < 0L ) {
            SpongeMod.instance.getLogger().error(LogMessages.DELAY_NEGATIVE_ERROR);
            return null;
        }

        if ( period < 0L ) {
            SpongeMod.instance.getLogger().error(LogMessages.INTERVAL_NEGATIVE_ERROR);
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

        ScheduledTask scheduledtask = new ScheduledTask(offset, period)
                .setPluginContainer(plugincontainer)
                .setRunnableBody(task)
                .setState(ScheduledTask.ScheduledTaskState.WAITING);

        return scheduledtask;
    }

    private void invalidateScheduledTaskList() {

        // look at the current task list.
        // determine if any remaining tasks are present
        // for the tasks that are present, dispose them safely.
        // leave the condition of the list empty.

        if (taskList == null) {
            for (ScheduledTask task : taskList) {
                if (task != null) {
                    // dispose of task
                    // TODO - Tempted to do a bit more rigorous destruction of the task.
                    taskList.remove(task);
                }
            }
            taskList = new ArrayList<ScheduledTask>();
        }
    }
    private  boolean startTask(ScheduledTask task) {
        boolean bRes = true;

        if (task != null) {

            Runnable taskRunnableBody = task.runnableBody;

            try {
                taskRunnableBody.run();
            } catch (Exception ex) {
                SpongeMod.instance.getLogger().error(LogMessages.USER_TASK_FAILED_TO_RUN_ERROR);
                SpongeMod.instance.getLogger().error(ex.toString());
                bRes = false;

            }

        }
        else {
            SpongeMod.instance.getLogger().error(LogMessages.USER_TASK_TO_RUN_WAS_NULL_WARNING);
            bRes = false;
        }

        return bRes;
    }
 

    // Text strings that are logged in the case of warnings/errors/exceptions as needed.
    // Edit to suit.
 
    private enum LogMessages {

        CANNOT_MAKE_TASK_WARNING("Task cannot be created."),

        INTERVAL_NEGATIVE_ERROR("The Task as defined cannot be created. The interval (period) of the Task is negative."),

        DELAY_NEGATIVE_ERROR("The Task as defined cannot be created. The delay (offset) of the Task is negative."),

        PLUGIN_CONTAINER_NULL_WARNING("The Scheduler could not create the Task because the PluginContainer was null."),

        PLUGIN_CONTAINER_INVALID_WARNING("The Task cannot be created because the PluginContainer was not derived from a PluginContainer.class."),

        NULL_RUNNABLE_ARGUMENT_WARNING("The Task cannot be created because the Runnable argument is null."),

        NULL_RUNNABLE_ARGUMENT_INVALID_WARNING("The Task could not be created because the Runnable argument is not derived from a Runnable interface."),

        USER_TASK_FAILED_TO_RUN_ERROR("The Scheduler tried to run the Task, but the Runnable could not be started."),

        USER_TASK_TO_RUN_WAS_NULL_WARNING("The Scheduler tried to run the Task, but the Task is null. The Task did not start.");

        String message;

        LogMessages(String val) {
            message = val;
        }
    }
    

}
