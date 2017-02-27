/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

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
public class FixtureLampCalibration implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "captureat")
    private Date captureAt;
    @XmlElement(name = "fixtureid")
    private Long fixtureId;
    @XmlElement(name = "initial")
    private Boolean initial;
    @XmlElement(name = "fixtureCalibrationMap")
    private Set<FixtureCalibrationMap> fixtureCalibrationMap;
    @XmlElement(name = "warmupTime")
    private Short warmupTime;
    @XmlElement(name = "stabilizationTime")
    private Short stabilizationTime;
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
     * @return the fixtureId
     */
    public Long getFixtureId() {
        return fixtureId;
    }

    /**
     * @param fixtureId
     *            the fixtureId to set
     */
    public void setFixtureId(Long fixtureId) {
        this.fixtureId = fixtureId;
    }

    /**
     * @return the initial
     */
    public Boolean getInitial() {
        return initial;
    }

    /**
     * @param initial
     *            the initial to set
     */
    public void setInitial(Boolean initial) {
        this.initial = initial;
    }

    /**
     * @return the fixtureCalibrationMap
     */
    public Set<FixtureCalibrationMap> getFixtureCalibrationMap() {
        if (fixtureCalibrationMap == null)
                return null;
        return fixtureCalibrationMap;
    }

    /**
     * @param fixtureCalibrationMap
     *            the fixtureCalibrationMap to set
     */
    public void setFixtureCalibrationMap(Set<FixtureCalibrationMap> fixtureCalibrationMap) {
        this.fixtureCalibrationMap = fixtureCalibrationMap;
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

}
