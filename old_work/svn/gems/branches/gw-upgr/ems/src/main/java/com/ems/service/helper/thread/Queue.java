package com.ems.service.helper.thread;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.ems.service.EmailManager;

public class Queue<Item> implements Iterable<Item> {
	private static final Logger log = Logger.getLogger(Queue.class);
	
	private int N;         // number of elements on queue
    private Node first;    // beginning of queue
    private Node last;     // end of queue

    private final int max_size;
    public long endTime = 0;
    private final EmailManager manager;
    private final Object LOCK_ENQUE = new Object();
    // helper linked list class
    private class Node {
        private Item item;
        private Node next;
    }

    // create an empty queue
    public Queue(int maxSize, EmailManager manager) {
    	this.max_size = maxSize;
        first = null;
        last  = null;
        this.manager = manager;
        
    }

    // is the queue empty?
    public synchronized boolean isEmpty() { return first == null; }
    public synchronized int length()      { return N;             }
    public synchronized int size()        { return N;             }

    // add an item to the queue
    public void enqueue(Item item) {
    	
    	try{
    		boolean isWarn = false;
    		synchronized(this){
    			isWarn = (N > (max_size * 0.7) );
    		}
	    	if(isWarn){
	        	log.warn("ERROR: ERROR: ERROR: Queue seems to be getting enqued only and getting full");
//	        	for(int i=1; i < 2; i++){
//	        		manager.addNewThread();
//	        	}
	        	synchronized(LOCK_ENQUE){
	        		LOCK_ENQUE.wait();
	        	}
	        }
	    	synchronized(this){
		    	if(N < max_size){
			        Node x = new Node();
			        x.item = item;
			        if (isEmpty()) { first = x;     last = x; }
			        else           { last.next = x; last = x; }
			        N++;
			       // System.err.println("PUSHED : "+ (x.item).toString());
		    	}else{
		    		log.error("PROBLEM: EXCEPTION: ERROR: ERROR: ERROR: QUEUE IS FULL. NO MORE ITEMS CAN BE ENQUEUED IN THIS QUEUE.......");
		    	}
		        synchronized(this)
		        {
		        	this.notifyAll();
		        }
	    	}
    	} catch (InterruptedException e) {
			log.error("ERROR OCCURED in Queue:",e);
		}
    }

    // remove and return the least recently added item
    public Item dequeue() {
    	
		Item item = null;
		item = getItem();
		synchronized(LOCK_ENQUE){
    		LOCK_ENQUE.notify();
    	}
        return item;
    }
    
    private synchronized Item getItem(){
    	Item item = null;
    	try{
	    	if(isEmpty()){
	    		synchronized(LOCK_ENQUE){
		    		LOCK_ENQUE.notify();
		    	}
	    		wait();
	    		return getItem();
	    	}
	    	item = first.item;
			first = first.next;
			N--;
	    } catch (InterruptedException e) {
			log.error("ERROR OCCURED in Queue:",e);
		}
		return item;
    }

    public Iterator<Item> iterator()  { return new QueueIterator();  }

    // an iterator, doesn't implement remove() since it's optional
    private class QueueIterator implements Iterator<Item> {
        private Node current = first;

        public boolean hasNext()  { return current != null; }
        public void remove() { throw new UnsupportedOperationException(); }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            Item item = current.item;
            current = current.next; 
            return item;
        }
    }



}
