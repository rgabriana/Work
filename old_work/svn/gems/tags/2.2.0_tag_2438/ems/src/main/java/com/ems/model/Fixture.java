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

/**
 * 
 @author pankaj kumar chauhan
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Fixture implements Serializable {

    private static final long serialVersionUID = 6311363006338951159L;
    @XmlElement(name = "id")
    private Long id;
    private String sensorId;
    private String type;
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
    @XmlElement(name = "xaxis")
    private Integer xaxis;
    @XmlElement(name = "yaxis")
    private Integer yaxis;
    @XmlElement(name = "lightlevel")
    private Integer dimmerControl;
    @XmlElement(name = "currentstate")
    private String currentState;
    @XmlElement(name = "lastoccupancyseen")
    private Integer lastOccupancySeen;
    @XmlElement(name = "ambientlight")
    private Integer lightLevel;
    private Floor floor;
    @XmlElement(name = "area")
    private Area area;
    private SubArea subArea;
    @XmlElement(name = "campusid")
    private Long campusId;
    @XmlElement(name = "buildingid")
    private Long buildingId;
    //private ProfileHandler profileHandler;
    @XmlElement(name = "currentprofile")
    private String currentProfile;
    @XmlElement(name = "originalprofilefrom")
    private String originalProfileFrom;
    @XmlElement(name = "location")
    private String location;
    private String savingsType;
    private Set<EventsAndFault> eventsAndFaults;
    private EnergyConsumption latestEnergyConsumption;
    @XmlElement(name = "snapaddress")
    private String snapAddress;
    @XmlElement(name = "name")
    private String fixtureName;
    private String macAddress;
    private Integer channel;
    @XmlElement(name = "version")
    private String version;
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
    private boolean pushProfile;
    private boolean pushGlobalProfile;

    private String lastCmdSent;
    private Date lastCmdSentAt;
    @XmlElement(name = "lastcmdstatus")
    private String lastCmdStatus;
    @XmlElement(name = "avgtemperature")
    private Short avgTemperature;
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
    private String cuVersion;
    private String modelNo;
    
    private String gatewayNameForFilter;
    private Long gatewayIdForFilter;
    private int mac1ForFilter;
    private int mac2ForFilter;
    private int mac3ForFilter;
    
    @XmlElement (name = "floorId")
    private Long floorId;

	public Fixture() {
    }

    public Fixture(Long id, String sensorId, String type, String ballastType, Date ballastLastChanged,
            Integer noOfBulbs, Integer xaxis, Integer yaxis, Long floorId, Long buildingId, Long campusId, Long areaId,
            Long subAreaId, Integer dimmerControl, String currentState, Integer lastOccupancySeen, Integer lightLevel,
            String savingsType, String snapAddress, String fixtureName, String macAddress, Integer channel,
            String version, String aesKey, Double bulbLife, Long gatewayId, String gatewayName, String description,
            String notes, Date bulbsLastServiceDate, Date ballastLastServiceDate, boolean active, String state,
            Long ballastId, Long itemNum, String ballastName, String inputVoltage, String lampType, Integer lampNum,
            Double ballastFactor, Integer wattage, String ballastManufacturer, Long bulbId, String bulbManufacturer,
            String bulbName, String bulbType, Long initialLumens, Long designLumens, Integer energy, Long lifeInsStart,
            Long lifeProgStart, Integer diameter, Double length, Integer cri, Integer colorTemp, String currentProfile,
            String originalProfileFrom, String location, Integer noOfFixtures, Long profileHandlerId,
            Date lastConnectivityAt, String ipAddress, Integer commType, Date lastStatsRcvdTime, Short profileChecksum,
            Short globalProfileChecksum, Short currApp, String firmwareVersion, String bootLoaderVersion, Long groupId,
            boolean pushProfile, boolean pushGlobalProfile, Integer currWattage, Long voltPowerMapId, Long secGwId,
            String upgradeStatus, Short avgTemperature, BigDecimal baselinePower, Short voltage,
            Integer commissionStatus, Integer isHopper, Integer versionSynced, Date lastBootTime,
            Float temperatureOffset, String cuVersion, String modelNo) {
        this.id = id;
        this.sensorId = sensorId;
        this.type = type;
        this.ballastType = ballastType;
        this.ballastLastChanged = ballastLastChanged;
        this.noOfBulbs = noOfBulbs;
        this.bulbWattage = wattage;
        this.wattage = currWattage;
        this.ballastManufacturer = ballastManufacturer;
        this.bulbManufacturer = bulbManufacturer;
        this.xaxis = xaxis;
        this.yaxis = yaxis;
        this.dimmerControl = dimmerControl;
        this.currentState = currentState;
        this.lastOccupancySeen = lastOccupancySeen;
        this.lightLevel = lightLevel;
        Floor floor = new Floor();
        floor.setId(floorId);
        this.floor = floor;
        this.buildingId = buildingId;
        this.campusId = campusId;
        Area area = new Area();
        area.setId(areaId);
        this.area = area;
        SubArea subArea = new SubArea();
        subArea.setId(subAreaId);
        this.subArea = subArea;
        this.savingsType = savingsType;
        this.snapAddress = snapAddress;
        this.fixtureName = fixtureName;
        this.macAddress = macAddress;
        this.channel = channel;
        this.version = version;
        this.aesKey = aesKey;
        this.bulbLife = bulbLife;
        Gateway gateway = new Gateway();
        gateway.setId(gatewayId);
        gateway.setGatewayName(gatewayName);
        this.setGateway(gateway);
        this.description = description;
        this.notes = notes;
        this.bulbsLastServiceDate = bulbsLastServiceDate;
        this.ballastLastServiceDate = ballastLastServiceDate;
        this.active = active;
        this.state = state;
        Ballast ballast = new Ballast();
        ballast.setId(ballastId);
        ballast.setLampNum(lampNum);
        ballast.setWattage(wattage);
        Integer fixtureWattage = wattage * lampNum;
        ballast.setFixtureWattage(fixtureWattage);
        ballast.setLampType(lampType);
        ballast.setBallastManufacturer(ballastManufacturer);
        ballast.setBallastName(ballastName);
        ballast.setInputVoltage(inputVoltage);
        ballast.setBallastFactor(ballastFactor);
        ballast.setVoltPowerMapId(voltPowerMapId);
        this.setBallast(ballast);
        Bulb bulb = new Bulb();
        bulb.setId(bulbId);
        bulb.setManufacturer(bulbManufacturer);
        bulb.setBulbName(bulbName);
        bulb.setType(bulbType);
        bulb.setInitialLumens(initialLumens);
        bulb.setDesignLumens(designLumens);
        bulb.setEnergy(energy);
        bulb.setLifeInsStart(lifeInsStart);
        bulb.setLifeProgStart(lifeProgStart);
        bulb.setDiameter(diameter);
        bulb.setLength(length);
        bulb.setCri(cri);
        bulb.setColorTemp(colorTemp);
        this.setBulb(bulb);
        this.setCurrentProfile(currentProfile);
        this.setOriginalProfileFrom(originalProfileFrom);
        this.setLocation(location);
        this.setNoOfFixtures(noOfFixtures);
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        //this.setProfileHandler(profileHandler);
        this.lastConnectivityAt = lastConnectivityAt;
        this.ipAddress = ipAddress;
        this.commType = commType;
        this.lastStatsRcvdTime = lastStatsRcvdTime;
        this.profileChecksum = profileChecksum;
        this.globalProfileChecksum = globalProfileChecksum;
        this.currApp = currApp;
        this.firmwareVersion = firmwareVersion;
        this.bootLoaderVersion = bootLoaderVersion;
        this.groupId = groupId;
        this.pushProfile = pushProfile;
        this.pushGlobalProfile = pushGlobalProfile;
        // getLatestEnergyConsumption();
        this.secGwId = secGwId;
        this.upgradeStatus = upgradeStatus;
        this.avgTemperature = avgTemperature;
        this.baselinePower = baselinePower;
        this.voltage = voltage;
        this.commissionStatus = commissionStatus;
        this.isHopper = isHopper;
        this.versionSynced = versionSynced;
        this.lastBootTime = lastBootTime;
        this.temperatureOffset = temperatureOffset;
        this.cuVersion = cuVersion;
	    this.modelNo = modelNo;
    }

    public static String unCommissionedStatus = "UN-COMMISSIONED";
    public static String commissionedButNotValidatedStatus = "COMMISSIONED-BUT-NOT-VALIDATED";
    public static String validatedStatus = "VALIDATED";

    /**
     * @return the id
     * @hibernate.id generator-class="native" unsaved-value="null"
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
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
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
     * @return the area
     */
    public Area getArea() {
        return area;
    }

    /**
     * @param area
     *            the area to set
     */
    public void setArea(Area area) {
        this.area = area;
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
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
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
     * @return the xaxis
     */
    public Integer getXaxis() {
        return xaxis;
    }

    /**
     * @param xaxis
     *            the xaxis to set
     */
    public void setXaxis(Integer xaxis) {
        this.xaxis = xaxis;
    }

    /**
     * @return the yaxis
     */
    public Integer getYaxis() {
        return yaxis;
    }

    /**
     * @param yaxis
     *            the yaxis to set
     */
    public void setYaxis(Integer yaxis) {
        this.yaxis = yaxis;
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
     * @return the fixtureName
     */
    public String getFixtureName() {
        return fixtureName;
    }

    /**
     * @param fixtureName
     *            the fixtureName to set
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
     * @param macAddress
     *            the macAddress to set
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
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
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
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
    public Short getAvgTemperature() {
        return avgTemperature;
    }

    /**
     * @param avgTemperature
     *            the avgTemperature to set
     */
    public void setAvgTemperature(Short avgTemperature) {
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

	/**
     * @return the modelNo
     * @hibernate.property column="model_no"
     */
    public String getModelNo() {
        return modelNo;
    }

    /**
     * @param modelNo
     *            the modelNo to set
     */
    public void setModelNo(String modelNo) {
        this.modelNo = modelNo;
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
}
