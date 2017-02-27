/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yogesh
 * 
 */
public class MotionBitRecord implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Long Id;
    private Long fixtureId;
    private Date captureAt;
    private String motionBits;
    private Integer motionBitsLevel;
	private Integer motionBitsFrequency ;

    /**
     * @return the id
     */
    public Long getId() {
        return Id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        Id = id;
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
     * @return the motionBits
     */
    public String getMotionBits() {
        return motionBits;
    }

    /**
     * @param motionBits the motionBits to set
     */
    public void setMotionBits(String motionBits) {
        this.motionBits = motionBits;
    }

    /**
     * @return the motionBitsLevel
     */
    public Integer getMotionBitsLevel() {
        return motionBitsLevel;
    }

    /**
     * @param motionBitsLevel
     *            the motionBitsLevel to set
     */
    public void setMotionBitsLevel(Integer motionBitsLevel) {
        this.motionBitsLevel = motionBitsLevel;
    }

    public Integer getMotionBitsFrequency() {
		return motionBitsFrequency;
	}

	public void setMotionBitsFrequency(Integer motionBitsFrequency) {
		this.motionBitsFrequency = motionBitsFrequency;
	}
}
