package com.ems.model;

/**
 * This is a bean that represents the bacnet configuration. It gets populated when any configuration
 * is done from the UI
 * @author naveen
 *
 */
public class BACnetConfig {
	
	private Boolean enableBacnet;
	private String vendorId;
	private int serverPort;
	private int networkId;
	private int apduLength;
	private int apduTimeout;
	private long energymanagerBaseInstance;
	private long switchgroupBaseInstance;
	private long areaBaseInstance;
	private String restApiKey;
	private String restApiSecret;
	private Boolean detailedMode;
	private String energyManagerName;
	
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public int getNetworkId() {
		return networkId;
	}
	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}
	public int getApduLength() {
		return apduLength;
	}
	public void setApduLength(int apduLength) {
		this.apduLength = apduLength;
	}
	public int getApduTimeout() {
		return apduTimeout;
	}
	public void setApduTimeout(int apduTimeout) {
		this.apduTimeout = apduTimeout;
	}
	public void setRestApiKey(String restApiKey) {
		this.restApiKey = restApiKey;
	}
	public String getRestApiKey() {
		return restApiKey;
	}
	public void setRestApiSecret(String restApiSecret) {
		this.restApiSecret = restApiSecret;
	}
	public String getRestApiSecret() {
		return restApiSecret;
	}
	public void setEnableBacnet(Boolean enableBacnet) {
		this.enableBacnet = enableBacnet;
	}
	public Boolean getEnableBacnet() {
		return enableBacnet;
	}
	public void setEnergymanagerBaseInstance(long energymanagerBaseInstance) {
		this.energymanagerBaseInstance = energymanagerBaseInstance;
	}
	public long getEnergymanagerBaseInstance() {
		return energymanagerBaseInstance;
	}
	public void setSwitchgroupBaseInstance(long switchgroupBaseInstance) {
		this.switchgroupBaseInstance = switchgroupBaseInstance;
	}
	public long getSwitchgroupBaseInstance() {
		return switchgroupBaseInstance;
	}
	public void setAreaBaseInstance(long areaBaseInstance) {
		this.areaBaseInstance = areaBaseInstance;
	}
	public long getAreaBaseInstance() {
		return areaBaseInstance;
	}
	public void setDetailedMode(Boolean detailedMode) {
		this.detailedMode = detailedMode;
	}
	public Boolean getDetailedMode() {
		return detailedMode;
	}
	public void setEnergyManagerName(String energyManagerName) {
		this.energyManagerName = energyManagerName;
	}
	public String getEnergyManagerName() {
		return energyManagerName;
	}
	
	

}