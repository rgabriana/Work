package com.emscloud.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Fixture implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	private long id;
	private String name;
	private Float locX;
	private Float locY;
	private String currProfile;
	private String macAddress;
	private Float baselineEnergy;
	private Long floorId;
	@XmlElement(name = "version")
	private String version;
	
	@XmlElement(name = "gatewayId")
	private Long gatewayId;
	
	
	
	public Long getGatewayId() {
		return gatewayId;
	}
	public void setGatewayId(Long gatewayId) {
		this.gatewayId = gatewayId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return the id
	 */

	@XmlElement(name = "id")
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
		
	/**
	 * @return the name
	 */
	@XmlElement(name = "name")
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
	 * @return the locX
	 */
	@XmlElement(name = "locX")
	public Float getLocX() {
		return locX;
	}
	/**
	 * @param locX the locX to set
	 */
	public void setLocX(Float locX) {
		this.locX = locX;
	}
		
	/**
	 * @return the currProfile
	 */

	@XmlElement(name = "currProfile")
	public String getCurrProfile() {
		return currProfile;
	}
	/**
	 * @param currProfile the currProfile to set
	 */
	public void setCurrProfile(String currProfile) {
		this.currProfile = currProfile;
	}
	
	/**
	 * @return the macAddress
	 */

	@XmlElement(name = "macAddress")
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
	 * @return the locY
	 */

	@XmlElement(name = "locY")
	public Float getLocY() {
		return locY;
	}
	/**
	 * @param locY the locY to set
	 */
	public void setLocY(Float locY) {
		this.locY = locY;
	}
	
	/**
	 * @return the baselineEnergy
	 */
	@XmlElement(name = "baselineEnergy")
	public Float getBaselineEnergy() {
		return baselineEnergy;
	}
	/**
	 * @param baselineEnergy the baselineEnergy to set
	 */
	public void setBaselineEnergy(Float baselineEnergy) {
		this.baselineEnergy = baselineEnergy;
	}
	
	/**
	 * @return the floorId
	 */
	@XmlElement(name = "floorId")
	public Long getFloorId() {
		return floorId;
	}
	/**
	 * @param floorId the floorId to set
	 */
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}
	
}
