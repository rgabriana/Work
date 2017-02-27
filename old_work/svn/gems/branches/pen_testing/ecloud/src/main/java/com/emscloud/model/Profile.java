package com.emscloud.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Shiv Mohan
 * 
 */
@Entity
@Table(name = "profile", schema = "public")
@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.NONE)
public class Profile implements Serializable, Cloneable {

    private static final long serialVersionUID = 3802906128122902345L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "minLevel")
    private Long minLevel;
    @XmlElement(name = "onLevel")
    private Long onLevel;
    @XmlElement(name = "motionDetectDuration")
    private Long motionDetectDuration;
    @XmlElement(name = "manualOverrideDuration")
    private Long manualOverrideDuration;
    @XmlElement(name = "motionSensitivity")
    private Long motionSensitivity;
    @XmlElement(name = "rampUpTime")
    private Long rampUpTime;
    @XmlElement(name = "ambientSensitivity")
    private Integer ambientSensitivity;

    public Profile()
    {
    }
    public Object clone() {
        return new Profile();
    }

    /**
	 */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_seq")
    @SequenceGenerator(name="profile_seq", sequenceName="profile_seq",allocationSize=1, initialValue=1)
	@Column(name = "id",unique = true, nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return minimum level
     */
    @Column(name = "min_level")
    public Long getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Long minLevel) {
        this.minLevel = minLevel;
    }

    /**
     * @return on level
     */
    @Column(name = "on_level")
    public Long getOnLevel() {
        return onLevel;
    }

    public void setOnLevel(Long onLevel) {
        this.onLevel = onLevel;
    }

    /**
     * @return motion detection duration
     */
    @Column(name = "motion_detect_duration")
    public Long getMotionDetectDuration() {
        return motionDetectDuration;
    }

    public void setMotionDetectDuration(Long motionDetectDuration) {
        this.motionDetectDuration = motionDetectDuration;
    }

    /**
     * @return manual override duration
     */
    @Column(name = "manual_override_duration")
    public Long getManualOverrideDuration() {
        return manualOverrideDuration;
    }

    public void setManualOverrideDuration(Long manualOverrideDuration) {
        this.manualOverrideDuration = manualOverrideDuration;
    }

    /**
     * @return motion sensitivity
     */
    @Column(name = "motion_sensitivity")
    public Long getMotionSensitivity() {
        return motionSensitivity;
    }

    public void setMotionSensitivity(Long motionSensitivity) {
        this.motionSensitivity = motionSensitivity;
    }

    /**
     * @return ramp up time
     */
    @Column(name = "ramp_up_time")
    public Long getRampUpTime() {
        return rampUpTime;
    }

    public void setRampUpTime(Long rampUpTime) {
        this.rampUpTime = rampUpTime;
    }

    /**
     * @return ambient sensitivity
     */
    @Column(name = "ambient_sensitivity")
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

    public int compare(Profile p2) {
        if (this.getMinLevel().intValue() != p2.getMinLevel().intValue())
            return -1;
        if (this.getOnLevel().intValue() != p2.getOnLevel().intValue())
            return -1;
        if (this.getMotionDetectDuration().intValue() != p2.getMotionDetectDuration().intValue())
            return -1;
        if (this.getManualOverrideDuration().intValue() != p2.getManualOverrideDuration().intValue())
            return -1;
        if (this.getMotionSensitivity().intValue() != p2.getMotionSensitivity().intValue())
            return -1;
        if (this.getRampUpTime().intValue() != p2.getRampUpTime().intValue())
            return -1;
        if (this.getAmbientSensitivity().intValue() != p2.getAmbientSensitivity().intValue())
            return -1;
        return 0;
    }
}
