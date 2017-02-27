package com.ems.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class EnergyConsumption implements Serializable {

    private static final long serialVersionUID = 1029630101450441670L;
    private Long id;
    private Short minTemperature;
    private Short maxTemperature;
    private Double avgTemperature;
    private Short lightOnSeconds;
    private Short lightMinLevel;
    private Short lightMaxLevel;
    private Short lightAvgLevel;
    private Short lightOn;
    private Short lightOff;
    private BigDecimal powerUsed;
    private Short occIn;
    private Short occOut;
    private Short occCount;
    private Short dimPercentage;
    private Short dimOffset;
    private Short brightPercentage;
    private Short brightOffset;
    private Date captureAt;
    private Fixture fixture;
    private String name;

    private Float cost;
    private Float price;
    private BigDecimal basePowerUsed;
    private Float baseCost;
    private BigDecimal savedPowerUsed;
    private Float savedCost;
    private BigDecimal occSaving;
    private BigDecimal tuneupSaving;
    private BigDecimal ambientSaving;
    private BigDecimal manualSaving;
    private Short zeroBucket;
    private Short avgVolts;
    private Short currState;
    private Short powerCalc;
    private Long motionBits;
    private Long energyCum;
    private Integer energyCalib;
    private Short minVolts;
    private Short maxVolts;
    private Integer energyTicks;
    private Short lastVolts;
    private Short savingType;
    private Integer cuStatus;
    private Short lastTemperature;
    private Long sysUptime;

    public EnergyConsumption() {
    }

    public EnergyConsumption(Long id, BigDecimal powerUsed, Date captureAt) {
        this.id = id;
        this.powerUsed = powerUsed;
        this.captureAt = captureAt;
    }

    public EnergyConsumption(Long id, String name, BigDecimal powerUsed) {
        this.id = id;
        this.powerUsed = powerUsed;
        this.name = name;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the minTemperature
     */
    public Short getMinTemperature() {
        return minTemperature;
    }

    /**
     * @param minTemperature
     *            the minTemperature to set
     */
    public void setMinTemperature(Short minTemperature) {
        this.minTemperature = minTemperature;
    }

    /**
     * @return the maxTemperature
     */
    public Short getMaxTemperature() {
        return maxTemperature;
    }

    /**
     * @param maxTemperature
     *            the maxTemperature to set
     */
    public void setMaxTemperature(Short maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    /**
     * @return the avgTemperature
     */
    public Double getAvgTemperature() {
        return avgTemperature;
    }

    /**
     * @param avgTemperature
     *            the avgTemperature to set
     */
    public void setAvgTemperature(Double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }

    /**
     * @return the lightOnSeconds
     */
    public Short getLightOnSeconds() {
        return lightOnSeconds;
    }

    /**
     * @param lightOnSeconds
     *            the lightOnSeconds to set
     */
    public void setLightOnSeconds(Short lightOnSeconds) {
        this.lightOnSeconds = lightOnSeconds;
    }

    /**
     * @return the lightMinLevel
     */
    public Short getLightMinLevel() {
        return lightMinLevel;
    }

    /**
     * @param lightMinLevel
     *            the lightMinLevel to set
     */
    public void setLightMinLevel(Short lightMinLevel) {
        this.lightMinLevel = lightMinLevel;
    }

    /**
     * @return the lightMaxLevel
     */
    public Short getLightMaxLevel() {
        return lightMaxLevel;
    }

    /**
     * @param lightMaxLevel
     *            the lightMaxLevel to set
     */
    public void setLightMaxLevel(Short lightMaxLevel) {
        this.lightMaxLevel = lightMaxLevel;
    }

    /**
     * @return the lightAvgLevel
     */
    public Short getLightAvgLevel() {
        return lightAvgLevel;
    }

    /**
     * @param lightAvgLevel
     *            the lightAvgLevel to set
     */
    public void setLightAvgLevel(Short lightAvgLevel) {
        this.lightAvgLevel = lightAvgLevel;
    }

    /**
     * @return the lightOn
     */
    public Short getLightOn() {
        return lightOn;
    }

    /**
     * @param lightOn
     *            the lightOn to set
     */
    public void setLightOn(Short lightOn) {
        this.lightOn = lightOn;
    }

    /**
     * @return the lightOff
     */
    public Short getLightOff() {
        return lightOff;
    }

    /**
     * @param lightOff
     *            the lightOff to set
     */
    public void setLightOff(Short lightOff) {
        this.lightOff = lightOff;
    }

    /**
     * @return the powerUsed
     */
    public BigDecimal getPowerUsed() {
        return powerUsed;
    }

    /**
     * @param powerUsed
     *            the powerUsed to set
     */
    public void setPowerUsed(BigDecimal powerUsed) {
        this.powerUsed = powerUsed;
    }

    /**
     * @return the occIn
     */
    public Short getOccIn() {
        return occIn;
    }

    /**
     * @param occIn
     *            the occIn to set
     */
    public void setOccIn(Short occIn) {
        this.occIn = occIn;
    }

    /**
     * @return the occOut
     */
    public Short getOccOut() {
        return occOut;
    }

    /**
     * @param occOut
     *            the occOut to set
     */
    public void setOccOut(Short occOut) {
        this.occOut = occOut;
    }

    /**
     * @return the occCount
     */
    public Short getOccCount() {
        return occCount;
    }

    /**
     * @param occCount
     *            the occCount to set
     */
    public void setOccCount(Short occCount) {
        this.occCount = occCount;
    }

    /**
     * @return the dimPercentage
     */
    public Short getDimPercentage() {
        return dimPercentage;
    }

    /**
     * @param dimPercentage
     *            the dimPercentage to set
     */
    public void setDimPercentage(Short dimPercentage) {
        this.dimPercentage = dimPercentage;
    }

    /**
     * @return the dimOffset
     */
    public Short getDimOffset() {
        return dimOffset;
    }

    /**
     * @param dimOffset
     *            the dimOffset to set
     */
    public void setDimOffset(Short dimOffset) {
        this.dimOffset = dimOffset;
    }

    /**
     * @return the brightPercentage
     */
    public Short getBrightPercentage() {
        return brightPercentage;
    }

    /**
     * @param brightPercentage
     *            the brightPercentage to set
     */
    public void setBrightPercentage(Short brightPercentage) {
        this.brightPercentage = brightPercentage;
    }

    /**
     * @return the brightOffset
     */
    public Short getBrightOffset() {
        return brightOffset;
    }

    /**
     * @param brightOffset
     *            the brightOffset to set
     */
    public void setBrightOffset(Short brightOffset) {
        this.brightOffset = brightOffset;
    }

    /**
     * @return the captureAt
     */
    public Date getCaptureAt() {
        return captureAt;
    }

    /**
     * @param captureAt
     *            the captureAt to set
     */
    public void setCaptureAt(Date captureAt) {
        this.captureAt = captureAt;
    }

    /**
     * @return the fixture
     */
    public Fixture getFixture() {
        return fixture;
    }

    /**
     * @param fixture
     *            the fixture to set
     */
    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cost
     */
    public Float getCost() {
        return cost;
    }

    /**
     * @param cost
     *            the cost to set
     */
    public void setCost(Float cost) {
        this.cost = cost;
    }

    /**
     * @return the price
     */
    public Float getPrice() {
        return price;
    }

    /**
     * @param price
     *            the price to set
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    /**
     * @return the basePowerUsed
     */
    public BigDecimal getBasePowerUsed() {
        return basePowerUsed;
    }

    /**
     * @param basePowerUsed
     *            the basePowerUsed to set
     */
    public void setBasePowerUsed(BigDecimal basePowerUsed) {
        this.basePowerUsed = basePowerUsed;
    }

    /**
     * @return the baseCost
     */
    public Float getBaseCost() {
        return baseCost;
    }

    /**
     * @param baseCost
     *            the baseCost to set
     */
    public void setBaseCost(Float baseCost) {
        this.baseCost = baseCost;
    }

    /**
     * @return the savedPowerUsed
     */
    public BigDecimal getSavedPowerUsed() {
        return savedPowerUsed;
    }

    /**
     * @param savedPowerUsed
     *            the savedPowerUsed to set
     */
    public void setSavedPowerUsed(BigDecimal savedPowerUsed) {
        this.savedPowerUsed = savedPowerUsed;
    }

    /**
     * @return the savedCost
     */
    public Float getSavedCost() {
        return savedCost;
    }

    /**
     * @param savedCost
     *            the savedCost to set
     */
    public void setSavedCost(Float savedCost) {
        this.savedCost = savedCost;
    }

    /**
     * @return the occSaving
     */
    public BigDecimal getOccSaving() {
        return occSaving;
    }

    /**
     * @param occSaving
     *            the occSaving to set
     */
    public void setOccSaving(BigDecimal occSaving) {
        this.occSaving = occSaving;
    }

    /**
     * @return the tuneupSaving
     */
    public BigDecimal getTuneupSaving() {
        return tuneupSaving;
    }

    /**
     * @param tuneupSaving
     *            the tuneupSaving to set
     */
    public void setTuneupSaving(BigDecimal tuneupSaving) {
        this.tuneupSaving = tuneupSaving;
    }

    /**
     * @return the ambientSaving
     */
    public BigDecimal getAmbientSaving() {
        return ambientSaving;
    }

    /**
     * @param ambientSaving
     *            the ambientSaving to set
     */
    public void setAmbientSaving(BigDecimal ambientSaving) {
        this.ambientSaving = ambientSaving;
    }

    /**
     * @return the manualSaving
     */
    public BigDecimal getManualSaving() {
        return manualSaving;
    }

    /**
     * @param manualSaving
     *            the manualSaving to set
     */
    public void setManualSaving(BigDecimal manualSaving) {
        this.manualSaving = manualSaving;
    }

    /**
     * @return the zeroBucket
     */
    public Short getZeroBucket() {
        return zeroBucket;
    }

    /**
     * @param zeroBucket
     *            the zeroBucket to set
     */
    public void setZeroBucket(Short zeroBucket) {
        this.zeroBucket = zeroBucket;
    }

    /**
     * @return the avgVolts
     */
    public Short getAvgVolts() {
        return avgVolts;
    }

    /**
     * @param avgVolts
     *            the avgVolts to set
     */
    public void setAvgVolts(Short avgVolts) {
        this.avgVolts = avgVolts;
    }

    /**
     * @return the currState
     */
    public Short getCurrState() {
        return currState;
    }

    /**
     * @param currState
     *            the currState to set
     */
    public void setCurrState(Short currState) {
        this.currState = currState;
    }

    /**
     * @return the powerClac
     */
    public Short getPowerCalc() {
        return powerCalc;
    }

    /**
     * @param powerCalc
     *            the powerCalc to set
     */
    public void setPowerCalc(Short powerCalc) {
        this.powerCalc = powerCalc;
    }

    /**
     * @return the motionBits
     */
    public Long getMotionBits() {
        return motionBits;
    }

    /**
     * @param motionBits
     *            the motionBits to set
     */
    public void setMotionBits(Long motionBits) {
        this.motionBits = motionBits;
    }

    /**
     * @return the energyCum
     */
    public Long getEnergyCum() {
        return energyCum;
    }

    /**
     * @param energyCum
     *            the energyCum to set
     */
    public void setEnergyCum(Long energyCum) {
        this.energyCum = energyCum;
    }

    /**
     * @return the energyCalib
     */
    public Integer getEnergyCalib() {
        return energyCalib;
    }

    /**
     * @param energyCalib
     *            the energyCalib to set
     */
    public void setEnergyCalib(Integer energyCalib) {
        this.energyCalib = energyCalib;
    }

    /**
     * @return the minVolts
     */
    public Short getMinVolts() {
        return minVolts;
    }

    /**
     * @param minVolts
     *            the minVolts to set
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
     * @param maxVolts
     *            the maxVolts to set
     */
    public void setMaxVolts(Short maxVolts) {
        this.maxVolts = maxVolts;
    }

    /**
     * @return the energyTicks
     */
    public Integer getEnergyTicks() {
        return energyTicks;
    }

    /**
     * @param energyTicks
     *            the energyTicks to set
     */
    public void setEnergyTicks(Integer energyTicks) {
        this.energyTicks = energyTicks;
    }
    
    /**
     * @return the lastVolts
     */
    public Short getLastVolts() {
    
      return lastVolts;
    }
    
    /**
     * @param lastVolts the lastVolts to set
     */
    public void setLastVolts(Short lastVolts) {
    
      this.lastVolts = lastVolts;
    }
    
    /**
     * @return the savingType
     */
    public Short getSavingType() {
    
      return savingType;
    }
    
    /**
     * @param savingType the savingType to set
     */
    public void setSavingType(Short savingType) {
    
      this.savingType = savingType;
    }

	/**
     * @return the cuStatus
     */
    public Integer getCuStatus() {
        return cuStatus;
    }

    /**
     * @param cuStatus
     *            the cuStatus to set
     */
    public void setCuStatus(Integer cuStatus) {
        this.cuStatus = cuStatus;
    }
    
    /**
     * @return the lastTemperature
     */
    public Short getLastTemperature() {
        return lastTemperature;
    }

    /**
     * @param lastTemperature
     *            the lastTemperature to set
     */
    public void setLastTemperature(Short lastTemperature) {
        this.lastTemperature = lastTemperature;
    }
    
    /**
     * @return the sysUptime
     */
    public Long getSysUptime() {
        return sysUptime;
    }

    /**
     * @param sysUptime
     *            the sysUptime to set
     */
    public void setSysUptime(Long sysUptime) {
        this.sysUptime = sysUptime;
    }
    
}
