package com.ems.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.action.SpringContext;
import com.ems.service.PlugloadEnergyConsumptionManager;
import com.ems.types.DeviceType;

/*
 * 
 @author Shrihari Mundada
 * */

@XmlRootElement(name = "plugload")
@XmlAccessorType(XmlAccessType.NONE)
public class Plugload extends Device implements Serializable {

	private static final long serialVersionUID = 6311363006338951159L;
	@XmlElement(name = "id")
	Integer plugloadId;
	@XmlElement(name = "profileId")
	Integer profileId;
	@XmlElement(name = "managedLoad")
	Float managedLoad;
	@XmlElement(name = "unmanagedLoad")
	Float unmanagedLoad;
	//@XmlElement(name = "profileHandlerId")
	//PlugloadProfileHandler profileHandler;
	@XmlElement(name = "currentProfile")
	private String currentProfile;
	@XmlElement(name = "originalProfileFrom")
	private String originalProfileFrom;
	@XmlElement(name = "currentState")
	String currentState;

	@XmlElement(name = "snapAddress")
	String snapAddress;
	@XmlElement(name = "gateway")
	Gateway gateway;
	private String gatewayNameForFilter;

	@XmlElement(name = "description")
	String description;
	@XmlElement(name = "notes")
	String notes;

	@XmlElement(name = "active")
	Boolean active;
	@XmlElement(name = "state")
	String state;
	@XmlElement(name = "lastconnectivityat")
	Date lastConnectivityAt;
	@XmlElement(name = "globalProfileChecksum")
	Short globalProfileChecksum;
	@XmlElement(name = "currApp")
	Short currApp;
	@XmlElement(name = "firmwareVersion")
	String firmwareVersion;
	@XmlElement(name = "bootloaderVersion")
	String bootLoaderVersion;
	@XmlElement(name = "groupId")
	private Long groupId;
	@XmlElement(name = "secGwId")
	Long secGwId;
	@XmlElement(name = "upgradeStatus")
	String upgradeStatus;
	@XmlElement(name = "scheduledProfileChecksum")
	private Integer scheduledProfileChecksum;
	private Boolean groupsSyncPending;
	private Long gatewayIdForFilter;
	@XmlElement(name = "pushProfile")
	private boolean pushProfile;
	@XmlElement(name = "pushGlobalProfile")
	private boolean pushGlobalProfile;
	@XmlElement(name = "managedBaselineLoad")
	BigDecimal managedBaselineLoad;
	@XmlElement(name = "unmanagedBaselineLoad")
	BigDecimal unmanagedBaselineLoad;	
	@XmlElement(name = "predefinedBaselineLoad")
	Float predefinedBaselineLoad;	
	@XmlElement(name = "voltage")
	Short voltage;
	@XmlElement(name = "commissionStatus")
	Integer commissionStatus;
	@XmlElement(name = "isHopper")
	Integer isHopper;
	@XmlElement(name = "versionSynced")
	Integer versionSynced;
	@XmlElement(name = "temperatureOffset")
	Float temperatureOffset;
	@XmlElement(name = "lastBootTime")
	Date lastBootTime;
	@XmlElement(name = "lastStatsRcvdTime")
	Date lastStatsRcvdTime;
	@XmlElement(name = "lastZBUpdateTime")
	Date lastZBUpdateTime;
	@XmlElement(name = "cuVersion")
	String cuVersion;
	@XmlElement(name = "currentDataId")
	Integer currentDataId;
	@XmlElement(name = "resetReason")
	Integer resetReason;
	@XmlElement(name = "configChecksum")
	Integer configChecksum; //this is similar to groupsChecksum in fixture
	@XmlElement(name = "groupSyncPending")
	Boolean groupSyncPending;
	@XmlElement(name = "commissionedTime")
	Date commissionedTime;
	@XmlElement(name = "avgTemperature")
	Float avgTemperature;
	@XmlElement(name = "avgVolts")
	Float avgVolts;
	@XmlElement(name = "lastoccupancyseen")
    private Integer lastOccupancySeen;
	
	private PlugloadEnergyConsumption latestPlugloadEnergyConsumption;
	
	public Plugload() {
		this.type = DeviceType.Plugload.getName();
	}

	public Integer getPlugloadId() {
		return plugloadId;
	}

	public void setPlugloadId(Integer plugloadId) {
		this.plugloadId = plugloadId;
	}

	public Integer getProfileId() {
		return profileId;
	}

	public void setProfileId(Integer profileId) {
		this.profileId = profileId;
	}

	public Float getManagedLoad() {
		return managedLoad;
	}

