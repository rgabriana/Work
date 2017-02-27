/**
 * 
 */
package com.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DRRecord {
	
	@XmlElement(name="fixtureId")
	private int fixtureId;
	@XmlElement(name="dimmerControl")
	private int dimmerControl;
	@XmlElement(name="currentState")
	private String currentState;
	@XmlElement(name="drReactivity")
	private int drReactivity;
	@XmlElement(name="minLevel")
	private int minLevel;
	@XmlElement(name="maxLevel")
	private int maxLevel;
	@XmlElement(name="avgPowerUsed")
	private Double avgPowerUsed;
	@XmlElement(name="powerUsed")
	private Double powerUsed;

	public DRRecord() {
	}

	public DRRecord(int fixtureId, int dimmerControl, String currentState, int drReactivity,
			int minLevel, int maxLevel, Double avgPowerUsed, Double powerUsed) {
		this.fixtureId = fixtureId;
		this.dimmerControl = dimmerControl;
		this.currentState = currentState;
		this.drReactivity = drReactivity;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.avgPowerUsed = avgPowerUsed;
		this.powerUsed = powerUsed;
	}

	public int getFixtureId() {
		return fixtureId;
	}

	public void setFixtureId(int fixtureId) {
		this.fixtureId = fixtureId;
	}

	public int getDimmerControl() {
		return dimmerControl;
	}

	public void setDimmerControl(int dimmerControl) {
		this.dimmerControl = dimmerControl;
	}

	public String isCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public int getDrReactivity() {
		return drReactivity;
	}

	public void setDrReactivity(int drReactivity) {
		this.drReactivity = drReactivity;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public Double getAvgPowerUsed() {
		return avgPowerUsed;
	}

	public void setAvgPowerUsed(Double avgPowerUsed) {
		this.avgPowerUsed = avgPowerUsed;
	}

	public Double getPowerUsed() {
		return powerUsed;
	}

	public void setPowerUsed(Double powerUsed) {
		this.powerUsed = powerUsed;
	}

	
}
