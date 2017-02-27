package com.emscloud.communication.longpollutil;

import org.apache.log4j.Logger;

import com.communication.types.CloudParamType;

public class NewRequest {
	
	static final Logger logger = Logger.getLogger(NewRequest.class.getName());
	
	public static String setHBRequest(String mac, short priority, String fixtures, Short enableHb, Short enableRealTime, Short triggerDealyTime, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.SetHB, addToCache);
		qo.addNameValue(CloudParamType.PayLoad, fixtures + "::::" + enableHb + "::::" + enableRealTime + "::::" + triggerDealyTime);
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);
	}

	
	public static String setDimLevelRequest(String mac, short priority, String fixtures, int percentage, int time, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.SetDimLevel, addToCache);
		qo.addNameValue(CloudParamType.PayLoad, fixtures + "::::" + percentage + "::::" + time);
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);
	}
	
	
	public static String setRequestDimLevelAndLastConnectivity(String mac, short priority, String fixtures, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.RequestDimLevelAndLastConnectivity, addToCache);
		qo.addNameValue(CloudParamType.PayLoad, fixtures);
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);
	}
	
	
	public static String  setRequestFloorPlan(String mac, short priority, Long floorId, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.RequestFloorPlan, addToCache);
		qo.addNameValue(CloudParamType.PayLoad, floorId.toString());
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);		
	}
	
	public static String setRequestSensor(String mac, short priority, String macId, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.RequestSensor, addToCache);
		qo.addNameValue(CloudParamType.PayLoad, macId);
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);		
	}
	
	
	public static String setRequestAllSensors(String mac, short priority, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.RequestAllSensors, addToCache);
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);		
	}
	
	public static String setRequestFacilityTree(String mac, short priority, boolean addToCache) {
		QueueObject qo = new QueueObject(priority, CloudParamType.RequestFacilityTree, addToCache);
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);		
	}	

	
	public static String taskChange(String mac, short priority) {
		QueueObject qo = new QueueObject(priority, CloudParamType.TaskChange);
		if(!RequestsBlockingPriorityQueue.getMap().containsKey(mac.toUpperCase().replaceAll(":", ""))) {
			RequestsBlockingPriorityQueue queue = new RequestsBlockingPriorityQueue(mac.toUpperCase().replaceAll(":", ""));
			RequestsBlockingPriorityQueue.getMap().put(queue.getMacId(), queue);
		}
		return RequestsBlockingPriorityQueue.getMap().get(mac.replaceAll(":", "")).addToQueue(qo);
	}
	
	public static class AddEmTaskToQueue implements Runnable {
		
		private String macId = null;
		private Short priority = 0;
		private Long wait = 2000L;
		public AddEmTaskToQueue(String macId, Short priority) {
			this.macId = macId;
			this.priority = priority;
		}
		public AddEmTaskToQueue(String macId, Short priority, Long wait) {
			this.macId = macId;
			this.priority = priority;
			this.wait = wait;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
			}
			NewRequest.taskChange(macId, priority);
		}
	}
	
}