	public void setManagedLoad(Float managedLoad) {
		this.managedLoad = managedLoad;
	}

	public Float getUnmanagedLoad() {
		return unmanagedLoad;
	}

	public void setUnmanagedLoad(Float unmanagedLoad) {
		this.unmanagedLoad = unmanagedLoad;
	}

	public String getCurrentProfile() {
		return currentProfile;
	}

	public void setCurrentProfile(String currentProfile) {
		this.currentProfile = currentProfile;
	}

	public String getOriginalProfileFrom() {
		return originalProfileFrom;
	}

	public void setOriginalProfileFrom(String originalProfileFrom) {
		this.originalProfileFrom = originalProfileFrom;
	}

	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public String getSnapAddress() {
		return snapAddress;
	}

	public void setSnapAddress(String snapAddress) {
		this.snapAddress = snapAddress;
	}

	/*
	public PlugloadProfileHandler getProfileHandler() {
		return profileHandler;
	}

	public void setProfileHandler(PlugloadProfileHandler profileHandler) {
		this.profileHandler = profileHandler;
	}
	*/

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getLastConnectivityAt() {
		return lastConnectivityAt;
	}

	public void setLastConnectivityAt(Date lastConnectivityAt) {
		this.lastConnectivityAt = lastConnectivityAt;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getBootLoaderVersion() {
		return bootLoaderVersion;
	}

	public void setBootLoaderVersion(String bootloaderVersion) {
		this.bootLoaderVersion = bootloaderVersion;
	}

	public String getUpgradeStatus() {
		return upgradeStatus;
	}

	public void setUpgradeStatus(String upgradeStatus) {
		this.upgradeStatus = upgradeStatus;
	}

	public boolean isPushProfile() {
		return pushProfile;
	}

	public void setPushProfile(Boolean pushProfile) {
		this.pushProfile = pushProfile;
	}

	/*
	 * public String getLastCmdStatus() { return lastCmdStatus; } public void
	 * setLastCmdStatus(String lastCmdStatus) { this.lastCmdStatus =
	 * lastCmdStatus; }
	 */

	public BigDecimal getManagedBaselineLoad() {
		return managedBaselineLoad;
	}

	public void setManagedBaselineLoad(BigDecimal managedBaselineLoad) {
		this.managedBaselineLoad = managedBaselineLoad;
	}

	public BigDecimal getUnmanagedBaselineLoad() {
		return unmanagedBaselineLoad;
	}

	public void setUnmanagedBaselineLoad(BigDecimal unmanagedBaselineLoad) {
		this.unmanagedBaselineLoad = unmanagedBaselineLoad;
	}

	public Integer getCommissionStatus() {
		return commissionStatus;
	}

	public void setCommissionStatus(Integer commissionStatus) {
		this.commissionStatus = commissionStatus;
	}

	public Integer getIsHopper() {
		return isHopper;
	}

	public void setIsHopper(Integer isHopper) {
		this.isHopper = isHopper;
	}

	public Integer getVersionSynced() {
		return versionSynced;
	}

	public void setVersionSynced(Integer versionSynced) {
		this.versionSynced = versionSynced;
	}

	public Short getGlobalProfileChecksum() {
		return globalProfileChecksum;
	}

	public void setGlobalProfileChecksum(Short globalProfileChecksum) {
		this.globalProfileChecksum = globalProfileChecksum;
	}

	public Short getCurrApp() {
		return currApp;
	}

	public void setCurrApp(Short currApp) {
		this.currApp = currApp;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getSecGwId() {
		return secGwId;
	}

	public void setSecGwId(Long secGwId) {
		this.secGwId = secGwId;
	}

	public Short getVoltage() {
		return voltage;
	}

	public void setVoltage(Short voltage) {
		this.voltage = voltage;
	}

	public Float getTemperatureOffset() {
		return temperatureOffset;
	}

	public void setTemperatureOffset(Float temperatureOffset) {
		this.temperatureOffset = temperatureOffset;
	}

	public Date getLastBootTime() {
		return lastBootTime;
	}

	public void setLastBootTime(Date lastBootTime) {
		this.lastBootTime = lastBootTime;
	}

	public Date getLastStatsRcvdTime() {
		return lastStatsRcvdTime;
	}

	public void setLastStatsRcvdTime(Date lastStatsRcvdTime) {
		this.lastStatsRcvdTime = lastStatsRcvdTime;
	}

	public String getCuVersion() {
		return cuVersion;
	}

	public void setCuVersion(String cuVersion) {
		this.cuVersion = cuVersion;
	}

	public Integer getCurrentDataId() {
		return currentDataId;
	}

	public void setCurrentDataId(Integer currentDataId) {
		this.currentDataId = currentDataId;
	}

	public Integer getResetReason() {
		return resetReason;
	}

	public void setResetReason(Integer resetReason) {
		this.resetReason = resetReason;
	}

	

	public Boolean getGroupSyncPending() {
		return groupSyncPending;
	}

	public void setGroupSyncPending(Boolean groupSyncPending) {
		this.groupSyncPending = groupSyncPending;
	}

	public Date getCommissionedTime() {
		return commissionedTime;
	}

	public void setCommissionedTime(Date commissionedTime) {
		this.commissionedTime = commissionedTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getGatewayNameForFilter() {
		return gatewayNameForFilter;
	}

	public Long getGatewayIdForFilter() {
		return gatewayIdForFilter;
	}

	public void setGatewayIdForFilter(Long gatewayIdForFilter) {
		if (gateway == null) {
			gateway = new Gateway();
		}
		gateway.setId(gatewayIdForFilter);
		this.gatewayIdForFilter = gatewayIdForFilter;
	}

	public void setGatewayNameForFilter(String gatewayNameForFilter) {
		if (gateway == null) {
			gateway = new Gateway();
		}
		gateway.setGatewayName(gatewayNameForFilter);
		this.gatewayNameForFilter = gatewayNameForFilter;
	}

	public Gateway getGateway() {
		return gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	public boolean isPushGlobalProfile() {
		return pushGlobalProfile;
	}

	public void setPushGlobalProfile(Boolean pushGlobalProfile) {
		this.pushGlobalProfile = pushGlobalProfile;
	}

	

	public Integer getScheduledProfileChecksum() {
		return scheduledProfileChecksum;
	}

	public void setScheduledProfileChecksum(Integer scheduledProfileChecksum) {
		this.scheduledProfileChecksum = scheduledProfileChecksum;
	}

	public Integer getConfigChecksum() {
		return configChecksum;
	}

	public void setConfigChecksum(Integer configChecksum) {
		this.configChecksum = configChecksum;
	}

	public Boolean getGroupsSyncPending() {
		return groupsSyncPending;
	}

	public void setGroupsSyncPending(Boolean groupsSyncPending) {
		this.groupsSyncPending = groupsSyncPending;
	}

	/**
	 * @return the avgTemperature
	 */
	public Float getAvgTemperature() {
		return avgTemperature;
	}

	/**
	 * @param avgTemperature the avgTemperature to set
	 */
	public void setAvgTemperature(Float avgTemperature) {
		this.avgTemperature = avgTemperature;
	}

	/**
	 * @param pushProfile the pushProfile to set
	 */
	public void setPushProfile(boolean pushProfile) {
		this.pushProfile = pushProfile;
	}

	/**
	 * @param pushGlobalProfile the pushGlobalProfile to set
	 */
	public void setPushGlobalProfile(boolean pushGlobalProfile) {
		this.pushGlobalProfile = pushGlobalProfile;
	}

	/**
	 * @return the avgVolts
	 */
	public Float getAvgVolts() {
		return avgVolts;
	}

	/**
	 * @param avgVolts the avgVolts to set
	 */
	public void setAvgVolts(Float avgVolts) {
		this.avgVolts = avgVolts;
	}

	public Integer getLastOccupancySeen() {
		return lastOccupancySeen;
	}

	public void setLastOccupancySeen(Integer lastOccupancySeen) {
		this.lastOccupancySeen = lastOccupancySeen;
	}
	
	

	public Date getLastZBUpdateTime() {
		return lastZBUpdateTime;
	}

	public void setLastZBUpdateTime(Date lastZBUpdateTime) {
		this.lastZBUpdateTime = lastZBUpdateTime;
	}

	public PlugloadEnergyConsumption getLatestPlugloadEnergyConsumption() {
		PlugloadEnergyConsumptionManager plugloadEnergyConsumptionManager = (PlugloadEnergyConsumptionManager) SpringContext
		        .getBean("plugloadEnergyConsumptionManager");
		PlugloadEnergyConsumption latestPlugloadEnergyConsumptions = plugloadEnergyConsumptionManager
		        .loadLatestPlugloadEnergyConsumptionByPlugloadId(this.id);
		this.latestPlugloadEnergyConsumption = latestPlugloadEnergyConsumptions;
		return latestPlugloadEnergyConsumption;
	}

	public Float getPredefinedBaselineLoad() {
		return predefinedBaselineLoad;
	}

	public void setPredefinedBaselineLoad(Float predefinedBaselineLoad) {
		this.predefinedBaselineLoad = predefinedBaselineLoad;
	}


	

}
