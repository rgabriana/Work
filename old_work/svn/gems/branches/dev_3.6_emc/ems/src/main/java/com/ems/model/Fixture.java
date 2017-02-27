package com.ems.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.action.SpringContext;
import com.ems.service.EnergyConsumptionManager;
import com.ems.types.DeviceType;

/**
 *
 @author pankaj kumar chauhan
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Fixture extends Device implements Serializable {

    private static final long serialVersionUID = 6311363006338951159L;

    private String sensorId;
    private String ballastType;
    private Date ballastLastChanged;
    @XmlElement(name = "noofbulbs")
    private Integer noOfBulbs;
    @XmlElement(name = "bulbwattage")
    private Integer bulbWattage;
    @XmlElement(name = "wattage")
    private Integer wattage;
    private String ballastManufacturer;
    private String bulbManufacturer;
    @XmlElement(name = "lightlevel")
    private Integer dimmerControl;
    @XmlElement(name = "currentstate")
    private String currentState;
    @XmlElement(name = "lastoccupancyseen")
    private Integer lastOccupancySeen;
    @XmlElement(name = "ambientlight")
    private Integer lightLevel;
    private SubArea subArea;
    //private ProfileHandler profileHandler;
    @XmlElement(name = "currentprofile")
    private String currentProfile;
    @XmlElement(name = "originalprofilefrom")
    private String originalProfileFrom;
    private String savingsType;
    private Set<EventsAndFault> eventsAndFaults;
    private EnergyConsumption latestEnergyConsumption;
    @XmlElement(name = "snapaddress")
    private String snapAddress;
    private Integer channel;
    private String aesKey;
    @XmlElement(name = "bulblife")
    private Double bulbLife;
    @XmlElement(name = "gateway")
    private Gateway gateway;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "notes")
    private String notes;
    private Date bulbsLastServiceDate;
    @XmlElement(name = "ballastlastservicedate")
    private Date ballastLastServiceDate;
    private boolean active;
    @XmlElement(name = "state")
    private String state;
    @XmlElement(name = "ballast")
    private Ballast ballast;
    @XmlElement(name = "bulb")
    private Bulb bulb;
    @XmlElement(name = "nooffixtures")
    private Integer noOfFixtures;
    @XmlElement(name = "lastconnectivityat")
    private Date lastConnectivityAt;
    private String ipAddress;
    @XmlElement(name = "commtype")
    private Integer commType;
    @XmlElement(name = "laststatsrcvdtime")
    private Date lastStatsRcvdTime;
    private Short profileChecksum;
    private Short globalProfileChecksum;
    @XmlElement(name = "currapp")
    private Short currApp;
    @XmlElement(name = "firmwareversion")
    private String firmwareVersion;
    @XmlElement(name = "bootloaderversion")
    private String bootLoaderVersion;
    @XmlElement(name = "groupid")
    private Long groupId;
    @XmlElement(name = "secgwid")
    private Long secGwId;
    @XmlElement(name = "upgradestatus")
    private String upgradeStatus;

    @XmlElement(name = "fixtureClassId")
    private Long fixtureClassId;

    @XmlElement(name = "fixtureclass")
    private FixtureClass fixtureclass;

    private boolean pushProfile;
    private boolean pushGlobalProfile;

    private String lastCmdSent;
    private Date lastCmdSentAt;
    @XmlElement(name = "lastcmdstatus")
    private String lastCmdStatus;
    @XmlElement(name = "avgtemperature")
    private Double avgTemperature;
    private BigDecimal baselinePower;
    @XmlElement(name = "voltage")
    private Short voltage;
    @XmlElement(name = "commissionstatus")
    private Integer commissionStatus;
    @XmlElement(name = "ishopper")
    private Integer isHopper;
    @XmlElement(name = "versionsynced")
    private Integer versionSynced;
    private Date lastBootTime;
    private Float temperatureOffset;
    @XmlElement(name = "cuversion")
    private String cuVersion;
    private Short resetReason;
    private Integer groupsChecksum;
    private Boolean groups_sync_pending;
    @XmlElement(name = "fxtype")
    private Integer fixtureType;
    @XmlElement(name = "currentAmbientValue")
    private Integer currentAmbientValue;
    @XmlElement(name = "manualAmbientValue")
    private Integer manualAmbientValue;
    @XmlElement(name = "currentTriggerType")
    private Integer currentTriggerType = 0;
    @XmlElement(name = "changeTriggerType")
    private Integer changeTriggerType = 0;
    @XmlElement(name = "occLevelTriggerTime")
    private Integer occLevelTriggerTime = 90;
    @XmlElement(name = "heartbeatStatus")
    private Integer heartbeatStatus;

    private String gatewayNameForFilter;
    private Long gatewayIdForFilter;
    private int mac1ForFilter;
    private int mac2ForFilter;
    private int mac3ForFilter;
    
    private Short lightingOccStatus;
    @XmlElement(name = "dalidrivercount")
    private Short daliDriverCount;
    @XmlElement(name = "manualModeDuration")
    private Short manualModeDuration;
    @XmlElement(name = "blefirmwareversion")
    private String bleFirmwareVersion;
    @XmlElement(name = "blemode")
    private String bleMode;
    
    @XmlElement(name = "dualChannelLedValue")
    private Integer dualChannelLedValue;

  
    public static String unCommissionedStatus = "UN-COMMISSIONED";
    public static String commissionedButNotValidatedStatus = "COMMISSIONED-BUT-NOT-VALIDATED";
    public static String validatedStatus = "VALIDATED";
    @XmlElement(name = "fixtureOut")
    private boolean fixtureOut = false;
    @XmlElement(name = "lampOut")
    private boolean lampOut =false;
    @XmlElement(name = "calibrated")
    private int calibrated=0;
    @XmlElement(name = "curvetype")
    private int curveType=0;
    @XmlElement(name = "outageDescription")
    private String outageDescription="";
    @XmlElement(name = "outageTime")
    private String outageTime="";

    private boolean useFxCurve = true;
    
    @XmlElement(name = "dualChannelLedDuration")
    private Short dualChannelLedDuration;
    
    private Short circRatio;
    
    public Fixture() {
        this.type = DeviceType.Fixture.getName();
        this.fixtureType= 0;
        this.manualModeDuration = 60;
        this.dualChannelLedDuration = 60;
      }

    public String getFixtureName() {

      return name;

    }

    public void setFixtureName(String fixtName) {

      name = fixtName;
    }

    /**
     * @return the sensorId
     */
    public String getSensorId() {
        return sensorId;
    }

    /**
     * @param sensorId
     *            the sensorId to set
     */
    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * @return the subArea
     */
    public SubArea getSubArea() {
        return subArea;
    }

    /**
     * @param subArea
     *            the subArea to set
     */
    public void setSubArea(SubArea subArea) {
        this.subArea = subArea;
    }

    /**
     * @return the ballastType
     */
    public String getBallastType() {
        return ballastType;
    }

    /**
     * @param ballastType
     *            the ballastType to set
     */
    public void setBallastType(String ballastType) {
        this.ballastType = ballastType;
    }

    /**
     * @return the ballastLastChanged
     */
    public Date getBallastLastChanged() {
        return ballastLastChanged;
    }

    /**
     * @param ballastLastChanged
     *            the ballastLastChanged to set
     */
    public void setBallastLastChanged(Date ballastLastChanged) {
        this.ballastLastChanged = ballastLastChanged;
    }

    /**
     * @return the noOfBulbs
     */
    public Integer getNoOfBulbs() {
        return noOfBulbs;
    }

    /**
     * @param noOfBulbs
     *            the noOfBulbs to set
     */
    public void setNoOfBulbs(Integer noOfBulbs) {
        this.noOfBulbs = noOfBulbs;
    }

    /**
     * @return the bulbWattage
     */
    public Integer getBulbWattage() {
        return bulbWattage;
    }

    /**
     * @param bulbWattage
     *            the bulbWattage to set
     */
    public void setBulbWattage(Integer bulbWattage) {
        this.bulbWattage = bulbWattage;
    }

    /**
     * @return the wattage
     */
    public Integer getWattage() {
        return wattage;
    }

    /**
     * @param wattage
     *            the wattage to set
     */
    public void setWattage(Integer wattage) {
        this.wattage = wattage;
    }

    /**
     * @return the ballastManufacturer
     */
    public String getBallastManufacturer() {
        return ballastManufacturer;
    }

    /**
     * @param ballastManufacturer
     *            the ballastManufacturer to set
     */
    public void setBallastManufacturer(String ballastManufacturer) {
        this.ballastManufacturer = ballastManufacturer;
    }

    /**
     * @return the bulbManufacturer
     */
    public String getBulbManufacturer() {
        return bulbManufacturer;
    }

    /**
     * @param bulbManufacturer
     *            the bulbManufacturer to set
     */
    public void setBulbManufacturer(String bulbManufacturer) {
        this.bulbManufacturer = bulbManufacturer;
    }

    /**
     * @return the profileHandler
     */
    /*
    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }
	*/
    /**
     * @param profileHandler
     *            the profileHandler to set
     */
    /*
    public void setProfileHandler(ProfileHandler profileHandler) {
        this.profileHandler = profileHandler;
    }
	*/
    /**
     * @return the currentProfile
     */
    public String getCurrentProfile() {
        return currentProfile;
    }

    /**
     * @param currentProfile
     *            the currentProfile to set
     */
    public void setCurrentProfile(String currentProfile) {
        this.currentProfile = currentProfile;
    }

    /**
     * @return the originalProfileFrom
     */
    public String getOriginalProfileFrom() {
        return originalProfileFrom;
    }

    /**
     * @param originalProfileFrom
     *            the originalProfileFrom to set
     *
     */
    public void setOriginalProfileFrom(String originalProfileFrom) {
        this.originalProfileFrom = originalProfileFrom;
    }

    /**
     * @return the events and faults
     */
    public Set<EventsAndFault> getEventsAndFaults() {
        return eventsAndFaults;
    }

    public void setEventsAndFaults(Set<EventsAndFault> eventsAndFaults) {
        this.eventsAndFaults = eventsAndFaults;
    }

    /**
     * @return the dimmerControl
     */
    public Integer getDimmerControl() {
        return dimmerControl;
    }

    /**
     * @param dimmerControl
     *            the dimmerControl to set
     */
    public void setDimmerControl(Integer dimmerControl) {
        this.dimmerControl = dimmerControl;
    }

    /**
     * @return the currentState
     */
    public String getCurrentState() {
        return currentState;
    }

    /**
     * @param currentState
     *            the currentState to set
     */
    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    /**
     * @return the lastOccupancySeen
     */
    public Integer getLastOccupancySeen() {
        return lastOccupancySeen;
    }

    /**
     * @param lastOccupancySeen
     *            the lastOccupancySeen to set
     */
    public void setLastOccupancySeen(Integer lastOccupancySeen) {
        this.lastOccupancySeen = lastOccupancySeen;
    }

    /**
     * @return the lightLevel
     */
    public Integer getLightLevel() {
        return lightLevel;
    }

    /**
     * @param lightLevel
     *            the lightLevel to set
     */
    public void setLightLevel(Integer lightLevel) {
        this.lightLevel = lightLevel;
    }

    /**
     * @return the savingsType
     */
    public String getSavingsType() {
        return savingsType;
    }

    /**
     * @param savingsType
     *            the savingsType to set
     */
    public void setSavingsType(String savingsType) {
        this.savingsType = savingsType;
    }

    /**
     * @return the latestEnergyConsumption
     */
    public EnergyConsumption getLatestEnergyConsumption() {
        EnergyConsumptionManager energyConsumptionManager = (EnergyConsumptionManager) SpringContext
                .getBean("energyConsumptionManager");
        EnergyConsumption latestEnergyConsumptions = energyConsumptionManager
                .loadLatestEnergyConsumptionByFixtureId(this.id);
        this.latestEnergyConsumption = latestEnergyConsumptions;
        return latestEnergyConsumption;
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
     * @return the channel
     */
    public Integer getChannel() {
        return channel;
    }

    /**
     * @param channel
     *            the channel to set
     */
    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    /**
     * @return the aesKey
     */
    public String getAesKey() {
        return aesKey;
    }

    /**
     * @param aesKey
     *            the aesKey to set
     */
    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    /**
     * @return the bulbLife
     */
    public Double getBulbLife() {
        return bulbLife;
    }

    /**
     * @param bulbLife
     *            the bulbLife to set
     */
    public void setBulbLife(Double bulblife) {
        this.bulbLife = bulblife;
    }

    /**
     * @return the gateway
     */
    public Gateway getGateway() {
        return gateway;
    }

    /**
     * @param gateway
     *            the gateway to set
     */
    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes
     *            the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the bulbsLastServiceDate
     */
    public Date getBulbsLastServiceDate() {
        return bulbsLastServiceDate;
    }

    /**
     * @param bulbsLastServiceDate
     *            the bulbsLastServiceDate to set
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
     * @param ballastLastServiceDate
     *            the ballastLastServiceDate to set
     */
    public void setBallastLastServiceDate(Date ballastLastServiceDate) {
        this.ballastLastServiceDate = ballastLastServiceDate;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the ballast
     */
    public Ballast getBallast() {
        return ballast;
    }

    public void setBallast(Ballast ballast) {
        this.ballast = ballast;
    }

    /**
     * @return the bulb
     */
    public Bulb getBulb() {
        return bulb;
    }

    public void setBulb(Bulb bulb) {
        this.bulb = bulb;
    }

    /**
     * @return number of fixtures
     */
    public Integer getNoOfFixtures() {
        return noOfFixtures;
    }

    public void setNoOfFixtures(Integer noOfFixtures) {
        this.noOfFixtures = noOfFixtures;
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
     * @return IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return comm type
     */
    public Integer getCommType() {
        return commType;
    }

    public void setCommType(Integer commType) {
        this.commType = commType;
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
     * @return profile checksum
     */
    public Short getProfileChecksum() {
        return profileChecksum;
    }

    public void setProfileChecksum(Short profileChecksum) {
        this.profileChecksum = profileChecksum;
    }

    /**
     * @return global profile checksum
     */
    public Short getGlobalProfileChecksum() {
        return globalProfileChecksum;
    }

    public void setGlobalProfileChecksum(Short globalProfileChecksum) {
        this.globalProfileChecksum = globalProfileChecksum;
    }

    /**
     * @return current App
     */
    public Short getCurrApp() {
        return currApp;
    }

    public void setCurrApp(Short currApp) {
        this.currApp = currApp;
    }

    /**
     * @return firmware version
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * @return boot loader version
     */
    public String getBootLoaderVersion() {
        return bootLoaderVersion;
    }

    public void setBootLoaderVersion(String bootLoaderVersion) {
        this.bootLoaderVersion = bootLoaderVersion;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * @return group
     */
    public Long getGroupId() {
        return groupId;
    }

    public void setSecGwId(Long secGwId) {
        this.secGwId = secGwId;
    }

    /**
     * @return secGwId
     */
    public Long getSecGwId() {
        return secGwId;
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

    public void setFixtureClassId(Long fixtureClassId) {
		this.fixtureClassId = fixtureClassId;
    }

	public Long getFixtureClassId() {
		return fixtureClassId;
	}

    public void setPushProfile(boolean pushProfile) {
        this.pushProfile = pushProfile;
    }

    /**
     * @return pushProfile
     */
    public boolean isPushProfile() {
        return this.pushProfile;
    }

    public void setPushGlobalProfile(boolean pushGlobalProfile) {
        this.pushGlobalProfile = pushGlobalProfile;
    }

    /**
     * @return pushGlobalProfile
     */
    public boolean isPushGlobalProfile() {
        return this.pushGlobalProfile;
    }

    public void setLastCmdSent(String lastCmdSent) {
        this.lastCmdSent = lastCmdSent;
    }

    /**
     * @return lastCmdSent
     */
    public String getLastCmdSent() {
        return this.lastCmdSent;
    }

    public void setLastCmdSentAt(Date lastCmdSentAt) {
        this.lastCmdSentAt = lastCmdSentAt;
    }

    /**
     * @return lastCmdSentAt
     */
    public Date getLastCmdSentAt() {
        return this.lastCmdSentAt;
    }

    public void setLastCmdStatus(String lastCmdStatus) {
        this.lastCmdStatus = lastCmdStatus;
    }

    /**
     * @return lastCmdStatus
     */
    public String getLastCmdStatus() {
        return this.lastCmdStatus;
    }

    /**
     * @return the avgTemperature
     */
    public Double getAvgTemperature() {
        return avgTemperature;
    }

    /**
     * @param avgTemperature
     *            the avgTemperature to set
     */
    public void setAvgTemperature(Double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }

    /**
     * @return the baselinePower
     */
    public BigDecimal getBaselinePower() {
        return baselinePower;
    }

    /**
     * @param avgTemperature
     *            the baselinePower to set
     */
    public void setBaselinePower(BigDecimal baselinePower) {
        this.baselinePower = baselinePower;
    }

    /**
     * @return the voltage
     */
    public Short getVoltage() {
        return voltage;
    }

    /**
     * @param voltage
     *            the voltage to set
     */
    public void setVoltage(Short voltage) {
        this.voltage = voltage;
    }

    /**
     * @return the commission_status
     */
    public Integer getCommissionStatus() {
        return commissionStatus;
    }

    /**
     * @param commissionStatus
     *            the commission_status to set
     */
    public void setCommissionStatus(Integer commissionStatus) {
        this.commissionStatus = commissionStatus;
    }

    /**
     * @return the is_hopper
     */
    public Integer getIsHopper() {
        return isHopper;
    }

    /**
     * @param isHopper
     *            the is_hopper to set
     */
    public void setIsHopper(Integer isHopper) {
        this.isHopper = isHopper;
    }

    /**
     * @return the versionSynced
     */
    public Integer getVersionSynced() {
        return versionSynced;
    }

    /**
     * @param versionSynced
     *            the versionSynced to set
     */
    public void setVersionSynced(Integer versionSynced) {
        this.versionSynced = versionSynced;
    }

    /**
     * @return the lastBootTime
     * @hibernate.property column="last_boot_time"
     */
    public Date getLastBootTime() {
        return lastBootTime;
    }

    /**
     * @param versionSynced
     *            the versionSynced to set
     */
    public void setLastBootTime(Date lastBootTime) {
        this.lastBootTime = lastBootTime;
    }

    /**
     * @return the temperatureOffset
     * @hibernate.property column="temperature_offset"
     */
    public Float getTemperatureOffset() {
        return temperatureOffset;
    }

    /**
     * @param temperaturireOffset
     *            the temperatureOffset to set
     */
    public void setTemperatureOffset(Float temperatureOffset) {
        this.temperatureOffset = temperatureOffset;
    }

    /**
     * @return the cuVersion
     * @hibernate.property column="cu_version"
     */
    public String getCuVersion() {
        return cuVersion;
    }

    /**
     * @param cuVersion
     *            the cuVersion to set
     */
    public void setCuVersion(String cuVersion) {
        this.cuVersion = cuVersion;
    }

	public String getGatewayNameForFilter() {
		return gatewayNameForFilter;
	}

	public void setGatewayNameForFilter(String gatewayNameForFilter) {
		if(gateway == null) {
			gateway = new Gateway();
		}
		gateway.setGatewayName(gatewayNameForFilter);
		this.gatewayNameForFilter = gatewayNameForFilter;
	}

	public Long getGatewayIdForFilter() {
		return gatewayIdForFilter;
	}

	public void setGatewayIdForFilter(Long gatewayIdForFilter) {
		if(gateway == null) {
			gateway = new Gateway();
		}
		gateway.setId(gatewayIdForFilter);
		this.gatewayIdForFilter = gatewayIdForFilter;
	}

    public int getMac1ForFilter() {
		return mac1ForFilter;
	}

	public void setMac1ForFilter(int mac1ForFilter) {
		this.mac1ForFilter = mac1ForFilter;
	}

	public int getMac2ForFilter() {
		return mac2ForFilter;
	}

	public void setMac2ForFilter(int mac2ForFilter) {
		this.mac2ForFilter = mac2ForFilter;
	}

	public int getMac3ForFilter() {
		return mac3ForFilter;
	}

	public void setMac3ForFilter(int mac3ForFilter) {
		this.mac3ForFilter = mac3ForFilter;
	}

	public Short getResetReason() {
		return resetReason;
	}

	public void setResetReason(Short resetReason) {
		this.resetReason = resetReason;
	}

	public Integer getGroupsChecksum() {
		return groupsChecksum;
	}

	public void setGroupsChecksum(Integer groupsChecksum) {
		this.groupsChecksum = groupsChecksum;
	}

	public Boolean getGroupsSyncPending() {
	    if (groups_sync_pending == null)
	        return false;
		return groups_sync_pending;
	}

	public void setGroupsSyncPending(Boolean groupsSyncPending) {
		this.groups_sync_pending = groupsSyncPending;
	}

	/**
	 * @return the fixtureOut
	 */
	public boolean isFixtureOut() {
		return fixtureOut;
	}

	/**
	 * @param fixtureOut the fixtureOut to set
	 */
	public void setFixtureOut(boolean fixtureOut) {
		this.fixtureOut = fixtureOut;
	}

	/**
	 * @return the lampOut
	 */
	public boolean isLampOut() {
		return lampOut;
	}

	/**
	 * @param lampOut the lampOut to set
	 */
	public void setLampOut(boolean lampOut) {
		this.lampOut = lampOut;
	}

	/**
	 * @return the curveType
	 */
	public int getCurveType() {
		return curveType;
	}

	/**
	 * @param curveType the curveType to set
	 */
	public void setCurveType(int curveType) {
		this.curveType = curveType;
	}

	/**
	 * @return the outageDescription
	 */
	public String getOutageDescription() {
		return outageDescription;
	}

	/**
	 * @param outageDescription the outageDescription to set
	 */
	public void setOutageDescription(String outageDescription) {
		this.outageDescription = outageDescription;
	}

	/**
	 * @return the outageTime
	 */
	public String getOutageTime() {
		return outageTime;
	}

	/**
	 * @param outageTime the outageTime to set
	 */
	public void setOutageTime(String outageTime) {
		this.outageTime = outageTime;
	}

	public int getCalibrated() {
		return calibrated;
	}

	public void setCalibrated(int calibrated) {
		this.calibrated = calibrated;
	}

	/**
	 * @return the useFxCurve
	 */
	public boolean isUseFxCurve() {
		return useFxCurve;
	}

	/**
	 * @param useFxCurve the useFxCurve to set
	 */
	public void setUseFxCurve(boolean useFxCurve) {
		this.useFxCurve = useFxCurve;
	}

	public void setFixtureclass(FixtureClass fixtureclass) {
		this.fixtureclass = fixtureclass;
	}

	public FixtureClass getFixtureclass() {
		return fixtureclass;
	}

	public Integer getFixtureType() {
		return fixtureType;
	}

	public void setFixtureType(Integer fixtureType) {
		this.fixtureType = fixtureType;
	}

    public Integer getCurrentAmbientValue() {
    	if(currentAmbientValue == null) {
    		return 0;
    	}
        return currentAmbientValue;
    }

    public void setCurrentAmbientValue(Integer currentAmbientValue) {
        this.currentAmbientValue = currentAmbientValue;
    }

    public Integer getManualAmbientValue() {
    	if(manualAmbientValue == null) {
    		return -1;
    	}
        return manualAmbientValue;
    }

    public void setManualAmbientValue(Integer manualAmbientValue) {
        this.manualAmbientValue = manualAmbientValue;
    }

		/**
		 * @return the groups_sync_pending
		 */
		public Boolean getGroups_sync_pending() {
			return groups_sync_pending;
		}

		/**
		 * @param groups_sync_pending the groups_sync_pending to set
		 */
		public void setGroups_sync_pending(Boolean groups_sync_pending) {
			this.groups_sync_pending = groups_sync_pending;
		}

		/**
		 * @return the lightingOccStatus
		 */
		public Short getLightingOccStatus() {
			return lightingOccStatus;
		}

		/**
		 * @param lightingOccStatus the lightingOccStatus to set
		 */
		public void setLightingOccStatus(Short lightingOccStatus) {
			this.lightingOccStatus = lightingOccStatus;
		}

		/**
		 * @return the unCommissionedStatus
		 */
		public static String getUnCommissionedStatus() {
			return unCommissionedStatus;
		}

		/**
		 * @param unCommissionedStatus the unCommissionedStatus to set
		 */
		public static void setUnCommissionedStatus(String unCommissionedStatus) {
			Fixture.unCommissionedStatus = unCommissionedStatus;
		}

		/**
		 * @return the commissionedButNotValidatedStatus
		 */
		public static String getCommissionedButNotValidatedStatus() {
			return commissionedButNotValidatedStatus;
		}

		/**
		 * @param commissionedButNotValidatedStatus the commissionedButNotValidatedStatus to set
		 */
		public static void setCommissionedButNotValidatedStatus(
				String commissionedButNotValidatedStatus) {
			Fixture.commissionedButNotValidatedStatus = commissionedButNotValidatedStatus;
		}

		/**
		 * @return the validatedStatus
		 */
		public static String getValidatedStatus() {
			return validatedStatus;
		}

		/**
		 * @param validatedStatus the validatedStatus to set
		 */
		public static void setValidatedStatus(String validatedStatus) {
			Fixture.validatedStatus = validatedStatus;
		}

		/**
		 * @param latestEnergyConsumption the latestEnergyConsumption to set
		 */
		public void setLatestEnergyConsumption(EnergyConsumption latestEnergyConsumption) {
			this.latestEnergyConsumption = latestEnergyConsumption;
		}

		public Integer getCurrentTriggerType() {
			return currentTriggerType;
		}

		public void setCurrentTriggerType(Integer currentTriggerType) {
			this.currentTriggerType = currentTriggerType;
		}

		public Integer getChangeTriggerType() {
			return changeTriggerType;
		}

		public void setChangeTriggerType(Integer changeTriggerType) {
			this.changeTriggerType = changeTriggerType;
		}

		public void enableZoneOccupancyTriggerType() {
			int currentType = getChangeTriggerType();
			if(currentType == 0) {
				setChangeTriggerType(2);
			}
		}

		public void enableRealtimeOccupancyTriggerType() {
			setChangeTriggerType(6);
		}
		
		public void disableZoneOccupancyTriggerType() {
			int currentType = getChangeTriggerType();
			if(currentType == 2) {
				setChangeTriggerType(0);
			}
		}

		public void disableRealtimeOccupancyTriggerType() {
			if(getArea() != null && getArea().getZoneSensorEnable() != null && getArea().getZoneSensorEnable()) {
				setChangeTriggerType(2);
			}
			else {
				setChangeTriggerType(0);
			}
		}

		public Integer getOccLevelTriggerTime() {
			return occLevelTriggerTime;
		}

		public void setOccLevelTriggerTime(Integer occLevelTriggerTime) {
			this.occLevelTriggerTime = occLevelTriggerTime;
		}

		public void setHeartbeatStatus(Integer heartbeatStatus) {
			this.heartbeatStatus = heartbeatStatus;
		}

		public Integer getHeartbeatStatus() {
			return heartbeatStatus;
		}

		public Short getDaliDriverCount() {
			return daliDriverCount;
		}

		public void setDaliDriverCount(Short daliDriverCount) {
			this.daliDriverCount = daliDriverCount;
		}

		public Short getManualModeDuration() {
			return manualModeDuration;
		}

		public void setManualModeDuration(Short manualModeDuration) {
			this.manualModeDuration = manualModeDuration;
		}

		public String getBleFirmwareVersion() {
			return bleFirmwareVersion;
		}

		public void setBleFirmwareVersion(String bleFirmwareVersion) {
			this.bleFirmwareVersion = bleFirmwareVersion;
		}

		public String getBleMode() {
			return bleMode;
		}

		public void setBleMode(String bleMode) {
			this.bleMode = bleMode;
		}

		public Integer getDualChannelLedValue() {
			return dualChannelLedValue;
		}

		public void setDualChannelLedValue(Integer dualChannelLedValue) {
			this.dualChannelLedValue = dualChannelLedValue;
		}

		public Short getDualChannelLedDuration() {
			return dualChannelLedDuration;
		}

		public void setDualChannelLedDuration(Short dualChannelLedDuration) {
			this.dualChannelLedDuration = dualChannelLedDuration;
		}

		public void setCircRatio(Short circRatio) {
			this.circRatio = circRatio;
		}

		public Short getCircRatio() {
			return circRatio;
		}
}
