/**
 * 
 */
package com.ems.server.util;

/**
 * @author 
 *
 */
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class EmsThreadPool {

    private static final Logger logger = Logger.getLogger(EmsThreadPool.class.getName());

    private final int noOfThreads;
    private final String threadName;
    private ThreadPoolWorker[] threadWorkerArr;
    private LinkedList workQueue;
    private boolean running = true;

    public EmsThreadPool(int noOfThreads, String name) {

        this.noOfThreads = noOfThreads;
        threadName = name;
        workQueue = new LinkedList();
        threadWorkerArr = new ThreadPoolWorker[noOfThreads];

        for (int i = 0; i < noOfThreads; i++) {
            threadWorkerArr[i] = new ThreadPoolWorker(threadName);
            threadWorkerArr[i].start();
        }

    } // end of constructor

    public void addWork(Runnable r) {

        synchronized (workQueue) {
            workQueue.addLast(r);
            workQueue.notify();
        }

    } // end of addWork method

    public void stopThreads() {

        running = false;
        for (int i = 0; i < noOfThreads; i++) {
            if (threadWorkerArr != null && threadWorkerArr[i] != null && threadWorkerArr[i].isAlive()) {
                threadWorkerArr[i].interrupt();
            }
        }
        threadWorkerArr = null;
        workQueue = null;

    } // end of method stopThreads

    private class ThreadPoolWorker extends Thread {

        ThreadPoolWorker(String name) {

            super(name);

        } // end of constructor

        public void run() {

            Runnable r;
            while (true) {
                if (!running) {
                    return;
                }
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
                    r = (Runnable) workQueue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the thread pool could leak threads
                try {
                    r.run();
                } catch (Exception e) {
                    // error
                    logger.debug(e.getMessage() + " while executing the work " + getName());
                }
            }

        } // end of run method

    } // end of class ThreadPoolWorker

} // end of class EmsThreadPool
