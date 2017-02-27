package com.ems.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeartbeatMapQueue extends HashMap<String, byte[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7944885156497176050L;
	
	private static Map<String, byte[]> map1 = new HashMap<String, byte[]>();

	private static Map<String, byte[]> map = Collections.synchronizedMap(map1);
	
	private static HeartbeatMapQueue  instance;
	
	private List<String> keys = new ArrayList<String>();
	
	private HeartbeatMapQueue() {
	}
	
	public static HeartbeatMapQueue  getInstance() {
		if (instance == null) {
            synchronized (HeartbeatMapQueue.class) {
                if (instance == null) {
                    instance = new HeartbeatMapQueue ();
                }
            }
        }
        return instance;
	}
	
	public void add(String key, byte[] pkt) {
		map.put(key, pkt);
	}
	
	public byte[] getRandom() {
		if(keys.size() > 0) {
			String key = keys.remove(0);
			return map.remove(key);
		}
		else {
			keys = new ArrayList<String>(map.keySet());
			if(keys.size() > 0) {
				String key = keys.remove(0);
				return map.remove(key);
			}
		}
		return null;
		
	}

}
