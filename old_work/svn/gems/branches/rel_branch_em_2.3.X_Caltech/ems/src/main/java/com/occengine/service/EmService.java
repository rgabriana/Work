/**
 * 
 */
package com.occengine.service;

import javax.annotation.Resource;

import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.ws.OccupancyService;
import com.occengine.OccupancyEngine;
import com.occengine.types.OccupancyStatus;
import com.occengine.utils.OccUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
public class EmService {

	private static EmService instance = null;

  private OccupancyService occService = new OccupancyService();
	
	/**
	 * 
	 */
	private EmService() {
		// TODO Auto-generated constructor stub
	}

	public static EmService getInstance() {
		
		if(instance == null) {
			synchronized(EmService.class) {
				if(instance == null) {
					instance = new EmService();
				}
			}
		}
		return instance;
		
	} //end of method getInstance
	
	public void turnOnGreen(String mac, int rglPeriod) {
		 
		//System.out.println(mac + " : turn on green ");
		occService.dimFixture(mac, 100, rglPeriod);
		
	} //end of method turnOnGreen
	
	public void turnOnRed(String mac, int rglPeriod) {
		
		//System.out.println(mac + " : turn on red ");
		occService.dimFixture(mac, 0, rglPeriod);
		
	} //end of method turnOnREd
	
	public void turnOnGreenRed(String mac, int rglPeriod) {
		
		//System.out.println(mac + " : turn on red ");
		occService.dimFixture(mac, ServerConstants.BMS_CLIENT_HB_STATE_ENUM, rglPeriod);
		
	} //end of method turnOnGreenRed
	
	public void configureHeartbeat(String mac, int hbPeriod) {
		
		occService.configureHeartbeat(mac, hbPeriod);
				
	} //end of method configureHeartbeat
	
	public void designateRGL(String mac, byte enableDisable) {
		
		occService.designateRGL(mac, enableDisable);
		
	} //end of method designateRGL
		
	public void heartbeat(String mac, byte[] packet, int tempOffset) {
	
		//System.out.println(mac + ": heartbeat event " + OccUtil.getLogPacket(packet));

		int pktIndex = 12; //starting packet body position
		pktIndex++; // 4th current state
		pktIndex++; // 5th current volt
		byte currTemp = packet[pktIndex++]; // 6th
		byte[] tempShortByteArr = new byte[2];
		// amb light bytes 7,8
		System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
		pktIndex += 2;
		short ambLight = (short) OccUtil.extractShortFromByteArray(tempShortByteArr, 0);
		
		// motion secs ago bytes 9,10
		System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
		pktIndex += 2;
		int motionSecAgo = OccUtil.extractShortFromByteArray(tempShortByteArr, 0);
		OccupancyStatus occStatus = OccupancyStatus.OCCUPIED;
		if(motionSecAgo > 2) {
			//no motion in the last 2 seconds
			occStatus = OccupancyStatus.VACANT;
		}
				
		// current time bytes 11,12,13, 14		
		pktIndex += 4;				
		// up time bytes 15,16,17,18		
		pktIndex += 4;	
		// byte 19 global profile checksum
		pktIndex++;	
		// byte 20 profile checksum
		pktIndex++;
		// bytes 21, 22 to off timer
		pktIndex += 2;
		// bytes 23,24 energy ticks		
		pktIndex += 2;	
		// bytes 25, 26, 27, 28 pulse duration time in milli sec		
		pktIndex += 4;		
		// bytes 29, 30 calib value		
		pktIndex += 2;
		// byte 31 is hopper
		pktIndex++;
		//byte 32 is group id
		pktIndex++;
	    
    float fxTemp = (short) (((currTemp) * 9 / 5) + 32 - tempOffset);
    	
    if(ServerMain.enableOccEngine) {
		OccupancyEngine.getInstance().updateOccStatus(mac, motionSecAgo, occStatus, ambLight, fxTemp);
    }
		
	} //end of method heartbeat
	
} //end of class EmsService
