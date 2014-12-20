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

import java.util.*;

/*

 Implementation Notes:

 This is a work in progress.

 For the feature/schedulers branch, initial checkin for updates
 to the API, the core game (where necessary) and added new
 interfaces for two kinds of schedulers:

 1. AsynchronousScheduler based on "wall-clock" timer
 (elapsed milliseconds on system). (initial version)
 2. SynchronousScheduler based on Server Tick events (incomplete)

 Synchronous is related to a clock, and by default the relationship
 of being "synchronous" is to be in step with the Minecraft server tick event.
 So, to be "asynchronous" means to not run on the time base of the Server Tick,
 but rather run on a time base that is relative, eg., the wall-clock.

 At this time, the basis of a Synchronous clock vis-a-vis the Minecraft
 Server Tick is still out of reach until some new mechanism is coded to
 emit the event of a new Tick.  Currently, there is a tickCounter, but
 what is needed is essentially a TickEvent in order to make the Synchronous
 Scheduler efficient and in step with the game Tick.

 Regardless, the AsynchronousScheduler allows for ad-hoc tasks to be
 scheduled as one-time-shot, or repeated tasks given a certain offset
 (delay from whence the Task is captured) and period (the interval
 between invocations of the Task).
 */

public class AsyncScheduler implements AsynchronousScheduler {


    // The intent is the implementation of the Task Scheduler is a single instance
    // in the context of the Sponge Core Mod.  There isn't a strong motivation to
    // have multiple instances of the "Scheduler".
    private static volatile AsynchronousScheduler instance = null;

    // The scheduler itself runs in it's own thread.  That thread is the
    // Thread Body "tbody", and is initialized when this class is instantiated.
    private Thread tbody = null;

    // Upon instantiation the Scheduler is pre-set to NOT_RUNNING
    private MachineState sm = MachineState.INVALID;

    // The simple private list of all pending (and running) ScheduledTasks
    private List<ScheduledTask> taskList = null;

    private boolean ready = false;

    private volatile long timeout = Long.MAX_VALUE;

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
    public static AsynchronousScheduler getInstance() {
        if (instance == null) {
            synchronized (AsyncScheduler.class) {
                if (instance == null) {
                    instance = (AsynchronousScheduler) new AsyncScheduler();
                }
            }
        }
        return instance;
    }

    private AsyncScheduler() {
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
                    System.out.println("INIT State Machine");
                    sm = MachineState.START;
                    break;
                }

                case START: {
                    System.out.println("START State Machine");
                    sm = MachineState.RUN;
                    break;
                }
                case RUN: {

                    //System.out.println("RUN State Machine");
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

                        try {
                            System.out.println("StateMachine RUN - Timeout: " + timeout);
                            taskList.wait(timeout);
                        } catch (IllegalMonitorStateException ex) {
                            System.out.println("Some other error: " + ex.toString());
                        } catch (InterruptedException ex) {
                            System.out.println("Interrupted in wait!");
                        }



                        for (Iterator<ScheduledTask> itr = taskList.iterator(); itr.hasNext(); ) {
                            ScheduledTask task = itr.next();

                            if (task.state == ScheduledTask.ScheduledTaskState.CANCELED) {
                                taskList.remove(task);
                                continue;
                            }

                            long threshold = Long.MAX_VALUE;

                            if (task.state == ScheduledTask.ScheduledTaskState.WAITING) {
                                threshold = task.offset;
                            }
                            else  if (task.state == ScheduledTask.ScheduledTaskState.RUNNING) {
                                threshold = task.period;
                            }

                            long now = System.currentTimeMillis();

                            if ( threshold  < (now - task.timestamp)) {
                                if (startTask(task)) {
                                    task.setState(ScheduledTask.ScheduledTaskState.RUNNING);
                                    // if task is one time shot, remove it from the list
                                    if (task.period == 0) {
                                        taskList.remove(task);
                                        continue;
                                    } else {
                                        // if the task is not a one time shot then keep it and change the timestamp to now.
                                        task.timestamp = System.currentTimeMillis();
                                    }

                                }
                            }
                        }

                        if (taskList.size() == 0) {
                            timeout = Long.MAX_VALUE;
                        }
                        else {
                            for (Iterator<ScheduledTask> itr = taskList.iterator(); itr.hasNext(); ) {
                                ScheduledTask task = itr.next();
                                if (task.state == ScheduledTask.ScheduledTaskState.WAITING) {
                                    timeout = (task.offset < timeout) ? task.offset : timeout;
                                }
                                else if (task.state == ScheduledTask.ScheduledTaskState.RUNNING) {
                                    timeout = (task.period < timeout) ? task.period : timeout;
                                }

                            }
                        }
                    }

