package com.ems.occengine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class SensorProcessor {
	
	static final Logger logger = Logger.getLogger(SensorProcessor.class.getName());
	
	static SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	
	public static boolean isHeartbeatFailure(SensorEventVO sensorEventVO, int failedCnts, int failedCntsOutOf) {
		int missedBits = 0;
		int temp = 1;
		int tempAndSensor = 0;
		synchronized (sensorEventVO) {
			for(int i = 0; i < failedCntsOutOf; i++) {
				tempAndSensor = 0;
				tempAndSensor = sensorEventVO.getHbCnt() & temp;
				if (tempAndSensor == 0) {
					missedBits++;
				}
				temp = temp << 1;
			}
		}
		boolean isFail = (failedCnts <= missedBits) && 
						(sensorEventVO.getLastRealTimeStateChange() == null || 
							sensorEventVO.getLastRealTimeStateChange().before(new Date((new Date()).getTime() - sensorEventVO.getHbInterval()*1000 - 10))); 
		sensorEventVO.setHeartbeatFailure(isFail);
		if(sensorEventVO.isHeartbeatFailure()) {
			sensorEventVO.setFailureCnt(sensorEventVO.getFailureCnt() + 1);
		}
		else {
			sensorEventVO.setFailureCnt(0);
		}
		sensorEventVO.print(getBits(sensorEventVO.getQueue()));
		return failedCnts <= missedBits;
	}
	
	public static void add(SensorEventVO sensorEventVO, byte occ, int numberOfBits) {
		synchronized(sensorEventVO) {
			fillMissingBits(sensorEventVO);
			addBits(sensorEventVO, occ, numberOfBits);
			if(sensorEventVO.getLastUpdated() == null) {
				long currentMillis = new Date().getTime();
				long hbTime = sensorEventVO.getHbInterval() * 1000;
				currentMillis = currentMillis - (currentMillis % hbTime) - hbTime;
				sensorEventVO.setLastUpdated(new Date(currentMillis));
			}
			sensorEventVO.setLastUpdated( new Date(sensorEventVO.getLastUpdated().getTime() + numberOfBits * 5 * 1000));
		}
		
	}
	
	private static void addBits(SensorEventVO sensorEventVO, byte occ, int numberOfBits) {
		int spaceLeft = 64 - sensorEventVO.getBitCnt();
		if(spaceLeft >= numberOfBits) {
			for(int a = 1; a <= numberOfBits; a++) {
				sensorEventVO.getQueue()[0] = sensorEventVO.getQueue()[0] << 1 | occ;
			}
			sensorEventVO.setBitCnt(sensorEventVO.getBitCnt() + numberOfBits);
		}
		else {
			int leftOver = numberOfBits - spaceLeft;
			for(int a = 1; a <= spaceLeft; a++) {
				sensorEventVO.getQueue()[0] = sensorEventVO.getQueue()[0] << 1 | occ;
				sensorEventVO.setBitCnt(sensorEventVO.getBitCnt() + 1);
			}
			for(int b = sensorEventVO.getQueue().length; b > 1; b--) {
				sensorEventVO.getQueue()[b-1] = sensorEventVO.getQueue()[b-2];
			}
			sensorEventVO.setBitCnt(0);
			sensorEventVO.getQueue()[0] = 0L;
			for(int a = 1; a <= leftOver; a++) {
				sensorEventVO.getQueue()[0] = sensorEventVO.getQueue()[0] << 1 | occ;
				sensorEventVO.setBitCnt(sensorEventVO.getBitCnt() + 1);
			}
			sensorEventVO.setBitCnt(leftOver);
		}
	}
	
	public static void fillMissingBits(SensorEventVO sensorEventVO) {
		while(sensorEventVO.getLastUpdated() != null && sensorEventVO.getLastUpdated().before(new Date((new Date()).getTime() - 2 * sensorEventVO.getHbInterval() * 1000 - 10*1000))) {
			sensorEventVO.setHbCnt(sensorEventVO.getHbCnt() << 1);
			byte adjustByte = sensorEventVO.getOccStatus();
			addBits(sensorEventVO, adjustByte, sensorEventVO.getHbInterval()/5);
			sensorEventVO.setLastUpdated(new Date(sensorEventVO.getLastUpdated().getTime() + sensorEventVO.getHbInterval() * 1000));
		}
	}
	
	public static void tempBitAdjust(SensorEventVO sensorEventVO) {
		Long extraBits = 0L;
		Long interBits = 0L;
		Byte occ = sensorEventVO.getOccStatus();
		Date lastUpdated = sensorEventVO.getLastUpdated();
		Date lastRT = sensorEventVO.getLastRealTimeStateChange();
		
		if (lastRT != null && lastUpdated != null) {
			if(lastRT.after(lastUpdated)) {
				extraBits = ((new Date()).getTime() - lastRT.getTime())/(1000*5);
				interBits = (lastRT.getTime() - lastUpdated.getTime())/(1000*5);
			}
			else {
				extraBits = ((new Date()).getTime() - lastUpdated.getTime())/(1000*5);
			}
		}
		else if(lastRT == null && lastUpdated != null) {
			extraBits = ((new Date()).getTime() - lastUpdated.getTime())/(1000*5);
		}
		else if (lastRT != null && lastUpdated == null) {
			extraBits = ((new Date()).getTime() - lastRT.getTime())/(1000*5);
		}
		if(interBits.intValue() > 0) {
			addBits(sensorEventVO, occ == CommandsConstants.OCCUPIED ? CommandsConstants.UNOCCUPIED: CommandsConstants.OCCUPIED, interBits.intValue());
		}
		if (extraBits.intValue() > 0) {
			addBits(sensorEventVO, occ, extraBits.intValue());
		}
	}
		
	public static Long getLong(SensorEventVO sensorEventVO, int index) {
		Long out = 0L;
		if( index == 0) {
			out = sensorEventVO.getQueue()[0];
			if(sensorEventVO.getBitCnt() < 64) {
				out = out | (sensorEventVO.getQueue()[1] << sensorEventVO.getBitCnt());
				if(logger.isDebugEnabled()) {
					logger.debug(sensorEventVO.getMac() + ":: " + "Index " + index + " bits :" + Long.toBinaryString(out));
				}
			}
		}
		else {
			out = sensorEventVO.getQueue()[index] >>> (64 - sensorEventVO.getBitCnt());
			if(sensorEventVO.getBitCnt() < 64) {
				out = out | sensorEventVO.getQueue()[++index] << (sensorEventVO.getBitCnt());
			}
			if(logger.isDebugEnabled()) {
				logger.debug(sensorEventVO.getMac() + ":: " + "Index " + index + " bits :" + Long.toBinaryString(out));
			}
		}
		return out;
		
	}
	
	public static void updateMotionSecAgo_5Mnt(SensorEventVO sensorEventVO, List<Integer> motionSecAgo) {
		for(int i = 2; i > -1; i--) {
			if(motionSecAgo.get(i) == 101) {
				sensorEventVO.setMotionSecAgo(sensorEventVO.getMotionSecAgo() == -1 ? 101 : (sensorEventVO.getMotionSecAgo() + 100 > 65535 ? 65535 : sensorEventVO.getMotionSecAgo() + 100));
			}
			else {
				sensorEventVO.setMotionSecAgo(motionSecAgo.get(i));
			}
			if(sensorEventVO.getLastRealTimeStateChange() == null ||  sensorEventVO.getLastUpdated() ==  null || sensorEventVO.getLastRealTimeStateChange().before(sensorEventVO.getLastUpdated())) {
				if (sensorEventVO.getMotionSecAgo() <= sensorEventVO.getOccFrequency())
					sensorEventVO.setOccStatus(CommandsConstants.OCCUPIED);
				else
					sensorEventVO.setOccStatus(CommandsConstants.UNOCCUPIED);
			}
			
			add(sensorEventVO, sensorEventVO.getOccStatus(), 20);
		}
		sensorEventVO.setHbCnt((sensorEventVO.getHbCnt() << 1) | 1);
	}
	
	public static String getBits(Long[] queue) {
		StringBuffer sb = new StringBuffer();
		for(int a = 2; a >= 0; a--) {
			sb.append(Long.toBinaryString(queue[a])).append("##");
		}
		return sb.toString();
	}

}
