package com.ems.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="plugloadEnergyConsumptionHourly")
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadEnergyConsumptionHourly implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;   
	
	@XmlElement(name = "avgTemperature")
  private Float avgTemperature; 
	@XmlElement(name = "minTemperature")
  private Float minTemperature;
	@XmlElement(name = "maxTemperature")
  private Float maxTemperature;
	@XmlElement(name = "lastTemperature")
  private Float lastTemperature;
  
	@XmlElement(name = "minVolts")
  private Short minVolts;
	@XmlElement(name = "maxVolts")
  private Short maxVolts;
	@XmlElement(name = "avgVolts")
  private Float avgVolts;
	@XmlElement(name = "lastVolts")
  private Short lastVolts;
  
	@XmlElement(name = "managedOnSecs")
  private Short managedOnSecs;
	@XmlElement(name = "managedOnToOffSec")
  private Short managedOnToOffSec;
	@XmlElement(name = "managedOffToOnSec")
  private Short managedOffToOnSec;
  
	@XmlElement(name = "lastMotionSecsAgo")
  private Integer lastMotionSecsAgo;
	@XmlElement(name = "currentState")
  private Byte currentState;
	@XmlElement(name = "currentBehavior")
  private Byte currentBehavior;
	@XmlElement(name = "noOfLoadChanges")
  private Short noOfLoadChanges;
	@XmlElement(name = "noOfPeersHeardFrom")
  private Short noOfPeersHeardFrom;	
         
	@XmlElement(name = "captureAt")
  private Date captureAt;
  private Plugload plugload;
  
  @XmlElement(name = "cost")
  private Float cost;
  @XmlElement(name = "price")
  private Float price;
  @XmlElement(name = "baseCost")
  private Float baseCost;
  @XmlElement(name = "savedCost")
  private Float savedCost;
  
  @XmlElement(name = "baseEnergy")
  private BigDecimal baseEnergy;
  @XmlElement(name = "energy")
  private BigDecimal energy;
  @XmlElement(name = "savedEnergy")
  private BigDecimal savedEnergy;
  @XmlElement(name = "savingType")
  private Byte savingType;
  @XmlElement(name = "occSaving")
  private BigDecimal occSaving;
  @XmlElement(name = "tuneupSaving")
  private BigDecimal tuneupSaving;
  @XmlElement(name = "manualSaving")
  private BigDecimal manualSaving;
  
  @XmlElement(name = "managedEnergyCum")
  private Long managedEnergyCum;
  
  @XmlElement(name = "baseUnmanagedEnergy")
  private BigDecimal baseUnmanagedEnergy;
  @XmlElement(name = "unmanagedEnergy")
  private BigDecimal unmanagedEnergy;
  @XmlElement(name = "savedUnmanagedEnergy")
  private BigDecimal savedUnmanagedEnergy;
  @XmlElement(name = "unmanagedEnergyCum")
  private Long unmanagedEnergyCum;
  
  @XmlElement(name = "managedCurrent")
  private BigDecimal managedCurrent;
  @XmlElement(name = "managedLastLoad")
  private BigDecimal managedLastLoad;
  @XmlElement(name = "managedPowerFactor")
  private BigDecimal managedPowerFactor;
  
  @XmlElement(name = "unmanagedCurrent")
  private BigDecimal unmanagedCurrent;
  @XmlElement(name = "unmanagedLastLoad")
  private BigDecimal unmanagedLastLoad;
  @XmlElement(name = "unmanagedPowerFactor")
  private BigDecimal unmanagedPowerFactor;
     
  @XmlElement(name = "zeroBucket")
  private Short zeroBucket;  
  @XmlElement(name = "cuCmdStatus")
  private Integer cuCmdStatus;
  @XmlElement(name = "noOfCuResets")
  private Integer noOfCuResets;
  @XmlElement(name = "cuStatus")
  private Integer cuStatus;
  @XmlElement(name = "sysUptime")
  private Long sysUptime;  
  @XmlElement(name = "currentApp")
  private byte currentApp;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getCaptureAt() {
		return captureAt;
	}
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	public Plugload getPlugload() {
		return plugload;
	}
	public void setPlugload(Plugload plugload) {
		this.plugload = plugload;
	}
	
	public Float getCost() {
		return cost;
	}
	public void setCost(Float cost) {
		this.cost = cost;
	}
	public Float getPrice() {
		return price;
	}
	public void setPrice(Float price) {
		this.price = price;
	}
	public Float getBaseCost() {
		return baseCost;
	}
	public void setBaseCost(Float baseCost) {
		this.baseCost = baseCost;
	}
	public Float getSavedCost() {
		return savedCost;
	}
	public void setSavedCost(Float savedCost) {
		this.savedCost = savedCost;
	}
	
	public Float getAvgVolts() {
		return avgVolts;
	}
	public void setAvgVolts(Float avgVolts) {
		this.avgVolts = avgVolts;
	}
		
	public BigDecimal getOccSaving() {
		return occSaving;
	}
	public void setOccSaving(BigDecimal occSaving) {
		this.occSaving = occSaving;
	}
	public BigDecimal getTuneupSaving() {
		return tuneupSaving;
	}
	public void setTuneupSaving(BigDecimal tuneupSaving) {
		this.tuneupSaving = tuneupSaving;
	}
	public BigDecimal getManualSaving() {
		return manualSaving;
	}
	public void setManualSaving(BigDecimal manualSaving) {
		this.manualSaving = manualSaving;
	}
	public Short getZeroBucket() {
		return zeroBucket;
	}
	public void setZeroBucket(Short zeroBucket) {
		this.zeroBucket = zeroBucket;
	}
	
	
	public Short getLastVolts() {
		return lastVolts;
	}
	public void setLastVolts(Short lastVolts) {
		this.lastVolts = lastVolts;
	}
	
	public Integer getCuStatus() {
		return cuStatus;
	}
	public void setCuStatus(Integer cuStatus) {
		this.cuStatus = cuStatus;
	}
	
	public Long getSysUptime() {
		return sysUptime;
	}
	public void setSysUptime(Long sysUptime) {
		this.sysUptime = sysUptime;
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
	 * @return the lastTemperature
	 */
	public Float getLastTemperature() {
		return lastTemperature;
	}
	/**
	 * @param lastTemperature the lastTemperature to set
	 */
	public void setLastTemperature(Float lastTemperature) {
		this.lastTemperature = lastTemperature;
	}
	/**
	 * @return the minVolts
	 */
	public Short getMinVolts() {
		return minVolts;
	}
	/**
	 * @param minVolts the minVolts to set
	 */
	public void setMinVolts(Short minVolts) {
		this.minVolts = minVolts;
	}
	/**
	 * @return the maxVolts
	 */
	public Short getMaxVolts() {
		return maxVolts;
	}
	/**
	 * @param maxVolts the maxVolts to set
	 */
	public void setMaxVolts(Short maxVolts) {
		this.maxVolts = maxVolts;
	}
	/**
	 * @return the managedOnSecs
	 */
	public Short getManagedOnSecs() {
		return managedOnSecs;
	}
	/**
	 * @param managedOnSecs the managedOnSecs to set
	 */
	public void setManagedOnSecs(Short managedOnSecs) {
		this.managedOnSecs = managedOnSecs;
	}
	/**
	 * @return the managedOnToOffSec
	 */
	public Short getManagedOnToOffSec() {
		return managedOnToOffSec;
	}
	/**
	 * @param managedOnToOffSec the managedOnToOffSec to set
	 */
	public void setManagedOnToOffSec(Short managedOnToOffSec) {
		this.managedOnToOffSec = managedOnToOffSec;
	}
	/**
	 * @return the managedOffToOnSec
	 */
	public Short getManagedOffToOnSec() {
		return managedOffToOnSec;
	}
	/**
	 * @param managedOffToOnSec the managedOffToOnSec to set
	 */
	public void setManagedOffToOnSec(Short managedOffToOnSec) {
		this.managedOffToOnSec = managedOffToOnSec;
	}
	/**
	 * @return the lastMotionSecsAgo
	 */
	public Integer getLastMotionSecsAgo() {
		return lastMotionSecsAgo;
	}
	/**
	 * @param lastMotionSecsAgo the lastMotionSecsAgo to set
	 */
	public void setLastMotionSecsAgo(Integer lastMotionSecsAgo) {
		this.lastMotionSecsAgo = lastMotionSecsAgo;
	}
	/**
	 * @return the savingType
	 */
	public Byte getSavingType() {
		return savingType;
	}
	/**
	 * @param savingType the savingType to set
	 */
	public void setSavingType(Byte savingType) {
		this.savingType = savingType;
	}
	/**
	 * @return the currentState
	 */
	public Byte getCurrentState() {
		return currentState;
	}
	/**
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(Byte currentState) {
		this.currentState = currentState;
	}
	/**
	 * @return the currentBehavior
	 */
	public Byte getCurrentBehavior() {
		return currentBehavior;
	}
	/**
	 * @param currentBehavior the currentBehavior to set
	 */
	public void setCurrentBehavior(Byte currentBehavior) {
		this.currentBehavior = currentBehavior;
	}
	/**
	 * @return the noOfLoadChanges
	 */
	public Short getNoOfLoadChanges() {
		return noOfLoadChanges;
	}
	/**
	 * @param noOfLoadChanges the noOfLoadChanges to set
	 */
	public void setNoOfLoadChanges(Short noOfLoadChanges) {
		this.noOfLoadChanges = noOfLoadChanges;
	}
	/**
	 * @return the noOfPeersHeardFrom
	 */
	public Short getNoOfPeersHeardFrom() {
		return noOfPeersHeardFrom;
	}
	/**
	 * @param noOfPeersHeardFrom the noOfPeersHeardFrom to set
	 */
	public void setNoOfPeersHeardFrom(Short noOfPeersHeardFrom) {
		this.noOfPeersHeardFrom = noOfPeersHeardFrom;
	}
	
	/**
	 * @return the managedEnergyCum
	 */
	public Long getManagedEnergyCum() {
		return managedEnergyCum;
	}
	/**
	 * @param managedEnergyCum the managedEnergyCum to set
	 */
	public void setManagedEnergyCum(Long managedEnergyCum) {
		this.managedEnergyCum = managedEnergyCum;
	}
	
	/**
	 * @return the unmanagedEnergyCum
	 */
	public Long getUnmanagedEnergyCum() {
		return unmanagedEnergyCum;
	}
	/**
	 * @param unmanagedEnergyCum the unmanagedEnergyCum to set
	 */
	public void setUnmanagedEnergyCum(Long unmanagedEnergyCum) {
		this.unmanagedEnergyCum = unmanagedEnergyCum;
	}
	/**
	 * @return the managedCurrent
	 */
	public BigDecimal getManagedCurrent() {
		return managedCurrent;
	}
	/**
	 * @param managedCurrent the managedCurrent to set
	 */
	public void setManagedCurrent(BigDecimal managedCurrent) {
		this.managedCurrent = managedCurrent;
	}
	/**
	 * @return the managedPowerFactor
	 */
	public BigDecimal getManagedPowerFactor() {
		return managedPowerFactor;
	}
	/**
	 * @param managedPowerFactor the managedPowerFactor to set
	 */
	public void setManagedPowerFactor(BigDecimal managedPowerFactor) {
		this.managedPowerFactor = managedPowerFactor;
	}
	/**
	 * @return the unmanagedCurrent
	 */
	public BigDecimal getUnmanagedCurrent() {
		return unmanagedCurrent;
	}
	/**
	 * @param unmanagedCurrent the unmanagedCurrent to set
	 */
	public void setUnmanagedCurrent(BigDecimal unmanagedCurrent) {
		this.unmanagedCurrent = unmanagedCurrent;
	}
	/**
	 * @return the unmanagedOowerFactor
	 */
	public BigDecimal getUnmanagedPowerFactor() {
		return unmanagedPowerFactor;
	}
	/**
	 * @param unmanagedPowerFactor the unmanagedPowerFactor to set
	 */
	public void setUnmanagedPowerFactor(BigDecimal unmanagedPowerFactor) {
		this.unmanagedPowerFactor = unmanagedPowerFactor;
	}
	/**
	 * @return the cuCmdStatus
	 */
	public Integer getCuCmdStatus() {
		return cuCmdStatus;
	}
	/**
	 * @param cuCmdStatus the cuCmdStatus to set
	 */
	public void setCuCmdStatus(Integer cuCmdStatus) {
		this.cuCmdStatus = cuCmdStatus;
	}
	/**
	 * @return the noOfCuResets
	 */
	public Integer getNoOfCuResets() {
		return noOfCuResets;
	}
	/**
	 * @param noOfCuResets the noOfCuResets to set
	 */
	public void setNoOfCuResets(Integer noOfCuResets) {
		this.noOfCuResets = noOfCuResets;
	}
	/**
	 * @return the baseEnergy
	 */
	public BigDecimal getBaseEnergy() {
		return baseEnergy;
	}
	/**
	 * @param baseEnergy the baseEnergy to set
	 */
	public void setBaseEnergy(BigDecimal baseEnergy) {
		this.baseEnergy = baseEnergy;
	}
	/**
	 * @return the energy
	 */
	public BigDecimal getEnergy() {
		return energy;
	}
	/**
	 * @param energy the energy to set
	 */
	public void setEnergy(BigDecimal energy) {
		this.energy = energy;
	}
	/**
	 * @return the savedEnergy
	 */
	public BigDecimal getSavedEnergy() {
		return savedEnergy;
	}
	/**
	 * @param savedEnergy the savedEnergy to set
	 */
	public void setSavedEnergy(BigDecimal savedEnergy) {
		this.savedEnergy = savedEnergy;
	}
	/**
	 * @return the baseUnmanagedEnergy
	 */
	public BigDecimal getBaseUnmanagedEnergy() {
		return baseUnmanagedEnergy;
	}
	/**
	 * @param baseUnmanagedEnergy the baseUnmanagedEnergy to set
	 */
	public void setBaseUnmanagedEnergy(BigDecimal baseUnmanagedEnergy) {
		this.baseUnmanagedEnergy = baseUnmanagedEnergy;
	}
	/**
	 * @return the unmanagedEnergy
	 */
	public BigDecimal getUnmanagedEnergy() {
		return unmanagedEnergy;
	}
	/**
	 * @param unmanagedEnergy the unmanagedEnergy to set
	 */
	public void setUnmanagedEnergy(BigDecimal unmanagedEnergy) {
		this.unmanagedEnergy = unmanagedEnergy;
	}
	/**
	 * @return the savedUnmanagedEnergy
	 */
	public BigDecimal getSavedUnmanagedEnergy() {
		return savedUnmanagedEnergy;
	}
	/**
	 * @param savedUnmanagedEnergy the savedUnmanagedEnergy to set
	 */
	public void setSavedUnmanagedEnergy(BigDecimal savedUnmanagedEnergy) {
		this.savedUnmanagedEnergy = savedUnmanagedEnergy;
	}
	/**
	 * @return the managedLastLoad
	 */
	public BigDecimal getManagedLastLoad() {
		return managedLastLoad;
	}
	/**
	 * @param managedLastLoad the managedLastLoad to set
	 */
	public void setManagedLastLoad(BigDecimal managedLastLoad) {
		this.managedLastLoad = managedLastLoad;
	}
	/**
	 * @return the unmanagedLastLoad
	 */
	public BigDecimal getUnmanagedLastLoad() {
		return unmanagedLastLoad;
	}
	/**
	 * @param unmanagedLastLoad the unmanagedLastLoad to set
	 */
	public void setUnmanagedLastLoad(BigDecimal unmanagedLastLoad) {
		this.unmanagedLastLoad = unmanagedLastLoad;
	}
	/**
	 * @return the currentApp
	 */
	public byte getCurrentApp() {
		return currentApp;
	}
	/**
	 * @param currentApp the currentApp to set
	 */
	public void setCurrentApp(byte currentApp) {
		this.currentApp = currentApp;
	} 
    
}