                    break;
                }

                case PAUSE: {

                    break;
                }
                case RESUME: {

                    sm = MachineState.RUN;
                    break;
                }
                case STOP: {
                    break;
                }

                case RESTART: {

                    sm = MachineState.INIT;
                    break;
                }
                case INVALID: {
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
        Optional<Task> result;
        final long NODELAY = 0;
        final long NOPERIOD = 0;

        ScheduledTask repeatingtask = taskValidationStep(plugin, task, NODELAY, NOPERIOD);

        if (repeatingtask == null) {
            result = Optional.absent();
            return result;
        }
        else {
            synchronized(taskList) {
                taskList.add(repeatingtask);
                taskList.notify();
            }
        }


        return Optional.of  ( (Task) repeatingtask );

    }

    @Override
    public Optional<Task> runTaskAfter(Object plugin, Runnable task, long delay) {
        Optional<Task> result;
        final long NOPERIOD = 0;

        ScheduledTask repeatingtask = taskValidationStep(plugin, task, delay, NOPERIOD);

        if (repeatingtask == null) {
            result = Optional.absent();
        }
        else {
            result = Optional.of  ( (Task) repeatingtask );
            synchronized(taskList) {
                taskList.add(repeatingtask);
                taskList.notify();
            }
        }

        return result;
    }

    @Override
    public Optional<Task> runRepeatingTask(Object plugin, Runnable task, long interval) {
        Optional<Task> result;
        final long NODELAY = 0;

        ScheduledTask repeatingtask = taskValidationStep(plugin, task, NODELAY, interval);

        if (repeatingtask == null) {
            result = Optional.absent();
        }
        else {
            result = Optional.of  ( (Task) repeatingtask );
            synchronized(taskList) {
                taskList.add(repeatingtask);
                taskList.notify();
            }
        }

        return result;
    }

    @Override
    public Optional<Task> runRepeatingTaskAfter(Object plugin, Runnable task, long interval, long delay) {
        Optional<Task> result;

        ScheduledTask repeatingtask = taskValidationStep(plugin, task, delay, interval);

        if (repeatingtask == null) {
            result = Optional.absent();
        }
        else {
            result = Optional.of  ( (Task) repeatingtask );
            synchronized(taskList) {
                taskList.add(repeatingtask);
                taskList.notify();
            }
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
            for (int i = 0; i < taskList.size(); ++i) {
                ScheduledTask tsk = taskList.get(i);
                if (tsk != null) {
                    // dispose of task t
                    // tsk.dispose();
                    taskList.remove(i);
                    tsk = null;
                }
            }
            taskList = new ArrayList<ScheduledTask>();
        }
    }
    private  boolean startTask(ScheduledTask task) {
        boolean bRes = true;
        if (task != null) {

            Runnable runnableThreadBodyOfTask = task.threadbody;
            try {
                runnableThreadBodyOfTask.run();
            } catch (Exception ex) {
                // TODO log what kind of failure this is?
                // TODO log here or throw?
//                this.logger.warn("The TaskScheduler tried to run the Task, but the Runnable could not be started: ", ex);
                bRes = false;

            }
        } else {
            // TODO Log the warning message that the task was null (or whatever else is failed).
           // this.logger.warn(String.format("The TaskScheduler tried to run the Task, but it was null. Nothing happened."));
            bRes = false;
        }

        return bRes;
    }


}
