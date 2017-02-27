package com.ems.hvac.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Sensor implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6549504982065636014L;
	@XmlElement(name = "id")
	Long id;
	@XmlElement(name = "name")
	String name;
	@XmlElement(name = "macAddress")
	String macAddress;
	@XmlElement(name = "lastOccupancySeen")
	Integer lastOccupancySeen;
	@XmlElement(name = "currentDimLevel")
	Integer currentDimLevel;
	@XmlElement(name = "outageFlag")
	Boolean outageFlag;
	@XmlElement(name = "lastStatusTime")
	Date lastStatusTime;
	@XmlElement(name = "avgTemperature")
	Short avgTemperature;
	@XmlElement(name = "avgAmbientLight")
	Short avgAmbientLight;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the lastOccupancySeen
	 */
	public Integer getLastOccupancySeen() {
		return lastOccupancySeen;
	}

	/**
	 * @param lastOccupancySeen the lastOccupancySeen to set
	 */
	public void setLastOccupancySeen(Integer lastOccupancySeen) {
		this.lastOccupancySeen = lastOccupancySeen;
	}

	/**
	 * @return the outageFlag
	 */
	public Boolean getOutageFlag() {
		return outageFlag;
	}

	/**
	 * @param outageFlag the outageFlag to set
	 */
	public void setOutageFlag(Boolean outageFlag) {
		this.outageFlag = outageFlag;
	}

	/**
	 * @return the lastStatusTime
	 */
	public Date getLastStatusTime() {
		return lastStatusTime;
	}

	/**
	 * @param lastStatusTime the lastStatusTime to set
	 */
	public void setLastStatusTime(Date lastStatusTime) {
		this.lastStatusTime = lastStatusTime;
	}

	/**
	 * @return the avgTemperature
	 */
	public Short getAvgTemperature() {
		return avgTemperature;
	}

	/**
	 * @param avgTemperature the avgTemperature to set
	 */
	public void setAvgTemperature(Short avgTemperature) {
		this.avgTemperature = avgTemperature;
	}

	/**
	 * @return the avgAmbientLight
	 */
	public Short getAvgAmbientLight() {
		return avgAmbientLight;
	}

	/**
	 * @param avgAmbientLight the avgAmbientLight to set
	 */
	public void setAvgAmbientLight(Short avgAmbientLight) {
		this.avgAmbientLight = avgAmbientLight;
	}

	/**
	 * @return the currentDimLevel
	 */
	public Integer getCurrentDimLevel() {
		return currentDimLevel;
	}

	/**
	 * @param currentDimLevel the currentDimLevel to set
	 */
	public void setCurrentDimLevel(Integer currentDimLevel) {
		this.currentDimLevel = currentDimLevel;
	}

} // end of class Sensor
