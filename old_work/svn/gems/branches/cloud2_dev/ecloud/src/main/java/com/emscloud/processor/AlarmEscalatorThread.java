/**
 * 
 */
package com.emscloud.processor;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Resource;

import com.emscloud.service.FaultManager;

/**
 * @author sreedhar.kamishetti
 *
 */
public class AlarmEscalatorThread extends Thread {

	private static int FIVE_MIN = 5 * 60 * 1000;
	private boolean running = true;
	private long lastAlarmEscalationTime = -1;
	
	@Resource
	private FaultManager faultMgr = null;
	
	/**
	 * 
	 */
	public AlarmEscalatorThread() {
		// TODO Auto-generated constructor stub
		
		lastAlarmEscalationTime = System.currentTimeMillis() - FIVE_MIN;
		
	} //end of constructor
	
	public void run() {
		
		while(true) {
			if(!running) {
				return;
			}
			long currentTime = System.currentTimeMillis();
			try {				
				ArrayList<String> alarmList = faultMgr.getAllAlarms(new Date(lastAlarmEscalationTime), 
						new Date(currentTime));
				lastAlarmEscalationTime = currentTime;
				//forward the events to HEMS
				Thread.sleep(FIVE_MIN);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
	} //end of method run
	
	private void forwardAlarms(ArrayList<String> alarmList) {
		
		//need to integrate with HEMS
		
	} //end of method forwardAlarms
	
} //end of class AlarmEscalatorThread
