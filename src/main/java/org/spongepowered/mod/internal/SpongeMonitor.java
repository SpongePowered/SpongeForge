/**
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
package org.spongepowered.mod.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// The job of the watch dog is to run a simple free running thread on schedule
// every three (TBR) seconds.
// In the execution of the watch dog, samples of latenecy are computed and
// the condition _conditionGood is maintained.
// 
// The result of determining the goodness of the state of the server in
// the watchdog is advice not binding to any consumer.
//
// The main purpose of the watchdog is to provide a view of the consistency
// of the main thread of the server and the subservient manager threads
// which form the basis of the living server.
//
// The state machine in the watchdog has phases:
//    Phase 0:
//       Collect data
//    Phase 1:
//       Compute average and std.deviation
//    Phase 2:
//       Test average and std.deviation to set _conditionGood
//



public class SpongeMonitor {
	
	private static final int METRIC_LIST_LENGTH = 0x64;
	public static Boolean conditionGood = true;
	private static int watchDogInterval = 3;  // 3 seconds
	private static volatile WatchDogState _state = WatchDogState.PHASE_UNKNOWN;

	// Working list of metrics
	private static long[] metric = new long[METRIC_LIST_LENGTH];
	
	// Initialized
	private static int index = 0;
	private static double avg = 0.0f;
	private static double stdDev = 0.0f;
	
	private enum WatchDogState {
		PHASE_UNKNOWN,
		PHASE_COLLECT_DATA,
		PHASE_COMPUTE_METRIC,
		PHASE_TEST,
		PHASE_DONE,
	}
	
    private SpongeMonitor() {
    	final Runnable _watchDogService = new Runnable() {
    		public void run() {
    			_state = WatchDogState.PHASE_COLLECT_DATA;
    			Boolean done = false;
    			while (! done) {
    				switch (_state) {
    				case PHASE_COLLECT_DATA: 
    					if (collectData())
    						_state = WatchDogState.PHASE_COMPUTE_METRIC;
    					else
    						_state = WatchDogState.PHASE_DONE;
    					break;
    				case PHASE_COMPUTE_METRIC:
    					processData();
    					_state = WatchDogState.PHASE_TEST;
    					break;
    				case PHASE_TEST:
    					testData();
    					_state = WatchDogState.PHASE_DONE;
    					break;
    				case PHASE_DONE:
    					done = true;
    					break;
    				}
    			}
    			
    			// Spew for debugging
    			if (index > METRIC_LIST_LENGTH)
    				System.out.println("SpongeMonitor: Avg: " + avg + " StdDev: " + stdDev);
    			else
    				System.out.println("SpongeMonitor: Still collecting " + (METRIC_LIST_LENGTH - index) + " left to go.");
    			
    		}
    	};
    	
    	final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(_watchDogService, 0, watchDogInterval, TimeUnit.SECONDS);
    }

    private static Boolean collectData() {
    	long now = System.currentTimeMillis();
    	System.out.println("WOOF: Collecting data: " + index);
    	metric[ (index++ % METRIC_LIST_LENGTH) ] = now;
    	return  (index > METRIC_LIST_LENGTH);
    }
    
    private static void processData() {
        // Compute avg and std.deviation

    	double diffs[] = new double[METRIC_LIST_LENGTH];
    	
    	avg = 0f;
    	for(int i = 0; i < METRIC_LIST_LENGTH-1; i++) {
    		diffs[i] = Math.abs(metric[i] - metric[i+1]);
    		avg += diffs[i];
    	}

    	// compute std-deviation on the differences between metrics
    	// Nominally should be exactly 1000 * the _watchDogInterval
    	double pwrsumS1 = 0f;
    	for(int i = 0; i < METRIC_LIST_LENGTH-1; i++) {
    		pwrsumS1 += diffs[i];
    	}

    	double pwrsumS2 = 0f;
    	for(int i = 0; i < METRIC_LIST_LENGTH-1; i++) {
    		pwrsumS2 += (diffs[i] * diffs[i]);
    	}
    	
    	stdDev = Math.sqrt( ((METRIC_LIST_LENGTH-1)*pwrsumS2)  - (pwrsumS1*pwrsumS1)) / ((double)METRIC_LIST_LENGTH-1);
    	avg /= (double)METRIC_LIST_LENGTH;
		_state = WatchDogState.PHASE_TEST;
    }
    
    private static void testData() {
    	// test the computed data and set _conditionGood
    	
    	// BUGBUG TODO
    	// What is the value for std.dev that is good?
    	_state = WatchDogState.PHASE_DONE;
    	
    }
   
    // Lazy and thread safe Singleton.
    private static class SingletonHolder { 
        public static final SpongeMonitor instance = new SpongeMonitor();
    }

    public static SpongeMonitor getInstance() {
        return SingletonHolder.instance;
    }
    
    public static Boolean watchDogHasGoodCondition() {
    	return conditionGood;
    }
}





