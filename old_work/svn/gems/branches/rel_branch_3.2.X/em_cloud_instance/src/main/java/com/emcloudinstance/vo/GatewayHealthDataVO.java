package com.emcloudinstance.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="gatewayHealthDataVO")
@XmlAccessorType(XmlAccessType.NONE)
public class GatewayHealthDataVO {
	@XmlElement(name = "gatewayId")
	Long gatewayId;
	@XmlElement(name = "gatewayName")
	String gatewayName;
	@XmlElement(name = "gatewayMac")
	String gatewayMac;
	@XmlElement(name = "gatewayVersion")
	String gatewayVersion;
	@XmlElement(name = "noOfSensor")
	Long noOfSensor;
	@XmlElement(name = "lastGatewayConnectivity")
	String lastGatewayConnectivity;


	public Long getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(Long gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public String getGatewayMac() {
		return gatewayMac;
	}

	public void setGatewayMac(String gatewayMac) {
		this.gatewayMac = gatewayMac;
	}

	public String getGatewayVersion() {
		return gatewayVersion;
	}

	public void setGatewayVersion(String gatewayVersion) {
		this.gatewayVersion = gatewayVersion;
	}

	public Long getNoOfSensor() {
		return noOfSensor;
	}

	public void setNoOfSensor(Long noOfSensor) {
		this.noOfSensor = noOfSensor;
	}

	public String getLastGatewayConnectivity() {
		return lastGatewayConnectivity;
	}

	public void setLastGatewayConnectivity(String lastGatewayConnectivity) {
		this.lastGatewayConnectivity = lastGatewayConnectivity;
	}



}