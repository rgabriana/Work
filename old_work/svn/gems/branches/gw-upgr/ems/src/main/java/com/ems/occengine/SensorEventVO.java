package com.ems.occengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

public class SensorEventVO extends Observable implements Serializable {
	
	public static final Logger logger = Logger
			.getLogger(SensorEventVO.class.getName());
	
	private static final long serialVersionUID = 1L;
	private String mac;
	private int motionSecAgo = -1;
	private short ambLight = -1;
	private int currVolt = -1;
	private short fxTemp = -1;
	private int power = -1;
	
	private int failureCnt = 0;

	private byte occStatus = CommandsConstants.UNOCCUPIED;
	private Date lastPMStatCommunication = null;
	
	private Date lastRealTimeStateChange = null;

	public final static String OCC_UPDATE = "OCC_UPDATE";

	private int occFrequency = 30;
	
	private List<Long> zoneList = new ArrayList<Long>();
	
	private boolean heartbeatFailure = false;
	
	private Integer bitCnt = 0;
	
	private Date lastUpdated = null;
	
	private int hbInterval = 30;
	
	private Integer hbCnt = 0;
	
	private Long[] queue = {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};
	
	private Boolean hbEnabled = false;
	
	private Boolean publishHBEnabledEvent = false;

	public SensorEventVO() {

	}
	
	public SensorEventVO clone() {
		SensorEventVO vo = new SensorEventVO();
		vo.setBitCnt(bitCnt);
		vo.setOccStatus(occStatus);
		vo.lastUpdated = lastUpdated;
		vo.setLastRealTimeStateChange(lastRealTimeStateChange);
		vo.setMac(mac);
		vo.setQueue(queue.clone());
		vo.setHeartbeatFailure(heartbeatFailure);
		return vo;
	}

	public SensorEventVO(String sName, int occFrequency, int hbInterval) {
		this.mac = sName;
		this.occFrequency = occFrequency;
		this.hbInterval = hbInterval;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public int getMotionSecAgo() {
		return motionSecAgo;
	}

	public void setMotionSecAgo(int motionSecAgo) {
		this.motionSecAgo = motionSecAgo;
	}

	public short getAmbLight() {
		return ambLight;
	}

	public void setAmbLight(short ambLight) {
		this.ambLight = ambLight;
	}

	public int getCurrVolt() {
		return currVolt;
	}

	public void setCurrVolt(int currVolt) {
		this.currVolt = currVolt;
	}

	public short getFxTemp() {
		return fxTemp;
	}

	public void setFxTemp(short fxTemp) {
		this.fxTemp = fxTemp;
	}

	public byte getOccStatus() {
		return occStatus;
	}

	public void setOccStatus(byte occStatus) {
		this.occStatus = occStatus;
	}

	public Date getLastPMStatCommunication() {
		return lastPMStatCommunication;
	}

	public void updateLastPMStatCommunication() {
		this.lastPMStatCommunication = new Date();
	}

	public List<Long> getZoneList() {
		return zoneList;
	}

	public void setZoneList(List<Long> zoneList) {
		this.zoneList = zoneList;
	}
	
	public void addZone(Long zoneId) {
		if (!zoneList.contains(zoneId)) {
			zoneList.add(zoneId);
		}
	}

	/**
	 * @return the occFrequency
	 */
	public int getOccFrequency() {
		return occFrequency;
	}

	/**
	 * @param occFrequency the occFrequency to set
	 */
	public void setOccFrequency(int occFrequency) {
		this.occFrequency = occFrequency;
	}

	/**
	 * @return the heartbeatFailure
	 */
	public boolean isHeartbeatFailure() {
		return heartbeatFailure;
	}

	/**
	 * @param heartbeatFailure the heartbeatFailure to set
	 */
	public void setHeartbeatFailure(boolean heartbeatFailure) {
		this.heartbeatFailure = heartbeatFailure;
	}

	public void setLastPMStatCommunication(Date lastPMStatCommunication) {
		this.lastPMStatCommunication = lastPMStatCommunication;
	}

	public void print(String motionBits) {
		logger.info("Sensor Info (cron): mac = " + this.mac + 
				", motionSecAgo = " + this.motionSecAgo + 
				", occStatus = " + this.occStatus +
				", lastPMStatCommunication = " + this.lastPMStatCommunication +
				", lastRealTimeStateChange = " + this.lastRealTimeStateChange +
				", heartbeatFailure = " + this.heartbeatFailure +
				", bitCnt = " + this.bitCnt +
				", lastUpdated = " + this.lastUpdated +
				", hbCnt = " + Integer.toBinaryString(this.hbCnt) +
				", motionBits = " +  motionBits);
	}

	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}

	/**
	 * @param power the power to set
	 */
	public void setPower(int power) {
		this.power = power;
	}

	/**
	 * @return the publishHBEnabledEvent
	 */
	public Boolean getPublishHBEnabledEvent() {
		return publishHBEnabledEvent;
	}

	/**
	 * @param publishHBEnabledEvent the publishHBEnabledEvent to set
	 */
	public void setPublishHBEnabledEvent(Boolean publishHBEnabledEvent) {
		this.publishHBEnabledEvent = publishHBEnabledEvent;
	}

	/**
	 * @return the bitCnt
	 */
	public Integer getBitCnt() {
		return bitCnt;
	}

	/**
	 * @param bitCnt the bitCnt to set
	 */
	public void setBitCnt(Integer bitCnt) {
		this.bitCnt = bitCnt;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @return the hbInterval
	 */
	public int getHbInterval() {
		return hbInterval;
	}

	/**
	 * @param hbInterval the hbInterval to set
	 */
	public void setHbInterval(int hbInterval) {
		this.hbInterval = hbInterval;
	}

	/**
	 * @return the hbCnt
	 */
	public Integer getHbCnt() {
		return hbCnt;
	}

	/**
	 * @param hbCnt the hbCnt to set
	 */
	public void setHbCnt(Integer hbCnt) {
		this.hbCnt = hbCnt;
	}

	/**
	 * @return the queue
	 */
	public Long[] getQueue() {
		return queue;
	}

	/**
	 * @param queue the queue to set
	 */
	public void setQueue(Long[] queue) {
		this.queue = queue;
	}

	/**
	 * @return the hbEnabled
	 */
	public Boolean getHbEnabled() {
		return hbEnabled;
	}

	/**
	 * @param hbEnabled the hbEnabled to set
	 */
	public void setHbEnabled(Boolean hbEnabled) {
		this.hbEnabled = hbEnabled;
	}

	/**
	 * @return the failureCnt
	 */
	public int getFailureCnt() {
		return failureCnt;
	}

	/**
	 * @param failureCnt the failureCnt to set
	 */
	public void setFailureCnt(int failureCnt) {
		this.failureCnt = failureCnt;
	}

	public Date getLastRealTimeStateChange() {
		return lastRealTimeStateChange;
	}

	public void setLastRealTimeStateChange(Date lastRealTimeStateChange) {
		this.lastRealTimeStateChange = lastRealTimeStateChange;
	}
	
	public void updateLastRealTimeStateChange() {
		this.lastRealTimeStateChange = new Date();
	}
	

}
