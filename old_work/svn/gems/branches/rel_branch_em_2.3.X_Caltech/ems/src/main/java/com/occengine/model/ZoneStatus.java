/**
 * 
 */
package com.occengine.model;

import java.util.Date;

/**
 * @author sreedhar.kamishetti
 *
 */
public class ZoneStatus {

	private Long id;
	private Integer occStatus;
	private Integer setback;
	private Float minTemperature;
	private Float maxTemperature;
	private Float avgTemperature;	
	private Integer fanSpeed;
	private Date lastTimestamp;
	private Integer heartbeat;	
	private Integer avgAmbientLight;
	private Date setbackEndTime;
	
	/**
	 * 
	 */
	public ZoneStatus() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the occStatus
	 */
	public Integer getOccStatus() {
		return occStatus;
	}

	/**
	 * @param occStatus the occStatus to set
	 */
	public void setOccStatus(Integer occStatus) {
		this.occStatus = occStatus;
	}

	/**
	 * @return the setback
	 */
	public Integer getSetback() {
		return setback;
	}

	/**
	 * @param setback the setback to set
	 */
	public void setSetback(Integer setback) {
		this.setback = setback;
	}

	/**
	 * @return the minTemperature
	 */
	public Float getMinTemperature() {
		return minTemperature;
	}

	/**
	 * @param minTemperature the minTemperature to set
	 */
	public void setMinTemperature(Float minTemperature) {
		this.minTemperature = minTemperature;
	}

	/**
	 * @return the maxTemperature
	 */
	public Float getMaxTemperature() {
		return maxTemperature;
	}

	/**
	 * @param maxTemperature the maxTemperature to set
	 */
	public void setMaxTemperature(Float maxTemperature) {
		this.maxTemperature = maxTemperature;
	}

	/**
	 * @return the avgTemperature
	 */
	public Float getAvgTemperature() {
		return avgTemperature;
	}

	/**
	 * @param avgTemperature the avgTemperature to set
	 */
	public void setAvgTemperature(Float avgTemperature) {
		this.avgTemperature = avgTemperature;
	}

	/**
	 * @return the fanSpeed
	 */
	public Integer getFanSpeed() {
		return fanSpeed;
	}

	/**
	 * @param fanSpeed the fanSpeed to set
	 */
	public void setFanSpeed(Integer fanSpeed) {
		this.fanSpeed = fanSpeed;
	}

	/**
	 * @return the lastTimestamp
	 */
	public Date getLastTimestamp() {
		return lastTimestamp;
	}

	/**
	 * @param lastTimestamp the lastTimestamp to set
	 */
	public void setLastTimestamp(Date lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	/**
	 * @return the heartbeat
	 */
	public Integer getHeartbeat() {
		return heartbeat;
	}

	/**
	 * @param heartbeat the heartbeat to set
	 */
	public void setHeartbeat(Integer heartbeat) {
		this.heartbeat = heartbeat;
	}

	/**
	 * @return the avgAmbientLight
	 */
	public Integer getAvgAmbientLight() {
		return avgAmbientLight;
	}

	/**
	 * @param avgAmbientLight the avgAmbientLight to set
	 */
	public void setAvgAmbientLight(Integer avgAmbientLight) {
		this.avgAmbientLight = avgAmbientLight;
	}

	/**
	 * @return the setbackEndTime
	 */
	public Date getSetbackEndTime() {
		return setbackEndTime;
	}

	/**
	 * @param setbackEndTime the setbackEndTime to set
	 */
	public void setSetbackEndTime(Date setbackEndTime) {
		this.setbackEndTime = setbackEndTime;
	}

}
