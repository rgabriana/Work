package com.emscloud.model.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Profile implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2188689168612406084L;
	
	public Short minLight;
	public Short maxLight;
	public Short rampupTime;
	public Short activeMotionWindow;
	public Short motionSensitivity;
	public Short ambientSensitivity;
	
	
	/**
	 * @return the minLight
	 */
	@XmlElement(name = "minLight")
	public Short getMinLight() {
		return minLight;
	}
	/**
	 * @param minLight the minLight to set
	 */
	public void setMinLight(Short minLight) {
		this.minLight = minLight;
	}
	/**
	 * @return the maxLight
	 */
	@XmlElement(name = "maxLight")
	public Short getMaxLight() {
		return maxLight;
	}
	/**
	 * @param maxLight the maxLight to set
	 */
	public void setMaxLight(Short maxLight) {
		this.maxLight = maxLight;
	}
	/**
	 * @return the rampupTime
	 */
	@XmlElement(name = "rampupTime")
	public Short getRampupTime() {
		return rampupTime;
	}
	/**
	 * @param rampupTime the rampupTime to set
	 */
	public void setRampupTime(Short rampupTime) {
		this.rampupTime = rampupTime;
	}
	/**
	 * @return the activeMotionWindow
	 */
	@XmlElement(name = "activeMotionWindow")
	public Short getActiveMotionWindow() {
		return activeMotionWindow;
	}
	/**
	 * @param activeMotionWindow the activeMotionWindow to set
	 */
	public void setActiveMotionWindow(Short activeMotionWindow) {
		this.activeMotionWindow = activeMotionWindow;
	}
	/**
	 * @return the motionSensitivity
	 */
	@XmlElement(name = "motionSensitivity")
	public Short getMotionSensitivity() {
		return motionSensitivity;
	}
	/**
	 * @param motionSensitivity the motionSensitivity to set
	 */
	public void setMotionSensitivity(Short motionSensitivity) {
		this.motionSensitivity = motionSensitivity;
	}
	/**
	 * @return the ambientSensitivity
	 */
	@XmlElement(name = "ambientSensitivity")
	public Short getAmbientSensitivity() {
		return ambientSensitivity;
	}
	/**
	 * @param ambientSensitivity the ambientSensitivity to set
	 */
	public void setAmbientSensitivity(Short ambientSensitivity) {
		this.ambientSensitivity = ambientSensitivity;
	}
	

}
