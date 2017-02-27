/**
 * 
 */
package com.ems.occengine;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ZoneEventVO extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static final Logger logger = Logger.getLogger(ZoneEventVO.class.getName());
	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElementWrapper(name = "sensors")
	@XmlElement(name = "sensor")
	private List<String> sensorList;
	@XmlElement(name = "topic")
	private String topic;
	@XmlElement(name = "rules")
	private Rules oRule;
	
	@XmlElement(name = "occStatus")
	private byte occStatus = CommandsConstants.UNOCCUPIED;
	
	private byte init = 0;
	
	@XmlElement(name = "stateChangeDate")
	private Date stateChangeDate = new Date();
	
	@XmlElement(name = "lastCommunication")
	private Date lastCommunication;
	
	@XmlElement(name = "zoneFailure")
	private boolean zoneFailure = false;
	
	@XmlElement(name = "avgTemp")
	private int avgTemp;
	@XmlElement(name = "minTemp")
	private int minTemp;
	@XmlElement(name = "maxTemp")
	private int maxTemp;
	@XmlElement(name = "avgDimLevel")
	private int avgDimLevel;
	@XmlElement(name = "avgPower")
	private int avgPower;
	@XmlElement(name = "floorName")
	private String floorName;

	@XmlElement(name = "buildingName")
	private String buildingName;
	
	@XmlElement(name = "percentOccupancy")
	private Integer percentOccupancy;
	
	
	private boolean override;
	
	public ZoneEventVO() {
	}
	

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

	public List<String> getSensorList() {
		return sensorList;
	}

	public void setSensorList(List<String> sensorList) {
		this.sensorList = sensorList;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Rules getoRule() {
		return oRule;
	}

	public void setoRule(Rules oRule) {
		this.oRule = oRule;
	}

	public byte getOccStatus() {
		return occStatus;
	}

	public void setOccStatus(byte occStatus) {
		this.occStatus = occStatus;
		setChanged();
	}

	public Date getLastCommunication() {
		return lastCommunication;
	}

	public void setLastCommunication(Date lastCommunication) {
		this.lastCommunication = lastCommunication;
	}

	/**
	 * @return the stateChangeDate
	 */
	public Date getStateChangeDate() {
		return stateChangeDate;
	}

	/**
	 * @param stateChangeDate the stateChangeDate to set
	 */
	public void setStateChangeDate(Date stateChangeDate) {
		this.stateChangeDate = stateChangeDate;
	}

	/**
	 * @return the zoneFailure
	 */
	public boolean isZoneFailure() {
		return zoneFailure;
	}

	/**
	 * @param zoneFailure the zoneFailure to set
	 */
	public void setZoneFailure(boolean zoneFailure) {
		if(zoneFailure != this.zoneFailure) {
			setStateChangeDate(new Date());
		}
		this.zoneFailure = zoneFailure;
		if(!this.zoneFailure) {
			setChanged();
		}
	}

	/**
	 * @return the avgTemp
	 */
	public int getAvgTemp() {
		return avgTemp;
	}


	/**
	 * @param avgTemp the avgTemp to set
	 */
	public void setAvgTemp(int avgTemp) {
		this.avgTemp = avgTemp;
	}


	/**
	 * @return the minTemp
	 */
	public int getMinTemp() {
		return minTemp;
	}


	/**
	 * @param minTemp the minTemp to set
	 */
	public void setMinTemp(int minTemp) {
		this.minTemp = minTemp;
	}


	/**
	 * @return the maxTemp
	 */
	public int getMaxTemp() {
		return maxTemp;
	}


	/**
	 * @param maxTemp the maxTemp to set
	 */
	public void setMaxTemp(int maxTemp) {
		this.maxTemp = maxTemp;
	}


	/**
	 * @return the avgDimLevel
	 */
	public int getAvgDimLevel() {
		return avgDimLevel;
	}


	/**
	 * @param avgDimLevel the avgDimLevel to set
	 */
	public void setAvgDimLevel(int avgDimLevel) {
		this.avgDimLevel = avgDimLevel;
	}


	/**
	 * @return the avgPower
	 */
	public int getAvgPower() {
		return avgPower;
	}


	/**
	 * @param avgPower the avgPower to set
	 */
	public void setAvgPower(int avgPower) {
		this.avgPower = avgPower;
	}


	/**
	 * @return the override
	 */
	public boolean isOverride() {
		return override;
	}


	/**
	 * @param override the override to set
	 */
	public void setOverride(boolean override) {
		this.override = override;
	}


	public String getFloorName() {
		return floorName;
	}


	public String getBuildingName() {
		return buildingName;
	}


	public void setFloorName(String floorName) {
		this.floorName = floorName;
	}


	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}


	public Integer getPercentOccupancy() {
		return percentOccupancy;
	}


	public void setPercentOccupancy(Integer percentOccupancy) {
		this.percentOccupancy = percentOccupancy;
	}


	public byte getOccOutput() {
		if(zoneFailure || init == 0) {
			return -1;
		}
		return occStatus;
	}


	public void setInit(byte init) {
		this.init = init;
	}


}
