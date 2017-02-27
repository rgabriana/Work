package com.ems.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class EnergyConsumptionDaily implements Serializable {

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

    private BigDecimal peakLoad;
    private BigDecimal minLoad;

    public EnergyConsumptionDaily() {
    }

    public EnergyConsumptionDaily(Long id, BigDecimal powerUsed, Date captureAt) {
        this.id = id;
        this.powerUsed = powerUsed;
        this.captureAt = captureAt;
    }

    public EnergyConsumptionDaily(Long id, String name, BigDecimal powerUsed) {
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
     * @return the peakLoad
     */
    public BigDecimal getPeakLoad() {
        return peakLoad;
    }

    /**
     * @param peakLoad
     *            the peakLoad to set
     */
    public void setPeakLoad(BigDecimal peakLoad) {
        this.peakLoad = peakLoad;
    }

    /**
     * @return the minLoad
     */
    public BigDecimal getMinLoad() {
        return minLoad;
    }

    /**
     * @param minLoad
     *            the minLoad to set
     */
    public void setMinLoad(BigDecimal minLoad) {
        this.minLoad = minLoad;
    }

}
