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

import java.util.*;

/*

 Implementation Notes:

 This is a work in progress.

 For the feature/schedulers branch, initial checkin for updates
 to the API, the core game (where necessary) and added new
 interfaces for one kind of scheduler - a synchronous scheduler.

 Synchronous is related to a clock, and by default the relationship
 of being "synchronous" is to be in step with the Minecraft server tick event.
 So, to be "asynchronous" means to not run on the time base of the Server Tick,
 but rather run on a time base that is relative, eg., the wall-clock.


 */

public class SyncScheduler implements Scheduler {


    // The intent is the implementation of the Task Scheduler is a single instance
    // in the context of the Sponge Core Mod.  There isn't a strong motivation to
    // have multiple instances of the "Scheduler".
    private static volatile Scheduler instance = null;

    // The scheduler itself runs in it's own thread.  That thread is the
    // Thread Body "tbody", and is initialized when this class is instantiated.
    private Thread tbody = null;

    // Upon instantiation the Scheduler is pre-set to NOT_RUNNING
    private MachineState sm = MachineState.INVALID;

    // The simple private list of all pending (and running) ScheduledTasks
    private List<ScheduledTask> taskList = null;

    private boolean ready = false;
    private volatile long timeout = Long.MAX_VALUE;
    private volatile long counter = 0;

    /**
     * <p>Unlike the State of a ScheduledTask, the @MachineState of the Scheduler is private and not an
     * enumeration of values that have any user facing purpose.  This is why the enumeration is
     * defined within the class and inaccessible to user code.</p>
     */
    private enum MachineState {
        INIT,
        START,
        RUN,
        PAUSE,
        RESUME,
        STOP,
        RESTART,
        INVALID,
    }

    /**
     * Returns the instance (handle) to the TaskScheduler.
     *
     * <p>A static reference to the TaskScheduler singleton is returned by
     * the function getInstance().  The implementation of getInstancce follows the
     * typical practice of double locking on the instance variable. The instance variable
     * is type modified volatile as part of the Pattern of this kind of Singleton implementation. </p>
     *
     * @return instance of the TaskScheduler Singleton
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



    @SubscribeEvent
    public void onTick(TickEvent event) {
        counter++;
    }

    private SyncScheduler() {
        /**
         * <p>We establish the TaskScheduler when the TaskScheduler is created.  This will
         * happen once.  We simply initialize the first state in the TaskScheduler state
         * machine and let the state machine execute.  Only external calls to the TaskScheduler
         * may control the behavior of the state machine itself.</p>
         *
         * TODO Explain working process of the state machine in the TaskScheduler
         */

        sm = MachineState.INIT;
        taskList = new ArrayList<ScheduledTask>();
        tbody = new Thread(new Runnable() {
            public void run() {
               SMachine();
            }
        });

