package com.ems.server.processor;

/**
 * @author 
 *
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.server.data.PMStatsWork;
import com.ems.server.service.PMStatsProcessorService;

public class PMStatsThread extends Thread {

	private final String threadName;
	private LinkedList<PMStatsWork> workQueue;
	private boolean running = true;
	private int noOfPacketsToProcess = 10;
	private int thresholdPackets = 5000;
	private PMStatsProcessorService pmStatsProcessorService;

	static final Logger timingLogger = Logger.getLogger("TimingLogger");

	public PMStatsThread(int noOfThreads, String name, int pmStatsQueueThreshold, int pmStatsProcessingBatchSize) {
		threadName = name;
		thresholdPackets = pmStatsQueueThreshold;
		noOfPacketsToProcess = pmStatsProcessingBatchSize;
		workQueue = new LinkedList();
		pmStatsProcessorService = (PMStatsProcessorService) SpringContext
				.getBean("pmStatsProcessorService");
	}

	public void addWork(PMStatsWork sw) {
		synchronized (workQueue) {
			workQueue.addLast(sw);
			workQueue.notify();
		}
	}

	public void stopThreads() {
		running = false;
		workQueue = null;
	}

	@Override
	public void run() {
		while (true) {
			if (!running) {
				return;
			}

			List<PMStatsWork> processingQueue = new ArrayList<PMStatsWork>(20);

			synchronized (workQueue) {
				while (workQueue.isEmpty()) {
					try {
						workQueue.wait();
					} catch (InterruptedException ignored) {
						// System.out.println("thread got interrupted");
					}
					if (!running) {
						return;
					}
				}

				// Make a processing queue for 20 or less at a time
				int counter = 0;
				while (counter < noOfPacketsToProcess && !workQueue.isEmpty()) {
					PMStatsWork work = workQueue.removeFirst();
					processingQueue.add(work);
					counter++;
				}
				
				//If our Queue size is over a limit that means we are constantly falling
				//apart in cleaning up the queue and this situation might lead to memory issues.
				//So let's clean up the queue completely so that the server comes in sane state
				//Ideally we should not reach to this.
				if(workQueue.size() > thresholdPackets){
					timingLogger.warn("The pm stats queue need to be cleared. This means server is processing them slowly");
					workQueue.clear();
				}
			}
			updateStats(processingQueue);
		}
	}

	private void updateStats(List<PMStatsWork> processingQueue) {
		try {
			pmStatsProcessorService.processStats(processingQueue);
		} catch (Exception ex) {
			//Let's catch the exception here so that our next batch works fine
            ex.printStackTrace();
		}

	}

}