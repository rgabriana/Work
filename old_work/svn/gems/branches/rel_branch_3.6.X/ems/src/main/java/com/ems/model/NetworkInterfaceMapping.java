package com.ems.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "networkInterfaceMapping")
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkInterfaceMapping implements Serializable {
	
	@XmlElement(name = "id")
	Long id;	
	@XmlElement(name = "networkTypes")
	NetworkTypes networkTypes ;
	@XmlElement(name = "interfaces")
	NetworkSettings networkSettings;
	@XmlElement(name = "mappedNetworkInterface")	
	String mappedNetworkInterface;	
	
	
	@XmlElement(name = "nimBACnet")
	NetworkInterfaceMapping nimBACnet;
	
	@XmlElement(name = "nimBuilding")
	NetworkInterfaceMapping nimBuilding;
	
	@XmlElement(name = "nimCorporate")
	NetworkInterfaceMapping nimCorporate;
	
	@XmlElement(name = "networkSettingsId")
	Long networkSettingsId;
	
	@XmlElement(name = "networkTypeId")
	Long networkTypeId;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public NetworkTypes getNetworkTypes() {
		return networkTypes;
	}
	public void setNetworkTypes(NetworkTypes networkTypes) {
		this.networkTypes = networkTypes;
	}
	public NetworkSettings getNetworkSettings() {
		return networkSettings;
	}
	public void setNetworkSettings(NetworkSettings networkSettings) {
		this.networkSettings = networkSettings;
	}
	public String getMappedNetworkInterface() {
		return mappedNetworkInterface;
	}
	public void setMappedNetworkInterface(String mappedNetworkInterface) {
		this.mappedNetworkInterface = mappedNetworkInterface;	}
	
	
	public NetworkInterfaceMapping getNimBACnet() {
		return nimBACnet;
	}
	public void setNimBACnet(NetworkInterfaceMapping nimBACnet) {
		this.nimBACnet = nimBACnet;
	}
	public NetworkInterfaceMapping getNimBuilding() {
		return nimBuilding;
	}
	public void setNimBuilding(NetworkInterfaceMapping nimBuilding) {
		this.nimBuilding = nimBuilding;
	}
	public NetworkInterfaceMapping getNimCorporate() {
		return nimCorporate;
	}
	public void setNimCorporate(NetworkInterfaceMapping nimCorporate) {
		this.nimCorporate = nimCorporate;
	}
	public Long getNetworkSettingsId() {
		return networkSettingsId;
	}
	public void setNetworkSettingsId(Long networkSettingsId) {
		this.networkSettingsId = networkSettingsId;
	}
	public Long getNetworkTypeId() {
		return networkTypeId;
	}
	public void setNetworkTypeId(Long networkTypeId) {
		this.networkTypeId = networkTypeId;
	}	

}
