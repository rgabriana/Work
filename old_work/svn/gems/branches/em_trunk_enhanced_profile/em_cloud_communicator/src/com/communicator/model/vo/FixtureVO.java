package com.communicator.model.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureVO  implements IData{
	
	@XmlElement(name = "actionType")
	private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "fixtureName")
    private String fixtureName;
	@XmlElement(name = "macAddress")
    private String macAddress;
	@XmlElement(name = "floorId")
    private Long floorId;
	@XmlElement(name = "areaId")
    private Long areaId;
	@XmlElement(name = "version")
    private String version;
	@XmlElement(name = "x")
    private Integer x;
	@XmlElement(name = "y")
    private Integer y;
	@XmlElement(name = "snapAddress")
    private String snapAddress;
	@XmlElement(name = "gatewayId")
    private Long gatewayId;
	@XmlElement(name = "firmwareVersion")
    private String firmwareVersion;
	@XmlElement(name = "bootloaderVersion")
    private String bootloaderVersion;
	@XmlElement(name = "cuVersion")
    private String cuVersion;
	@XmlElement(name = "versionSynced")
    private Integer versionSynced;
	@XmlElement(name = "bulbsLastServiceDate")
    private Date bulbsLastServiceDate;
	@XmlElement(name = "ballastLastServiceDate")
    private Date ballastLastServiceDate;
	@XmlElement(name = "ipAddress")
    private String ipAddress;
	@XmlElement(name = "baselinePower")
    private BigDecimal baselinePower;
	@XmlElement(name = "state")
    private String state;
    
    public FixtureVO() {
    }
    
    public FixtureVO(String actionType, Long id, String fixtureName, String macAddress, Long floorId, Long areaId, 
    		String version, Integer x, Integer y, String snapAddress, Long gatewayId, String firmwareVersion,
    		String bootloaderVersion, String cuVersion, Integer versionSynced, Date bulbsLastServiceDate, 
    		Date ballastLastServiceDate, String ipAddress, BigDecimal baselinePower, String state) {
    	
    	this.actionType = actionType;
    	this.id = id;
    	this.fixtureName = fixtureName;
    	this.macAddress = macAddress;
    	this.floorId = floorId;
    	this.areaId = areaId;
    	this.version = version;
    	this.x = x;
    	this.y = y;
    	this.snapAddress = snapAddress;
    	this.gatewayId = gatewayId;
    	this.firmwareVersion = firmwareVersion;
    	this.bootloaderVersion = bootloaderVersion;
    	this.cuVersion = cuVersion;
    	this.versionSynced = versionSynced;
    	this.bulbsLastServiceDate = bulbsLastServiceDate;
    	this.ballastLastServiceDate = ballastLastServiceDate;
    	this.ipAddress = ipAddress;
    	this.baselinePower = baselinePower;
    	this.setState(state);
    	
    	
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
	 * @return the fixtureName
	 */
	public String getFixtureName() {
		return fixtureName;
	}

	/**
	 * @param fixtureName the fixtureName to set
	 */
	public void setFixtureName(String fixtureName) {
		this.fixtureName = fixtureName;
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
	 * @return the areaId
	 */
	public Long getAreaId() {
		return areaId;
	}

	/**
	 * @param areaId the areaId to set
	 */
	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
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
	 * @return the gatewayId
	 */
	public Long getGatewayId() {
		return gatewayId;
	}

	/**
	 * @param gatewayId the gatewayId to set
	 */
	public void setGatewayId(Long gatewayId) {
		this.gatewayId = gatewayId;
	}

	/**
	 * @return the firmwareVersion
	 */
	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	/**
	 * @param firmwareVersion the firmwareVersion to set
	 */
	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * @return the bootloaderVersion
	 */
	public String getBootloaderVersion() {
		return bootloaderVersion;
	}

	/**
	 * @param bootloaderVersion the bootloaderVersion to set
	 */
	public void setBootloaderVersion(String bootloaderVersion) {
		this.bootloaderVersion = bootloaderVersion;
	}

	/**
	 * @return the cuVersion
	 */
	public String getCuVersion() {
		return cuVersion;
	}

	/**
	 * @param cuVersion the cuVersion to set
	 */
	public void setCuVersion(String cuVersion) {
		this.cuVersion = cuVersion;
	}

	/**
	 * @return the versionSynced
	 */
	public Integer getVersionSynced() {
		return versionSynced;
	}

	/**
	 * @param versionSynced the versionSynced to set
	 */
	public void setVersionSynced(Integer versionSynced) {
		this.versionSynced = versionSynced;
	}

	/**
	 * @return the bulbsLastServiceDate
	 */
	public Date getBulbsLastServiceDate() {
		return bulbsLastServiceDate;
	}

	/**
	 * @param bulbsLastServiceDate the bulbsLastServiceDate to set
	 */
	public void setBulbsLastServiceDate(Date bulbsLastServiceDate) {
		this.bulbsLastServiceDate = bulbsLastServiceDate;
	}

	/**
	 * @return the ballastLastServiceDate
	 */
	public Date getBallastLastServiceDate() {
		return ballastLastServiceDate;
	}

	/**
	 * @param ballastLastServiceDate the ballastLastServiceDate to set
	 */
	public void setBallastLastServiceDate(Date ballastLastServiceDate) {
		this.ballastLastServiceDate = ballastLastServiceDate;
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
	 * @return the baselinePower
	 */
	public BigDecimal getBaselinePower() {
		return baselinePower;
	}

	/**
	 * @param baselinePower the baselinePower to set
	 */
	public void setBaselinePower(BigDecimal baselinePower) {
		this.baselinePower = baselinePower;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

}
