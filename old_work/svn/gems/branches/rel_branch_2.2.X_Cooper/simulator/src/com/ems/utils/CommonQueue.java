package com.ems.utils;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import com.ems.su.Packets;

/**
 * @author SAMEER SURJIKAR
 * 
 * 
 */
public class CommonQueue {
    private Queue<Packets> contentq;
    private Object waitNotifyLock = new Object();

    public synchronized Object getWaitNotifyLock() {
        return waitNotifyLock;
    }

    public CommonQueue() {

        contentq = new PriorityBlockingQueue<Packets>();

    }

    public synchronized boolean push(Packets queueElement) {
        contentq.add(queueElement);
        synchronized (this.waitNotifyLock) {
            this.waitNotifyLock.notifyAll();
        }

        return true;

    }

    public synchronized Packets pop() {
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
