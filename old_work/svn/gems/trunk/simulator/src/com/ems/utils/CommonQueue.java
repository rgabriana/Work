package com.ems.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
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

    public Object getWaitNotifyLock() {
        return waitNotifyLock;
    }

    public CommonQueue() {

        contentq = new PriorityBlockingQueue<Packets>(1000, packetSorter);

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

    public synchronized void flush() {
        contentq.clear();
    }
    
    public synchronized int size() {
        return contentq.size();
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

    public static Comparator<Packets> packetSorter = new Comparator<Packets>() {
        @Override
        public int compare(Packets p1, Packets p2) {
            return p2.getiPriority() - p1.getiPriority();
        }
    };
}
