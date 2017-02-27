package com.ems.model;

/**
 * This is a bean that represents the bacnet configuration. It gets populated when any configuration
 * is done from the UI
 * @author naveen
 *
 */
public class BACnetConfig {
	
	private String vendorId = "516";
	private int serverPort;
	private int networkId;
	private int apduLength = 1476;
	private int apduTimeout = 10;
	private long deviceBaseInstance = 400000;
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
	public long getDeviceBaseInstance() {
		return deviceBaseInstance;
	}
	public void setDeviceBaseInstance(long deviceBaseInstance) {
		this.deviceBaseInstance = deviceBaseInstance;
	}
	
	

}
