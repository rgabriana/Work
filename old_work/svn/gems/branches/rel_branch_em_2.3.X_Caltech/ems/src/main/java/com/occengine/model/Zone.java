/**
 * 
 */
package com.occengine.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author sreedhar.kamishetti
 *
 */
public class Zone implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4759323014325412752L;
	
	private Long id;
	private String name;
	private String location;
	private Integer status;
	private Long startTime;
	private Long endTime;
	
	private Date lastStatusUpdateTime;
	private Integer occStatus;
	private Integer setback;
	private Float minTemperature;
	private Float maxTemperature;
	private Float avgTemperature;	
	private Integer fanSpeed;
	private Date lastReliableTime;
	private Integer heartbeat;	
	private Integer avgAmbientLight;
	private Date setbackStartTime;
	private Date setbackEndTime;
	private Date bmsClientLastHbTime;
	private Integer lastBmsClientSetback;

	private Date lastRestoredTime;
	
	/**
	 * 
	 */
	public Zone() {
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the startTime
	 */
	public Long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
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
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the lastReliableTime
	 */
	public Date getLastReliableTime() {
		return lastReliableTime;
	}

	/**
	 * @param lastReliableTime the lastReliableTime to set
	 */
	public void setLastReliableTime(Date lastReliableTime) {
		this.lastReliableTime = lastReliableTime;
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
	 * @return the lastStatusUpdateTime
	 */
	public Date getLastStatusUpdateTime() {
		return lastStatusUpdateTime;
	}

	/**
	 * @param lastStatusUpdateTime the lastStatusUpdateTime to set
	 */
	public void setLastStatusUpdateTime(Date lastStatusUpdateTime) {
		this.lastStatusUpdateTime = lastStatusUpdateTime;
	}

	/**
	 * @return the setbackStartTime
	 */
	public Date getSetbackStartTime() {
		return setbackStartTime;
	}

	/**
	 * @param setbackStartTime the setbackStartTime to set
	 */
	public void setSetbackStartTime(Date setbackStartTime) {
		this.setbackStartTime = setbackStartTime;
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

	/**
	 * @return the bmsClientLastHbTime
	 */
	public Date getBmsClientLastHbTime() {
		return bmsClientLastHbTime;
	}

	/**
	 * @param bmsClientLastHbTime the bmsClientLastHbTime to set
	 */
	public void setBmsClientLastHbTime(Date bmsClientLastHbTime) {
		this.bmsClientLastHbTime = bmsClientLastHbTime;
	}

	/**
	 * @return the lastBmsClientSetback
	 */
	public Integer getLastBmsClientSetback() {
		return lastBmsClientSetback;
	}

	/**
	 * @param lastBmsClientSetback the lastBmsClientSetback to set
	 */
	public void setLastBmsClientSetback(Integer lastBmsClientSetback) {
		this.lastBmsClientSetback = lastBmsClientSetback;
	}

	/**
	 * @return the lastRestoredTime
	 */
	public Date getLastRestoredTime() {
		return lastRestoredTime;
	}

	/**
	 * @param lastRestoredTime the lastRestoredTime to set
	 */
	public void setLastRestoredTime(Date lastRestoredTime) {
		this.lastRestoredTime = lastRestoredTime;
	}
		
}
