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
 * @author Shilpa Nene
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MotionBitsScheduler implements Serializable {
			 
    /**
	 * 
	 */
	private static final long serialVersionUID = -5350410629216941620L;
	
	@XmlElement(name = "id")
    private Long		id;
	@XmlElement(name = "displayName")
	private String		displayName;
    @XmlElement(name = "name")
	private String		name;
	@XmlElement(name = "captureStart")
	private String		captureStart;
    @XmlElement(name = "captureEnd")
	private String		captureEnd;
    @XmlElement(name = "transmitFreq")
	private Integer		transmitFreq;
    @XmlElement(name = "bitLevel")
	private Short 		bitLevel;
    @XmlElement(name = "daysOfWeek")
	private Short		daysOfWeek;
//    @XmlElement(name = "oneTime")
//	private Boolean		oneTime;
    @XmlElement(name = "motionBitGroup")
    private GemsGroup	motionBitGroup;
    
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the motionBitGroup
	 */
	public GemsGroup getMotionBitGroup() {
		return motionBitGroup;
	}
	/**
	 * @param motionBitGroup the motionBitGroup to set
	 */
	public void setMotionBitGroup(GemsGroup motionBitGroup) {
		this.motionBitGroup = motionBitGroup;
	}
	/**
	 * @return the captureStart
	 */
	public String getCaptureStart() {
		return captureStart;
	}
	/**
	 * @param captureStart the captureStart to set
	 */
	public void setCaptureStart(String captureStart) {
		this.captureStart = captureStart;
	}
	/**
	 * @return the captureEnd
	 */
	public String getCaptureEnd() {
		return captureEnd;
	}
	/**
	 * @param captureEnd the captureEnd to set
	 */
	public void setCaptureEnd(String captureEnd) {
		this.captureEnd = captureEnd;
	}
	/**
	 * @return the transmitFreq
	 */
	public Integer getTransmitFreq() {
		return transmitFreq;
	}
	/**
	 * @param transmitFreq the transmitFreq to set
	 */
	public void setTransmitFreq(Integer transmitFreq) {
		this.transmitFreq = transmitFreq;
	}
	/**
	 * @return the bitLevel
	 */
	public Short getBitLevel() {
		return bitLevel;
	}
	/**
	 * @param bitLevel the bitLevel to set
	 */
	public void setBitLevel(Short bitLevel) {
		this.bitLevel = bitLevel;
	}
	/**
	 * @return the daysOfWeek
	 */
	public Short getDaysOfWeek() {
		return daysOfWeek;
	}
	/**
	 * @param daysOfWeek the daysOfWeek to set
	 */
	public void setDaysOfWeek(Short daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}
	/**
	 * @return the oneTime
	 */
//	public Boolean getOneTime() {
//		return oneTime;
//	}
	/**
	 * @param oneTime the oneTime to set
	 */
//	public void setOneTime(Boolean oneTime) {
//		this.oneTime = oneTime;
//	}


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

   
}