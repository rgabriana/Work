package com.communicator.model.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GatewayVO implements IData {
	
	@XmlElement(name = "actionType")
	private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "gatewayName")
    private String gatewayName;
	@XmlElement(name = "floorId")
    private Long floorId;
	@XmlElement(name = "x")
    private Integer x;
	@XmlElement(name = "y")
    private Integer y;
	@XmlElement(name = "macAddress")
    private String macAddress;
	@XmlElement(name = "app1Version")
    private String app1Version;
	@XmlElement(name = "app2Version")
    private String app2Version;
	@XmlElement(name = "ipAddress")
    private String ipAddress;
	@XmlElement(name = "snapAddress")
    private String snapAddress;
	@XmlElement(name = "bootLoaderVersion")
    private String bootLoaderVersion;
	@XmlElement(name = "noOfSensors")
    private Integer noOfSensors;
    
    public GatewayVO() {
    }
    
    public GatewayVO(String actionType, Long id, String gatewayName, Long floorId, 
    		Integer x, Integer y, String macAddress, String app1Version, String app2Version,
    		String ipAddress, String snapAddress, String bootLoaderVersion, Integer noOfSensors) {
    	this.actionType = actionType;
    	this.id = id;
    	this.gatewayName = gatewayName;
    	this.floorId = floorId;
    	this.x = x;
    	this.y = y;
    	this.macAddress = macAddress;
    	this.app1Version = app1Version;
    	this.app2Version = app2Version;
    	this.ipAddress = ipAddress;
    	this.snapAddress = snapAddress;
    	this.bootLoaderVersion = bootLoaderVersion;
    	this.noOfSensors = noOfSensors;
    }

	/**
	 * @return the actionType
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

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
	 * @return the gatewayName
	 */
	public String getGatewayName() {
		return gatewayName;
	}

	/**
	 * @param gatewayName the gatewayName to set
	 */
	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	/**
	 * @return the floorId
	 */
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
	 * @return the x
	 */
	public Integer getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(Integer x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public Integer getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(Integer y) {
		this.y = y;
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
	 * @return the app1Version
	 */
	public String getApp1Version() {
		return app1Version;
	}

	/**
	 * @param app1Version the app1Version to set
	 */
	public void setApp1Version(String app1Version) {
		this.app1Version = app1Version;
	}

	/**
	 * @return the app2Version
	 */
	public String getApp2Version() {
		return app2Version;
	}

	/**
	 * @param app2Version the app2Version to set
	 */
	public void setApp2Version(String app2Version) {
		this.app2Version = app2Version;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the snapAddress
	 */
	public String getSnapAddress() {
		return snapAddress;
	}

	/**
	 * @param snapAddress the snapAddress to set
	 */
	public void setSnapAddress(String snapAddress) {
		this.snapAddress = snapAddress;
	}

	/**
	 * @return the bootLoaderVersion
	 */
	public String getBootLoaderVersion() {
		return bootLoaderVersion;
	}

	/**
	 * @param bootLoaderVersion the bootLoaderVersion to set
	 */
	public void setBootLoaderVersion(String bootLoaderVersion) {
		this.bootLoaderVersion = bootLoaderVersion;
	}

	/**
	 * @return the noOfSensors
	 */
	public Integer getNoOfSensors() {
		return noOfSensors;
	}

	/**
	 * @param noOfSensors the noOfSensors to set
	 */
	public void setNoOfSensors(Integer noOfSensors) {
		this.noOfSensors = noOfSensors;
	}

}
