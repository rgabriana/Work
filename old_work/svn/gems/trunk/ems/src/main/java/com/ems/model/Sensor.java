package com.ems.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shrihari
 * Created to return the Sensor details as a part of SensorEnergyStats in lighting API
 *
 */
@XmlRootElement(name="Sensor")
@XmlAccessorType(XmlAccessType.NONE)
public class Sensor {
	
	@XmlElement(name="occupancyState")
	private Short occupancyState;
	
	public BigInteger getFixtureId() {
		return fixtureId;
	}
	public void setFixtureId(BigInteger fixtureId) {
		this.fixtureId = fixtureId;
	}
	public String getFixtureName() {
		return fixtureName;
	}
	public void setFixtureName(String fixtureName) {
		this.fixtureName = fixtureName;
	}
	public Double getBaseEnergy() {
		return baseEnergy;
	}
	public void setBaseEnergy(Double baseEnergy) {
		this.baseEnergy = baseEnergy;
	}
	public BigDecimal getEnergy() {
		return energy;
	}
	public void setEnergy(BigDecimal energy) {
		this.energy = energy;
	}
	public BigDecimal getSavedEnergy() {
		return savedEnergy;
	}
	public void setSavedEnergy(BigDecimal savedEnergy) {
		this.savedEnergy = savedEnergy;
	}
	public BigDecimal getOccSavings() {
		return occSavings;
	}
	public void setOccSavings(BigDecimal occSavings) {
		this.occSavings = occSavings;
	}
	public BigDecimal getAmbientSavings() {
		return ambientSavings;
	}
	public void setAmbientSavings(BigDecimal ambientSavings) {
		this.ambientSavings = ambientSavings;
	}
	public BigDecimal getTuneupSavings() {
		return tuneupSavings;
	}
	public void setTuneupSavings(BigDecimal tuneupSavings) {
		this.tuneupSavings = tuneupSavings;
	}
	public BigDecimal getManualSavings() {
		return manualSavings;
	}
	public void setManualSavings(BigDecimal manualSavings) {
		this.manualSavings = manualSavings;
	}
	public BigInteger getOccCount() {
		return occCount;
	}
	public void setOccCount(BigInteger occCount) {
		this.occCount = occCount;
	}
	public Float getDimLevel() {
		return dimLevel;
	}
	public void setDimLevel(Float dimLevel) {
		this.dimLevel = dimLevel;
	}
	@XmlElement(name="id")
	private BigInteger fixtureId;
	@XmlElement(name="name")
	private String fixtureName;
	
	@XmlElement(name="baseEnergy")
	private Double baseEnergy;
	@XmlElement(name="energy")
	private BigDecimal energy;
	@XmlElement(name="savedEnergy")
	private BigDecimal savedEnergy;
	@XmlElement(name="occSavings")
	private BigDecimal occSavings;
	@XmlElement(name="ambientSavings")
	private BigDecimal ambientSavings;
	@XmlElement(name="tuneupSavings")
	private BigDecimal tuneupSavings;
	@XmlElement(name="manualSavings")
	private BigDecimal manualSavings;
	@XmlElement(name="occupancy")
	private BigInteger occCount;
	@XmlElement(name="dimLevel")
	private Float dimLevel;
	@XmlElement(name="ambientLight")
	private Integer ambientLight;
	/**
	 * @return the occupancyState
	 */
	public Short getOccupancyState() {
		return occupancyState;
	}
	/**
	 * @param occupancyState the occupancyState to set
	 */
	public void setOccupancyState(Short occupancyState) {
		this.occupancyState = occupancyState;
	}
	public Integer getAmbientLight() {
		return ambientLight;
	}
	public void setAmbientLight(Integer ambientLight) {
		this.ambientLight = ambientLight;
	}

}
