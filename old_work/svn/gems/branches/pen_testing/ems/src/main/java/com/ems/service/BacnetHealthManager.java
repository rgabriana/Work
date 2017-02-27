package com.ems.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("bacnetHealthManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BacnetHealthManager {
	private static final Logger log = Logger.getLogger(BacnetHealthManager.class);
	private long lastBacnetConnectivityAt = -1;

	public long getLastBacnetConnectivityAt() {
		return lastBacnetConnectivityAt;
	}

	public void setLastBacnetConnectivityAt(long lastBacnetConnectivityAt) {
		this.lastBacnetConnectivityAt = lastBacnetConnectivityAt;
	}
	
	public boolean isBacnetRunning()
	{
		boolean isRunning = false;
		long ts = lastBacnetConnectivityAt;
		long now = System.currentTimeMillis();
		long diff = now - ts;
		long diffInMinutes  = (diff/1000)/60;
		if(diffInMinutes<=15)
		{
			isRunning = true;
		}else
		{
			log.error("Bacnet seems to be disconnected for more than "+ diffInMinutes +" minutes. Please check with bacnet lighting application logs for more details.");
		}
		//System.out.println("diff in Minutes"+ diffInMinutes + " Is Running " + isRunning);
		return isRunning;
	}
}
