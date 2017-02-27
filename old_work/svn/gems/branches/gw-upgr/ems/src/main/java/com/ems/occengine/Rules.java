/**
 * 
 */
package com.ems.occengine;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Rules implements Serializable {
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "zoneHbTimeInterval")
	private Integer zoneHbTimeInterval = 300;
	
	@XmlElement(name = "sensorHbfailureAlarmCount")
	private Integer sensorHbfailureAlarmCount = 2;
	
	@XmlElement(name = "sensorHbfailureAlarmOutofCount")
	private Integer sensorHbfailureAlarmOutofCount = 2;
	
	@XmlElement(name = "percentSensorHbfailureZoneAlarm")
	private Integer percentSensorHbfailureZoneAlarm = 50;
	
	@XmlElement(name = "sensorOccToUnoccChangeTime")
	private Integer sensorOccToUnoccChangeTime = 90;
	
	@XmlElement(name = "sensorOffTriggerDelay")
	private Integer sensorOffTriggerDelay = 300;
	
	@XmlElement(name = "lingerToAutoPercent")
	private Integer lingerToAutoPercent = 90;
	
	@XmlElement(name = "lingerToOffPercent")
	private Integer lingerToOffPercent = 90;	
	
	@XmlElement(name = "sensorOnTriggerDelay")
	private Integer sensorOnTriggerDelay = 1;

	/**
	 * @return the zoneHbTimeInterval
	 */
	public Integer getZoneHbTimeInterval() {
		return zoneHbTimeInterval;
	}

	/**
	 * @param zoneHbTimeInterval the zoneHbTimeInterval to set
	 */
	public void setZoneHbTimeInterval(Integer zoneHbTimeInterval) {
		this.zoneHbTimeInterval = zoneHbTimeInterval;
	}

	/**
	 * @return the sensorHbfailureAlarmCount
	 */
	public Integer getSensorHbfailureAlarmCount() {
		return sensorHbfailureAlarmCount;
	}

	/**
	 * @param sensorHbfailureAlarmCount the sensorHbfailureAlarmCount to set
	 */
	public void setSensorHbfailureAlarmCount(Integer sensorHbfailureAlarmCount) {
		this.sensorHbfailureAlarmCount = sensorHbfailureAlarmCount;
	}

	/**
	 * @return the sensorHbfailureAlarmOutofCount
	 */
	public Integer getSensorHbfailureAlarmOutofCount() {
		return sensorHbfailureAlarmOutofCount;
	}

	/**
	 * @param sensorHbfailureAlarmOutofCount the sensorHbfailureAlarmOutofCount to set
	 */
	public void setSensorHbfailureAlarmOutofCount(
			Integer sensorHbfailureAlarmOutofCount) {
		this.sensorHbfailureAlarmOutofCount = sensorHbfailureAlarmOutofCount;
	}

	/**
	 * @return the percentSensorHbfailureZoneAlarm
	 */
	public Integer getPercentSensorHbfailureZoneAlarm() {
		return percentSensorHbfailureZoneAlarm;
	}

	/**
	 * @param percentSensorHbfailureZoneAlarm the percentSensorHbfailureZoneAlarm to set
	 */
	public void setPercentSensorHbfailureZoneAlarm(
			Integer percentSensorHbfailureZoneAlarm) {
		this.percentSensorHbfailureZoneAlarm = percentSensorHbfailureZoneAlarm;
	}

	/**
	 * @return the sensorOccToUnoccChangeTime
	 */
	public Integer getSensorOccToUnoccChangeTime() {
		return sensorOccToUnoccChangeTime;
	}

	/**
	 * @param sensorOccToUnoccChangeTime the sensorOccToUnoccChangeTime to set
	 */
	public void setSensorOccToUnoccChangeTime(Integer sensorOccToUnoccChangeTime) {
		this.sensorOccToUnoccChangeTime = sensorOccToUnoccChangeTime;
	}

	/**
	 * @return the sensorOffTriggerDelay
	 */
	public Integer getSensorOffTriggerDelay() {
		return sensorOffTriggerDelay;
	}

	/**
	 * @param sensorOffTriggerDelay the sensorOffTriggerDelay to set
	 */
	public void setSensorOffTriggerDelay(Integer sensorOffTriggerDelay) {
		this.sensorOffTriggerDelay = sensorOffTriggerDelay;
	}

	/**
	 * @return the lingerToAutoPercent
	 */
	public Integer getLingerToAutoPercent() {
		return lingerToAutoPercent;
	}

	/**
	 * @param lingerToAutoPercent the lingerToAutoPercent to set
	 */
	public void setLingerToAutoPercent(Integer lingerToAutoPercent) {
		this.lingerToAutoPercent = lingerToAutoPercent;
	}

	/**
	 * @return the lingerToOffPercent
	 */
	public Integer getLingerToOffPercent() {
		return lingerToOffPercent;
	}

	/**
	 * @param lingerToOffPercent the lingerToOffPercent to set
	 */
	public void setLingerToOffPercent(Integer lingerToOffPercent) {
		this.lingerToOffPercent = lingerToOffPercent;
	}

	/**
	 * @return the sensorOnTriggerDelay
	 */
	public Integer getSensorOnTriggerDelay() {
		return sensorOnTriggerDelay;
	}

	/**
	 * @param sensorOnTriggerDelay the sensorOnTriggerDelay to set
	 */
	public void setSensorOnTriggerDelay(Integer sensorOnTriggerDelay) {
		this.sensorOnTriggerDelay = sensorOnTriggerDelay;
	}
	
	
}
