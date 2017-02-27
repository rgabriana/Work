/**
 * 
 */
package com.ems.utils;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yogesh
 * 
 */
public class SUMsgFromGemsQueue {
    private Queue<byte[]> contentq;
    private Object waitNotifyLock = new Object();

    public synchronized Object getWaitNotifyLock() {
        return waitNotifyLock;
    }

    public SUMsgFromGemsQueue() {

        contentq = new LinkedBlockingQueue<byte[]>();

    }

    public synchronized boolean push(byte[] queueElement) {
        contentq.add(queueElement);
        synchronized (this.waitNotifyLock) {
            this.waitNotifyLock.notifyAll();
        }

        return true;

    }

    public synchronized byte[] pop() {
        if (!contentq.isEmpty()) {
            return contentq.remove();
        } else {
            return null;
        }
    }

    public synchronized boolean isQueueEmpty() {
        return contentq.isEmpty();
    }

    public synchronized Iterator getContentQueueIterator() {
        if (!contentq.isEmpty()) {
            Iterator iterator = contentq.iterator();
            return iterator;
        } else {
            return null;
        }
    }

}
