package com.ems.server.processor;

/**
 * @author 
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.server.data.ZeroBucketStruct;
import com.ems.server.service.UpdatePMStatsZeroBucketProcessorService;

public class UpdatePMStatsThread extends Thread {

	private final String threadName;
	private Map<Long, ZeroBucketStruct> workQueue;
	private boolean running = true;
	private int noOfPacketsToProcess = 10;
	private int thresholdPackets = 5000;
	private UpdatePMStatsZeroBucketProcessorService updatePMStatsZeroBucketProcessorService;

	static final Logger timingLogger = Logger.getLogger("TimingLogger");

	public UpdatePMStatsThread(int noOfThreads, String name,
			int pmStatsQueueThreshold, int pmStatsProcessingBatchSize) {
		threadName = name;
		thresholdPackets = pmStatsQueueThreshold;
		noOfPacketsToProcess = pmStatsProcessingBatchSize;
		workQueue = new HashMap<Long, ZeroBucketStruct>();
		updatePMStatsZeroBucketProcessorService = (UpdatePMStatsZeroBucketProcessorService) SpringContext
				.getBean("updatePMStatsZeroBucketProcessorService");
	}

	public void addZeroBucketStruct(ZeroBucketStruct sw) {
		synchronized (workQueue) {
			if (!workQueue.containsKey(sw.getFixtureId())) {
				workQueue.put(sw.getFixtureId(), sw);
				workQueue.notify();
			}
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

			List<ZeroBucketStruct> processingQueue = new ArrayList<ZeroBucketStruct>(
					20);

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
				Iterator<Entry<Long, ZeroBucketStruct>> itr = workQueue
						.entrySet().iterator();
				while (counter < noOfPacketsToProcess && !workQueue.isEmpty()) {
					Map.Entry<Long, ZeroBucketStruct> entrySet = (Map.Entry) itr
							.next();
					itr.remove(); //
					processingQueue.add(entrySet.getValue());
					counter++;
				}

				// If our Queue size is over a limit that means we are
				// constantly falling
				// apart in cleaning up the queue and this situation might lead
				// to memory issues.
				// So let's clean up the queue completely so that the server
				// comes in sane state
				// Ideally we should not reach to this.
				if (workQueue.size() > thresholdPackets) {
					timingLogger
							.warn("The update pm stats queue need to be cleared. This means server is processing them slowly");
					workQueue.clear();
				}
			}
			updateStats(processingQueue);
		}
	}

	private void updateStats(List<ZeroBucketStruct> processingQueue) {
		try {
			updatePMStatsZeroBucketProcessorService
					.processStats(processingQueue);
		} catch (Exception ex) {
			// Let's catch the exception here so that our next batch works fine
			ex.printStackTrace();
		}

	}

}