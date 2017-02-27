package com.ems.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FacilityEnergyStats {
	
	
	@XmlElement(name="id")
	private BigInteger floorId;
	@XmlElement(name="intervalStart")
	private Date captureAt;
	public Date getCaptureAt() {
		return captureAt;
	}
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	@XmlElement(name="name")
	private String floorName;
	
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
	private BigDecimal dimLevel;

	public BigInteger getFloorId() {
		return floorId;
	}
	public void setFloorId(BigInteger floorId) {
		this.floorId = floorId;
	}
	public String getFloorName() {
		return floorName;
	}
	public void setFloorName(String floorName) {
		this.floorName = floorName;
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
	public BigDecimal getDimLevel() {
		return dimLevel;
	}
	public void setDimLevel(BigDecimal dimLevel) {
		this.dimLevel = dimLevel;
	}

}
