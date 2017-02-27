/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

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
public class LampCalibrationConfiguration implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "enabled")
    private Boolean enabled;
    @XmlElement(name = "facilityType")
    private Short facilityType;
    private Short frequency;
    @XmlElement(name = "scheduledTime")
    private Integer scheduledTime;
    @XmlElement(name = "mode")
    private Short mode;
    @XmlElement(name = "warmupTime")
    private Short warmupTime;
    @XmlElement(name = "stabilizationTime")
    private Short stabilizationTime;
    @XmlElement(name = "excludedFixtures")
    private String excludedFixtures;
    @XmlElement(name = "potentialDegradeThreshold")
    private Short potentialDegradeThreshold;
    @XmlElement(name = "degradeThreshold")
    private Short degradeThreshold;
    
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
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }
    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    /**
     * @return the facilityType
     */
    public Short getFacilityType() {
        return facilityType;
    }
    /**
     * @param facilityType the facilityType to set
     */
    public void setFacilityType(Short facilityType) {
        this.facilityType = facilityType;
    }
    /**
     * @return the frequency
     */
    public Short getFrequency() {
        return frequency;
    }
    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(Short frequency) {
        this.frequency = frequency;
    }
    /**
     * @return the scheduledTime
     */
    public Integer getScheduledTime() {
        return scheduledTime;
    }
    /**
     * @param scheduledTime the scheduledTime to set
     */
    public void setScheduledTime(Integer scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    /**
     * @return the mode
     */
    public Short getMode() {
        return mode;
    }
    /**
     * @param mode the mode to set
     */
    public void setMode(Short mode) {
        this.mode = mode;
    }
    /**
     * @return the warmupTime
     */
    public Short getWarmupTime() {
        return warmupTime;
    }
    /**
     * @param warmupTime the warmupTime to set
     */
    public void setWarmupTime(Short warmupTime) {
        this.warmupTime = warmupTime;
    }
    /**
     * @return the stabilizationTime
     */
    public Short getStabilizationTime() {
        return stabilizationTime;
    }
    /**
     * @param stabilizationTime the stabilizationTime to set
     */
    public void setStabilizationTime(Short stabilizationTime) {
        this.stabilizationTime = stabilizationTime;
    }
    /**
     * @return the excludedFixtures
     */
    public String getExcludedFixtures() {
        return excludedFixtures;
    }
    /**
     * @param excludedFixtures the excludedFixtures to set
     */
    public void setExcludedFixtures(String excludedFixtures) {
        this.excludedFixtures = excludedFixtures;
    }
    /**
     * @return the potentialDegradeThreshold
     */
    public Short getPotentialDegradeThreshold() {
        return potentialDegradeThreshold;
    }
    /**
     * @param potentialDegradeThreshold the potentialDegradeThreshold to set
     */
    public void setPotentialDegradeThreshold(Short potentialDegradeThreshold) {
        this.potentialDegradeThreshold = potentialDegradeThreshold;
    }
    /**
     * @return the degradeThreshold
     */
    public Short getDegradeThreshold() {
        return degradeThreshold;
    }
    /**
     * @param degradeThreshold the degradeThreshold to set
     */
    public void setDegradeThreshold(Short degradeThreshold) {
        this.degradeThreshold = degradeThreshold;
    }

}