        tbody.start();
    }

    private void SMachine() {

        do {
            switch (sm) {

                case INIT: {

                    sm = MachineState.START;
                    break;
                }

                case START: {

                    sm = MachineState.RUN;
                    break;
                }
                case RUN: {


                    // This is the Running state of the State Machine.
                    // In this state, all tasks that are known by the TaskScheduler will be executed
                    // per the parameters of each task and the constraints of the Task Scheduler.
                    //
                    // Only internal Sponge Core Mod agents can control the operation of the state machine
                    // and control the Running/Pause/Resume/etc.. operation of the State Machine in the
                    // TaskScheduler.

                    // In other words, the Sponge Core Mod itself is what turns on/off/suspends/resumes
                    // the actual state machine that processes pending Tasks in the Scheduler.
                    //
                    // User (plugin developer) code on the other hand is what stimulates the Scheduler
                    // to be aware of new Tasks to enqueue to the list of pending Tasks.


                    synchronized (taskList) {


                        // So, a lot of hay is made over the fact that in current Java implementation,
                        // containers are (can be made so) thread-safe. Plus,  the thoughts that
                        // wait/notify is antique compared to these new thread-aware containers have
                        // been suggested several times.

                        // The decision to use wait/notify is not so much to protect the list
                        // from corruption, but to reduce the CPU footprint of the Scheduler.

                        // "Waiting" is yielding and so by simply yielding, we're not consuming
                        // time busy-waiting, we're letting the JVM wake up the thread when there is
                        // work to do.  The goal is to have a Scheduler whereby the ratio of time spent
                        // in overhead (running the scheduler) is a mere small fraction of all the time spent
                        // actually running tasks from plugins.

                        // As far as the timeout used, the timeout is dynamically set during the operation
                        // of the State Machine so that a good guess for how long to time out is used.

                        // We rely also on the fact the wait will be interrupted when the notify is called\
                        // (when a task is added)

                        try {
                            //System.out.println("StateMachine RUN - Timeout: " + timeout);
                            taskList.wait(timeout);
                        } catch (IllegalMonitorStateException ex) {
                            System.out.println("Illegal Monitor State Exception: " + ex.toString());
                        } catch (InterruptedException ex) {
                            System.out.println("Interrupt Exception!");
                        }


                        /**
                         * For each task, inspect the state.
                         *
                         * For the state of CANCELED, remove it and look at the next task, if any.
                         *
                         * For the state of WAITING, the task has not begun, so check the
                         * offset time before starting that task for the first time.
                         *
                         * Else if the task is already RUNNING, use the period (the time delay until
                         * the next moment to run the task).
                         *
                         *
                         */

                        for (ScheduledTask task : taskList) {
                            // for (Iterator<ScheduledTask> itr = taskList.iterator(); itr.hasNext(); ) {
                            //     ScheduledTask task = itr.next();

                            if (task.state == ScheduledTask.ScheduledTaskState.CANCELED) {
                                taskList.remove(task);
                                continue;
                            }

                            // We don't know how long to "wait" until letting the task run, yet.
                            long threshold = Long.MAX_VALUE;

                            if (task.state == ScheduledTask.ScheduledTaskState.WAITING) {
                                threshold = task.offset;
                            } else if (task.state == ScheduledTask.ScheduledTaskState.RUNNING) {
                                threshold = task.period;
                            }

                            long now = counter; // System.currentTimeMillis();

                            // So, if the current time minus the timestamp of the task is greater than
                            // the delay to wait before starting the task, then start the task.

                            // Repeating tasks get a reset-timestamp each time they are set RUNNING
                            // If the task has a period of 0 (zero) this task will not repeat, and is removed
                            // after we start it.

                            if (threshold < (now - task.timestamp)) {

                                // startTask is just a utility function within the Scheduler that
                                // starts the task.

                                if (startTask(task)) {
                                    task.setState(ScheduledTask.ScheduledTaskState.RUNNING);
                                    // if task is one time shot, remove it from the list
                                    if (task.period == 0) {
                                        taskList.remove(task);
                                        continue;
                                    } else {
                                        // if the task is not a one time shot then keep it and change the timestamp to now.
                                        task.timestamp = counter; // System.currentTimeMillis();
                                    }

                                }
                            }
                        }

                        // After processing all the tasks, we need to make sure we have set
                        // the threshhold for waiting (waite/notify) correctly.
                        // First, if the size of the taskList is zero, there are no tasks,
                        // so we wait virtually forever until a new task is added.

                        // Else, scan through the remaining tasks (WAITING OR RUNNING) and reset
                        // the threshhold timeout to be the minimum.


                        if (taskList.size() == 0) {
                            timeout = Long.MAX_VALUE;
                        } else {
                            for (ScheduledTask taskAdjustee : taskList) {
                                // for (Iterator<ScheduledTask> itr = taskList.iterator(); itr.hasNext(); ) {
                                //    ScheduledTask task = itr.next();
                                if (taskAdjustee.state == ScheduledTask.ScheduledTaskState.WAITING) {
                                    timeout = (taskAdjustee.offset < timeout) ? taskAdjustee.offset : timeout;
                                } else if (taskAdjustee.state == ScheduledTask.ScheduledTaskState.RUNNING) {
                                    timeout = (taskAdjustee.period < timeout) ? taskAdjustee.period : timeout;
                                }

                            }
                        }
                    }

                    break;
                }

                case PAUSE: {
                    // Do what we need to do as we enter the PAUSE state.
                    //
                    // TODO
                    //
                    // We're in PAUSE.  There is no way to leave PAUSE unless
                    // external control of the State Machine permits.
                    //
                    // An exception could be that we want to leave PAUSE
                    // when a special kind of event or special kind of Task is
                    // set to run.

                    synchronized (taskList) {
                        try {
                            //System.out.println("StateMachine RUN - Timeout: " + timeout);
                            taskList.wait(timeout);
                        } catch (IllegalMonitorStateException ex) {
                            System.out.println("Illegal Monitor State Exception: " + ex.toString());
                        } catch (InterruptedException ex) {
                            System.out.println("Interrupt Exception!");
                        }
                    }

                    break;
                }
                case RESUME: {
                    // Do what we need to do before re-entering the RUN state
                    //
                    // While the machine is Paused the timestamps did not change
                    // so, tasks that were almost ready to run will now be ready
                    // to run as soon as we leave this state.  The impact is
                    // if the machine was Paused long enough, then the rush of
                    // new tasks will be executed in the RUN state.
                    //
                    // The contract with the Plugin is that we run Tasks on a certain
                    // offset and period (delay and interval) but only when the
                    // machine is running.
                    //
                    // This machine ignores the edge-case where a Task is almost
                    // ready to run, and the machine is Paused.   When the machine
                    // resumes, the task that was held Paused by the machine will
                    // run as soon as the machine re-enters RUN.
                    //
                    // The way around that behavior would be to reset a new offset and
                    // period for one cycle based on the remaining time until the task would
                    // have been run and then use that data for the timing of the Task
                    // as soon as the machine is Resumed.  Then reset the offset/period
                    // again as normal.  The case is complicated since we can be re-Paused
                    // at anytime.  So, it's easier to just run the Tasks when we Resume
                    // if their timestamps are old enough (  threshhold < now - timestamp )
                    //
                    // now set the State Machine to re enter the RUS state

                    sm = MachineState.RUN;
                    break;
                }

                case STOP: {
                    // Do what we need to do before invalidating the machine (and exiting)
                    //
                    // TODO
                    //
                    // now set the State Machine to INVALID

                    sm = MachineState.INVALID;
                    break;
                }

                case RESTART: {
                    // Do what we need to do before re-initializing the State Machine.
                    //
                    // TODO
                    //
                    // now set the State Machine to re-INITialize.

                    sm = MachineState.INIT;
                    break;
                }
                case INVALID: {
                    // We're truly done with the machine.  There is no going back.
                    // Finish up whatever needs to be done and then exit the Thread that owns
                    // this state machine
                    // TODO - anything?

                    // Now let the loop exit, and thus the thread to finish.

                    break;
                }
            }
        } while ( sm != MachineState.INVALID) ;
    }


    @Override
    public Optional<Task> runTask(Object plugin, Runnable task) {
        /**
         * The intent of this method is to run a single task (non-repeating) and has zero
         * offset (doesn't wait a delay before starting), and a zero period (no repetition)
         */
        Optional<Task> result = Optional.absent();
        final long NODELAY = 0;
        final long NOPERIOD = 0;

        ScheduledTask nonRepeatingtask = taskValidationStep(plugin, task, NODELAY, NOPERIOD);

        if (nonRepeatingtask == null) {
            // TODO - Log error ?

        }
        else {
            synchronized(taskList) {
                taskList.add(nonRepeatingtask);
                taskList.notify();
            }
            result = Optional.of  ( (Task) nonRepeatingtask );
        }

        return result;
    }

    @Override
    public Optional<Task> runTaskAfter(Object plugin, Runnable task, long delay) {
        Optional<Task> result = Optional.absent();
        final long NOPERIOD = 0;

        ScheduledTask nonRepeatingTask = taskValidationStep(plugin, task, delay, NOPERIOD);

        if (nonRepeatingTask == null) {
            // TODO - Log error ?
        }
        else {

            synchronized(taskList) {
                nonRepeatingTask.setTimestamp(counter);
                taskList.add(nonRepeatingTask);
                taskList.notify();
            }
            result = Optional.of  ( (Task) nonRepeatingTask );
        }

        return result;
    }

    @Override
    public Optional<Task> runRepeatingTask(Object plugin, Runnable task, long interval) {
        Optional<Task> result = Optional.absent();;
        final long NODELAY = 0;

        ScheduledTask repeatingTask = taskValidationStep(plugin, task, NODELAY, interval);

        if (repeatingTask == null) {
            // TODO - Log error ?
        }
        else {

            synchronized(taskList) {
                repeatingTask.setTimestamp(counter);
                taskList.add(repeatingTask);
                taskList.notify();
            }
            result = Optional.of  ( (Task) repeatingTask );
        }

        return result;
    }

    @Override
    public Optional<Task> runRepeatingTaskAfter(Object plugin, Runnable task, long interval, long delay) {
        Optional<Task> result = Optional.absent();;

        ScheduledTask repeatingTask = taskValidationStep(plugin, task, delay, interval);

        if (repeatingTask == null) {
            // TODO - Log error ?
        }
        else {
            synchronized(taskList) {
                repeatingTask.setTimestamp(counter);
                taskList.add(repeatingTask);
                taskList.notify();
            }
            result = Optional.of  ( (Task) repeatingTask );
        }

        return result;
    }

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
        return null;
    }

    @Override
    public Collection<Task> getScheduledTasks(Object plugin) {
        return null;
    }

    //
    //


    private ScheduledTask taskValidationStep(Object plugin, Runnable task, long offset, long period) {

        // No owner
        if (plugin == null) {
//            this.logger.warn("The TaskScheduler could not create the Task because the PluginContainer was null."
//                    +" Check the source code of the plugin.");
            return null;
        }
        // Owner is not a PluginContainer derived class
        else if (!PluginContainer.class.isAssignableFrom(plugin.getClass())) {
//            this.logger.warn("The TaskScheduler could not create the task because the PluginContainer was not "
//                    +"derived from a PluginContainer.class.  Check the source code of the plugin.");
            return null;
        }


        // Is task a Runnable task?
        if (task == null) {
//            this.logger.warn("The TaskScheduler could not create the Task because the Runnable argument "
//                    +" was null. Check the source code of the plugin.");
            return null;
        }
        // task is not derived from a Runnable class.
        else if (!Runnable.class.isAssignableFrom(task.getClass())) {
//            this.logger.warn("The TaskScheduler could not create the Task because the Runnable argument "
//                    +" was not (or is not) derived from a Runnable interface.  Check source code of plugin.");
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

        long now = System.currentTimeMillis();
        ScheduledTask scheduledtask = new ScheduledTask(offset, period)
                .setPluginContainer(plugincontainer)
                .setRunnableBody(task)
                .setTimestamp(now)
                .setState(ScheduledTask.ScheduledTaskState.WAITING);

        if (period < timeout) {
            timeout = period;
        }
        return scheduledtask;

    }

    private void invalidateScheduledTaskList() {
        // look at the current task list.
        // determine if any remaining tasks are present
        // for the tasks that are present, dispose them safely.
        // leave the condition of the list empty./
        if (taskList == null) {
            for (ScheduledTask task : taskList) {
                if (task != null) {
                    // dispose of task
                    taskList.remove(task);
                }
            }
            taskList = new ArrayList<ScheduledTask>();
        }
    }
    private  boolean startTask(ScheduledTask task) {
        boolean bRes = true;

        if (task != null) {
            Runnable runnableThreadBodyOfTask = task.threadbody;
            if (((Thread) runnableThreadBodyOfTask).isAlive()) {
                // we're still running?!
                // do not restart the Runnable thread body if so.
                bRes = false;
            } else {
                try {
                    runnableThreadBodyOfTask.run();
                } catch (Exception ex) {
                    // TODO log what kind of failure this is?
                    // TODO log here or throw?
                    // this.logger.warn("The TaskScheduler tried to run the Task, but the Runnable could not be started: ", ex);
                    bRes = false;

                }
            }
        }
        else {
            // TODO Log the warning message that the task was null (or whatever else is failed).
            // this.logger.warn(String.format("The TaskScheduler tried to run the Task, but it was null. Nothing happened."));
            bRes = false;
        }

        return bRes;
    }


}
