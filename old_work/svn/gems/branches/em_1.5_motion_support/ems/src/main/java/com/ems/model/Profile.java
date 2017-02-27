package com.ems.model;

import java.io.Serializable;

/**
 * 
 * @author Shiv Mohan
 * 
 */
public class Profile implements Serializable, Cloneable {

    private static final long serialVersionUID = 3802906128122902345L;

    private Long id;
    private Long minLevel;
    private Long onLevel;
    private Long motionDetectDuration;
    private Long manualOverrideDuration;
    private Long motionSensitivity;
    private Long rampUpTime;
    private Integer ambientSensitivity;

    public Object clone() {
        return new Profile();
    }

    /**
	 */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return minimum level
     */
    public Long getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Long minLevel) {
        this.minLevel = minLevel;
    }

    /**
     * @return on level
     */
    public Long getOnLevel() {
        return onLevel;
    }

    public void setOnLevel(Long onLevel) {
        this.onLevel = onLevel;
    }

    /**
     * @return motion detection duration
     */
    public Long getMotionDetectDuration() {
        return motionDetectDuration;
    }

    public void setMotionDetectDuration(Long motionDetectDuration) {
        this.motionDetectDuration = motionDetectDuration;
    }

    /**
     * @return manual override duration
     */
    public Long getManualOverrideDuration() {
        return manualOverrideDuration;
    }

    public void setManualOverrideDuration(Long manualOverrideDuration) {
        this.manualOverrideDuration = manualOverrideDuration;
    }

    /**
     * @return motion sensitivity
     */
    public Long getMotionSensitivity() {
        return motionSensitivity;
    }

    public void setMotionSensitivity(Long motionSensitivity) {
        this.motionSensitivity = motionSensitivity;
    }

    /**
     * @return ramp up time
     */
    public Long getRampUpTime() {
        return rampUpTime;
    }

    public void setRampUpTime(Long rampUpTime) {
        this.rampUpTime = rampUpTime;
    }

    /**
     * @return ambient sensitivity
     */
    public Integer getAmbientSensitivity() {
        return ambientSensitivity;
    }

    public void setAmbientSensitivity(Integer ambientSensitivity) {
        this.ambientSensitivity = ambientSensitivity;
    }

    public Profile copy() {
        Profile p = new Profile();
        p.setManualOverrideDuration(this.getManualOverrideDuration());
        p.setMinLevel(this.getMinLevel());
        p.setMotionDetectDuration(this.getMotionDetectDuration());
        p.setOnLevel(this.getOnLevel());
        p.setMotionSensitivity(this.getMotionSensitivity());
        p.setRampUpTime(this.getRampUpTime());
        p.setAmbientSensitivity(this.getAmbientSensitivity());
        return p;
    }

    /**
     * Copies target profile values to source.
     */
    public void copyFrom(Profile target) {
        this.setManualOverrideDuration(target.getManualOverrideDuration());
        this.setMinLevel(target.getMinLevel());
        this.setMotionDetectDuration(target.getMotionDetectDuration());
        this.setOnLevel(target.getOnLevel());
        this.setMotionSensitivity(target.getMotionSensitivity());
        this.setRampUpTime(target.getRampUpTime());
        this.setAmbientSensitivity(target.getAmbientSensitivity());
    }
}
