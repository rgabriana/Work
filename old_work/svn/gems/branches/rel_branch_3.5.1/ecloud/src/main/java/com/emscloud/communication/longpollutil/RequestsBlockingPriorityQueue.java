package com.emscloud.communication.longpollutil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.communication.types.CloudParamType;
import com.communication.utils.NameValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class RequestsBlockingPriorityQueue {
	
	public static final Logger logger = Logger.getLogger(RequestsBlockingPriorityQueue.class.getName());
	
	private PriorityBlockingQueue<QueueObject> queue = new PriorityBlockingQueue<QueueObject>(10, priorityComparator);
	
	private static Map<String, RequestsBlockingPriorityQueue> map = new HashMap<String, RequestsBlockingPriorityQueue>();
	
	private static Map<String, QueueObject> lastReq = new HashMap<String, QueueObject>(); 
	
	private String macId = null;
	
	public static final String waitingForValue = "-NULL-";
	
	private static Cache<String, String> cache = CacheBuilder.newBuilder()
														.expireAfterWrite(1, TimeUnit.MINUTES)
														.maximumSize(10000)
														.concurrencyLevel(2)
														.build();
	

	public RequestsBlockingPriorityQueue(String macId) {
		this.setMacId(macId);
		QueueObject qo = new QueueObject((short)0, CloudParamType.TaskChange);
		taskChange(qo);
	}
	
	//Comparator anonymous class implementation
    public static Comparator<QueueObject> priorityComparator = new Comparator<QueueObject>(){
        @Override
        public int compare(QueueObject q1, QueueObject q2) {
            return (int) (q2.getPriority() - q1.getPriority());
        }
    };
    
    public String addToQueue(QueueObject qo) {
    	if(queue.size() > 100) {
    		logger.warn("Request queue for EM " + macId +  " is too long (size = " + queue.size()  + ") .  Cleaning up.");
    		while(queue.size() > 50) {
        		QueueObject q = queue.remove();
        		logger.info("Removed = " + q.getPriority() + ", " + q.getNvs().get(0).getValue());
        	}
    	}
    	if(qo.getAddToCache()) {
    		cache.put(qo.getUid(), waitingForValue);
    	}
    	if(queue.offer(qo)) {
    		return qo.getUid();
    	}
    	else {
    		return null;
    	}
    }
    
    public boolean taskChange(QueueObject qo) {
    	if(queue.size() == 0) {
    		if(addToQueue(qo) != null) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	return true;
    }
    
    public ArrayList<NameValue> getNameValues() {    
    	QueueObject qo = queue.poll();
		if(qo != null && qo.getNvs() != null) {
			if(CloudParamType.TaskChange.getName().equals(qo.getNvs().get(0).getValue())) {
				return new ArrayList<NameValue>();
			}
			lastReq.put(macId, qo);
			return qo.getNvs();
		}
    	return new ArrayList<NameValue>();
    	
    }
    
    

	/**
	 * @return the map
	 */
	public static Map<String, RequestsBlockingPriorityQueue> getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public static void setMap(Map<String, RequestsBlockingPriorityQueue> map) {
		RequestsBlockingPriorityQueue.map = map;
	}
	
	

	/**
	 * @return the macId
	 */
	public String getMacId() {
		return macId;
	}

	/**
	 * @param macId the macId to set
	 */
	public void setMacId(String macId) {
		this.macId = macId;
	}
	

	/**
	 * @return the lastReq
	 */
	public static Map<String, QueueObject> getLastReq() {
		return lastReq;
	}
    
	/**
	 * @return the cache
	 */
	public static Cache<String, String> getCache() {
		return cache;
	}
    
 

}

