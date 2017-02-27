package com.emscloud.vo;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Device implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	private long id;
	private String name;
	private Float locX;
	private Float locY;	
	private String macAddress;	
	private Long floorId;
	private String type;	
	@XmlElement(name = "version")
	private String version;
	private String switchGroups;
	private String motionGroups;
	private Date lastConnectivity;
	private String hardwareVersion;
	private Short channel;
	private Integer networkId;
	private String state;
	private Integer isHopper;
	
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
	
	/**
	 * @return the type
	 */
	@XmlElement(name = "type")
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	@XmlElement(name = "switchGroups")
	public String getSwitchGroups() {
		return switchGroups;
	}
	public void setSwitchGroups(String switchGroups) {
		this.switchGroups = switchGroups;
	}
	
	@XmlElement(name = "motionGroups")
	public String getMotionGroups() {
		return motionGroups;
	}
	public void setMotionGroups(String motionGroups) {
		this.motionGroups = motionGroups;
	}
	
	@XmlElement(name = "lastConnectivity")
	public Date getLastConnectivity() {
		return lastConnectivity;
	}
	public void setLastConnectivity(Date lastConnectivity) {
		this.lastConnectivity = lastConnectivity;
	}
	
	@XmlElement(name = "hardwareVersion")
	public String getHardwareVersion() {
		return hardwareVersion;
	}
	public void setHardwareVersion(String hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}
	
	@XmlElement(name = "channel")
	public Short getChannel() {
		return channel;
	}
	public void setChannel(Short channel) {
		this.channel = channel;
	}
	
	@XmlElement(name = "networkId")
	public Integer getNetworkId() {
		return networkId;
	}
	public void setNetworkId(Integer networkId) {
		this.networkId = networkId;
	}
	
	@XmlElement(name = "state")
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	@XmlElement(name = "isHopper")
	public Integer getIsHopper() {
		return isHopper;
	}
	public void setIsHopper(Integer isHopper) {
		this.isHopper = isHopper;
	}
	
	
	
}
