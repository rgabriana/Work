package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="plugloadProfile")
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadProfile implements Serializable{
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*	  id bigint NOT NULL,
	  active_motion_window bigint,
	   mode bigint,
*/
	@XmlElement(name = "id")
	Long id;
	@XmlElement(name = "mode")
	Byte mode;
	@XmlElement(name = "activeMotion")
	Integer activeMotion;	
	@XmlElement(name = "manualOverrideTime")
	Integer manualOverrideTime;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getActiveMotion() {
		return activeMotion;
	}
	public void setActiveMotion(Integer activeMotion) {
		this.activeMotion = activeMotion;
	}
	public Byte getMode() {
		return mode;
	}
	public void setMode(Byte mode) {
		this.mode = mode;
	}

	public void copyFrom(PlugloadProfile target) {
		this.setActiveMotion(target.getActiveMotion());
		this.setMode(target.getMode());	
		this.setManualOverrideTime(target.getManualOverrideTime());
	}
	
	/**
	 * @return the manualOverrideTime
	 */
	public Integer getManualOverrideTime() {
		return manualOverrideTime;
	}
	/**
	 * @param manualOverrideTime the manualOverrideTime to set
	 */
	public void setManualOverrideTime(Integer manualOverrideTime) {
		this.manualOverrideTime = manualOverrideTime;
	}
	
}
