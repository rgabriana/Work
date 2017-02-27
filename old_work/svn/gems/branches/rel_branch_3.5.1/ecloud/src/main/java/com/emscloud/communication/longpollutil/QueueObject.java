package com.emscloud.communication.longpollutil;

import java.util.ArrayList;
import java.util.Random;

import com.communication.types.CloudParamType;
import com.communication.utils.NameValue;

public class QueueObject {
	
	private  short priority = 0;
	private ArrayList<NameValue> nvs = new ArrayList<NameValue>();
	private String uid = null;
	private Boolean addToCache;
	
	
	private static Random r = new Random();
	
	public QueueObject(short priority, CloudParamType type, boolean addToCache) {
		this.priority = priority;
		this.uid = generateUid(type);
		this.addToCache = addToCache;
		addNameValue(CloudParamType.RequestType, type.getName());
		if(addToCache) {
			this.nvs.add(new NameValue(CloudParamType.Uid, this.uid));
		}
	}
	
	public QueueObject(short priority, CloudParamType type) {
		this(priority, type, false);
	}
	
	public static String generateUid(CloudParamType type) {
		return type.getName() + (new Long(r.nextLong())).toString(); 
	}
	
	
	/**
	 * @return the priority
	 */
	public short getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(short priority) {
		this.priority = priority;
	}
	
	public void addNameValue(CloudParamType name, String value) {
		nvs.add(new NameValue(name, value));
	}

	/**
	 * @return the nvs
	 */
	public ArrayList<NameValue> getNvs() {
		return nvs;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the addToCache
	 */
	public Boolean getAddToCache() {
		return addToCache;
	}

	/**
	 * @param addToCache the addToCache to set
	 */
	public void setAddToCache(Boolean addToCache) {
		this.addToCache = addToCache;
	}

}
