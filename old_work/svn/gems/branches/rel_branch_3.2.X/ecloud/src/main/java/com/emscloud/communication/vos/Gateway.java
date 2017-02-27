package com.emscloud.communication.vos;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.types.DeviceType;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Gateway extends Device implements Serializable {

    private static final long serialVersionUID = -417271237512093198L;
    private String uniqueIdentifierId;
    @XmlElement(name = "status")
    private boolean status;
    @XmlElement(name = "commissioned")
    private boolean commissioned;
    private Set<Fixture> fixtures;

    private String campusName; // Not saving this value in database.View only
    private String buildingName; // Not saving this value in database.View only
    private String floorName; // Not saving this value in database.View only

    @XmlElement(name = "ipaddress")
    private String ipAddress;
    @XmlElement(name = "port")
    private Short port;
    @XmlElement(name = "snapaddress")
    private String snapAddress;
    @XmlElement(name = "gatewaytype")
    private Short gatewayType;
    private Short serialPort;
    @XmlElement(name = "channel")
    private Integer channel; // 0 - 15
    @XmlElement(name = "aeskey")
    private String aesKey; // not used.
    private String userName;
    private String password;
    @XmlElement(name = "wirelessnetworkid")
    private Integer wirelessNetworkId;
    @XmlElement(name = "wirelessencrypttype")
    private Integer wirelessEncryptType;
    @XmlElement(name = "wirelessencryptkey")
    private String wirelessEncryptKey;
    @XmlElement(name = "wirelessradiorate")
    private Integer wirelessRadiorate; // 0 or 2
    private Integer ethSecType; // none = 0, AuthOnly, AuthAndEncrypt
    private Integer ethSecIntegrityType; // MD5 = 0 or SHA1
    private Integer ethSecEncryptType; // none = 0, AES-56 or AES-128
    private String ethSecKey;
    private Integer ethIpaddrType; // static = 0 or locallink
    @XmlElement(name = "app1version")
    private String app1Version;
    @XmlElement(name = "app2version")
    private String app2Version;

    @XmlElement(name = "lastconnectivityat")
    private Date lastConnectivityAt;
    private Date lastStatsRcvdTime;
    private String subnetMask;
    private String defaultGw;
    @XmlElement(name = "noofsensors")
    private Integer noOfSensors;
    
    @XmlElement(name = "noofactivesensors")
    private Integer noOfActiveSensors;

    @XmlElement(name = "upgradestatus")
    private String upgradeStatus;
    @XmlElement(name = "bootloaderversion")
    private String bootLoaderVersion;

    @XmlElement(name = "curruptime")
    private Long currUptime;
    private Long currNoPktsFromGems;
    private Long currNoPktsToGems;
    private Long currNoPktsToNodes;
    private Long currNoPktsFromNodes;
    
    @XmlElement(name = "noofwds")
    private Integer noOfWds;
    
 

    public Gateway() {
      this.type = DeviceType.Gateway.getName();
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
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
     * @return the floor
     */
    public Floor getFloor() {
        return floor;
    }

    /**
     * @param floor
     *            the floor to set
     */
    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    /**
     * @return the campusId
     */
    public Long getCampusId() {
        return campusId;
    }

    /**
     * @param campusId
     *            the campusId to set
     */
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    /**
     * @return the buildingId
     */
    public Long getBuildingId() {
        return buildingId;
    }

    /**
     * @param buildingId
     *            the buildingId to set
     */
    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    /**
     * @return the uniqueIdentifierId
     */
    public String getUniqueIdentifierId() {
        return uniqueIdentifierId;
    }

    /**
     * @param uniqueIdentifierId
     *            the uniqueIdentifierId to set
     */
    public void setUniqueIdentifierId(String uniqueIdentifierId) {
        this.uniqueIdentifierId = uniqueIdentifierId;
    }

    /**
     * @return the status
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * @return the commissioned
     * 
     */
    public boolean isCommissioned() {
        return commissioned;
    }

    /**
     * @param commissioned
     *            the commissioned to set
     */
    public void setCommissioned(boolean commissioned) {
        this.commissioned = commissioned;
    }

    public Set<Fixture> getFixtures() {
        return fixtures;
    }

    /**
     * @param fixtures
     *            the fixtures to set
     */
    public void setFixtures(Set<Fixture> fixtures) {
        this.fixtures = fixtures;
    }

    /**
     * @return the campusName
     */
    public String getCampusName() {
        return campusName;
    }

    /**
     * @param campusName
     *            the campusName to set
     */
    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    /**
     * @return the buildingName
     */
    public String getBuildingName() {
        return buildingName;
    }

    /**
     * @param buildingName
     *            the buildingName to set
     */
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    /**
     * @return the floorName
     */
    public String getFloorName() {
        return floorName;
    }

    /**
     * @param floorName
     *            the floorName to set
     */
    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress
     *            the ipAddress to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the port
     */
    public Short getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(Short port) {
        this.port = port;
    }

    /**
     * @return the snapAddress
     */
    public String getSnapAddress() {
        return snapAddress;
    }

    /**
     * @param snapAddress
     *            the snapAddress to set
     */
    public void setSnapAddress(String snapAddress) {
        this.snapAddress = snapAddress;
    }

    /**
     * @return the gatewayType
     */
    public Short getGatewayType() {
        return gatewayType;
    }

    /**
     * @param gatewayType
     *            the gatewayType to set
     */
    public void setGatewayType(Short gatewayType) {
        this.gatewayType = gatewayType;
    }

    /**
     * @return the serialPort
     */
    public Short getSerialPort() {
        return serialPort;
    }

    /**
     * @param serialPort
     *            the serialPort to set
     */
    public void setSerialPort(Short serialPort) {
        this.serialPort = serialPort;
    }

    /**
     * @param channel
     *            the channel to set
     */
    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    /**
     * @return the channel
     */
    public Integer getChannel() {
        return channel;
    }

    /**
     * @param aesKey
     *            the aesKey to set
     */
    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    /**
     * @return the aesKey
     */
    public String getAesKey() {
        return aesKey;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param gatewayName
     *            the gatewayName to set
     */
    public void setGatewayName(String gatewayName) {
        this.name = gatewayName;
    }

    /**
     * @return the gatewayName
     */
    public String getGatewayName() {
        return name;
    }

    /**
     * @param wirelessNetworkId
     *            the wirelessNetworkId to set
     */
    public void setWirelessNetworkId(Integer wirelessNetworkId) {
        this.wirelessNetworkId = wirelessNetworkId;
    }

    /**
     * @return the wirelessNetworkId
     */
    public Integer getWirelessNetworkId() {
        return wirelessNetworkId;
    }

    /**
     * @param wirelessEncryptType
     *            the wirelessEncryptType to set
     */
    public void setWirelessEncryptType(Integer wirelessEncryptType) {
        this.wirelessEncryptType = wirelessEncryptType;
    }

    /**
     * @return the wirelessEncryptType
     */
    public Integer getWirelessEncryptType() {
        return wirelessEncryptType;
    }

    /**
     * @param wirelessEncryptKey
     *            the wirelessEncryptKey to set
     */
    public void setWirelessEncryptKey(String wirelessEncryptKey) {
        this.wirelessEncryptKey = wirelessEncryptKey;
    }

    /**
     * @return the wirelessEncryptKey
     */
    public String getWirelessEncryptKey() {
        return wirelessEncryptKey;
    }

    /**
     * @param wirelessRadiorate
     *            the wirelessRadiorate to set
     */
    public void setWirelessRadiorate(Integer wirelessRadiorate) {
        this.wirelessRadiorate = wirelessRadiorate;
    }

    /**
     * @return the wirelessRadiorate
     */
    public Integer getWirelessRadiorate() {
        return wirelessRadiorate;
    }

    /**
     * @param ethSecType
     *            the ethSecType to set
     */
    public void setEthSecType(Integer ethSecType) {
        this.ethSecType = ethSecType;
    }

    /**
     * @return the ethSecType
     */
    public Integer getEthSecType() {
        return ethSecType;
    }

    /**
     * @param ethSecIntegrityType
     *            the ethSecIntegrityType to set
     */
    public void setEthSecIntegrityType(Integer ethSecIntegrityType) {
        this.ethSecIntegrityType = ethSecIntegrityType;
    }

    /**
     * @return the ethSecIntegrityType
     */
    public Integer getEthSecIntegrityType() {
        return ethSecIntegrityType;
    }

    /**
     * @param ethSecEncryptType
     *            the ethSecEncryptType to set
     */
    public void setEthSecEncryptType(Integer ethSecEncryptType) {
        this.ethSecEncryptType = ethSecEncryptType;
    }

    /**
     * @return the ethSecEncryptType
     */
    public Integer getEthSecEncryptType() {
        return ethSecEncryptType;
    }

    /**
     * @param ethSecKey
     *            the ethSecKey to set
     */
    public void setEthSecKey(String ethSecKey) {
        this.ethSecKey = ethSecKey;
    }

    /**
     * @return the ethSecKey
     */
    public String getEthSecKey() {
        return ethSecKey;
    }

    /**
     * @param ethIpaddrType
     *            the ethIpaddrType to set
     */
    public void setEthIpaddrType(Integer ethIpaddrType) {
        this.ethIpaddrType = ethIpaddrType;
    }

    /**
     * @return the ethIpaddrType
     */
    public Integer getEthIpaddrType() {
        return ethIpaddrType;
    }

    /**
     * @param app1Version
     *            the app1Version to set
     */
    public void setApp1Version(String app1Version) {
        this.app1Version = app1Version;
    }

    /**
     * @return the app1Version
     */
    public String getApp1Version() {
        return app1Version;
    }

    public void setVersion(String version) {
    	super.setVersion(version);
    	app2Version = version;
    }

    /**
     * @param app2Version
     *            the app2Version to set
     */
    public void setApp2Version(String app2Version) {
        this.app2Version = app2Version;
    }

    /**
     * @return the app2Version
     */
    public String getApp2Version() {
        return version;
    }

    /**
     * @return the currUptime
     */
    public Long getCurrUptime() {
        return currUptime;
    }

    /**
     * @param currUptime
     *            the currUptime to set
     */
    public void setCurrUptime(Long currUptime) {
        this.currUptime = currUptime;
    }

    /**
     * @return the currNoPktsFromGems
     */
    public Long getCurrNoPktsFromGems() {
        return currNoPktsFromGems;
    }

    /**
     * @param currNoPktsFromGems
     *            the currNoPktsFromGems to set
     */
    public void setCurrNoPktsFromGems(Long currNoPktsFromGems) {
        this.currNoPktsFromGems = currNoPktsFromGems;
    }

    /**
     * @return the currnoPktsToGems
     */
    public Long getCurrNoPktsToGems() {
        return currNoPktsToGems;
    }

    /**
     * @param currnoPktsToGems
     *            the currnoPktsToGems to set
     */
    public void setCurrNoPktsToGems(Long currnoPktsToGems) {
        this.currNoPktsToGems = currnoPktsToGems;
    }

    /**
     * @return the currNoPktsToNodes
     */
    public Long getCurrNoPktsToNodes() {
        return currNoPktsToNodes;
    }

    /**
     * @param currNoPktsToNodes
     *            the currNoPktsToNodes to set
     */
    public void setCurrNoPktsToNodes(Long currNoPktsToNodes) {
        this.currNoPktsToNodes = currNoPktsToNodes;
    }

    /**
     * @return the currNoPktsFromNodes
     */
    public Long getCurrNoPktsFromNodes() {
        return currNoPktsFromNodes;
    }

    /**
     * @param currNoPktsFromNodes
     *            the currNoPktsFromNodes to set
     */
    public void setCurrNoPktsFromNodes(Long currNoPktsFromNodes) {
        this.currNoPktsFromNodes = currNoPktsFromNodes;
    }

    /**
     * @return last stats recvd time
     */
    public Date getLastStatsRcvdTime() {
        return lastStatsRcvdTime;
    }

    public void setLastStatsRcvdTime(Date lastStatsRcvdTime) {
        this.lastStatsRcvdTime = lastStatsRcvdTime;
    }

    /**
     * @return last connectivity
     */
    public Date getLastConnectivityAt() {
        return lastConnectivityAt;
    }

    public void setLastConnectivityAt(Date lastConnectivityAt) {
        this.lastConnectivityAt = lastConnectivityAt;
    }

    /**
     * @param subnetMask
     *            the subnetMask to set
     */
    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    /**
     * @return the subnetMask
     */
    public String getSubnetMask() {
        return subnetMask;
    }

    /**
     * @param defaultGw
     *            the defaultGw to set
     */
    public void setDefaultGw(String defaultGw) {
        this.defaultGw = defaultGw;
    }

    /**
     * @return the defaultGw
     */
    public String getDefaultGw() {
        return defaultGw;
    }

    /**
     * @param noOfSensors
     *            the noOfSensors to set
     */
    public void setNoOfSensors(int noOfSensors) {
        this.noOfSensors = noOfSensors;
    }

    /**
     * @return the noOfSensors
     */
    public int getNoOfSensors() {
        return noOfSensors;
    }

    public void setUpgradeStatus(String upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }

    /**
     * @return upgradeStatus
     */
    public String getUpgradeStatus() {
        return upgradeStatus;
    }

    public void setBootLoaderVersion(String bootLoaderVersion) {
        this.bootLoaderVersion = bootLoaderVersion;
    }

    /**
     * @return bootLoaderVersion
     */
    public String getBootLoaderVersion() {
        return bootLoaderVersion;
    }

	/**
	 * @return the hexNetworkId
	 */
	public String getHexWirelessNetworkId() {
		if(wirelessNetworkId != null){
			return Integer.toHexString(wirelessNetworkId) ;
		}
		return "";
	}

	public Integer getNoOfActiveSensors() {
		return noOfActiveSensors;
	}

	public void setNoOfActiveSensors(Integer noOfActiveSensors) {
		this.noOfActiveSensors = noOfActiveSensors;
	}

    /**
     * @return the noOfWds
     */
    public Integer getNoOfWds() {
        return noOfWds;
    }

    /**
     * @param noOfWds the noOfWds to set
     */
    public void setNoOfWds(Integer noOfWds) {
        this.noOfWds = noOfWds;
    }
	  
}
