package com.ems.model;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */

/**
 * @author yogesh
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureCalibrationMap implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    @XmlElement(name = "id")
    private Long id;
    private FixtureLampCalibration fixtureLampCalibration;
    @XmlElement(name = "volt")
    private Double volt;
    @XmlElement(name = "power")
    private Double power;
    @XmlElement(name = "lux")
    private Double lux;
    @XmlElement(name="enabled")
    private Boolean enabled;
    
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
     * @return the fixtureLampCalibration
     */
    public FixtureLampCalibration getFixtureLampCalibration() {
        return fixtureLampCalibration;
    }
    /**
     * @param fixtureLampCalibration the fixtureLampCalibration to set
     */
    public void setFixtureLampCalibration(FixtureLampCalibration fixtureLampCalibration) {
        this.fixtureLampCalibration = fixtureLampCalibration;
    }

    /**
     * @return the volt
     */
    public Double getVolt() {
        return volt;
    }
    /**
     * @param volt the volt to set
     */
    public void setVolt(Double volt) {
        this.volt = volt;
    }
    /**
     * @return the power
     */
    public Double getPower() {
        return power;
    }
    /**
     * @param power the power to set
     */
    public void setPower(Double power) {
        this.power = power;
    }
    /**
     * @return the lux
     */
    public Double getLux() {
        return lux;
    }
    /**
     * @param lux the lux to set
     */
    public void setLux(Double lux) {
        this.lux = lux;
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

}
