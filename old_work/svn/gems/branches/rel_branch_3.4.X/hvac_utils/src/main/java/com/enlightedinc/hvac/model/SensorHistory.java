package com.enlightedinc.hvac.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SensorHistory implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7355756475492717077L;
	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "macAddress")
	private String macAddress;
	@XmlElement(name = "captureAt")
	private Date captureAt;
	@XmlElement(name = "motionBits")
	private Long motionBits;
	@XmlElement(name = "avgTemperature")
	private Short avgTemperature;
	@XmlElement(name = "avgAmbientLight")
	private Short avgAmbientLight;
	@XmlElement(name = "zeroBucket")
	private Short zeroBucket;
	@XmlElement(name = "powerUsed")
	private BigDecimal powerUsed;
	@XmlElement(name = "basePowerUsed")
	private BigDecimal basePowerUsed;
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
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}
	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	/**
	 * @return the captureAt
	 */
	public Date getCaptureAt() {
		return captureAt;
	}
	/**
	 * @param captureAt the captureAt to set
	 */
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	/**
	 * @return the motionBits
	 */
	public Long getMotionBits() {
		return motionBits;
	}
	/**
	 * @param motionBits the motionBits to set
	 */
	public void setMotionBits(Long motionBits) {
		this.motionBits = motionBits;
	}
	/**
	 * @return the avgTemperature
	 */
	public Short getAvgTemperature() {
		return avgTemperature;
	}
	/**
	 * @param avgTemperature the avgTemperature to set
	 */
	public void setAvgTemperature(Short avgTemperature) {
		this.avgTemperature = avgTemperature;
	}
	/**
	 * @return the avgAmbientLight
	 */
	public Short getAvgAmbientLight() {
		return avgAmbientLight;
	}
	/**
	 * @param avgAmbientLight the avgAmbientLight to set
	 */
	public void setAvgAmbientLight(Short avgAmbientLight) {
		this.avgAmbientLight = avgAmbientLight;
	}
	/**
	 * @return the zeroBucket
	 */
	public Short getZeroBucket() {
		return zeroBucket;
	}
	/**
	 * @param zeroBucket the zeroBucket to set
	 */
	public void setZeroBucket(Short zeroBucket) {
		this.zeroBucket = zeroBucket;
	}
	/**
	 * @return the powerUsed
	 */
	public BigDecimal getPowerUsed() {
		return powerUsed;
	}
	/**
	 * @param powerUsed the powerUsed to set
	 */
	public void setPowerUsed(BigDecimal powerUsed) {
		this.powerUsed = powerUsed;
	}
	/**
	 * @return the basePowerUsed
	 */
	public BigDecimal getBasePowerUsed() {
		return basePowerUsed;
	}
	/**
	 * @param basePowerUsed the basePowerUsed to set
	 */
	public void setBasePowerUsed(BigDecimal basePowerUsed) {
		this.basePowerUsed = basePowerUsed;
	}
	
}
