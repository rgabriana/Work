package com.emscloud.vo;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmHealthDataVO {
	
	@XmlElement(name = "emInstanceId")
	private Long emInstanceId;
	
	@XmlElement(name = "customerName")
	private String customerName;
	
	@XmlElement(name = "emInstanceName")
	private String emInstanceName;
	
	@XmlElement(name = "lastEmConnectivity")
	private String lastEmConnectivity;
	
	@XmlElement(name = "emConnectivityInMinutes")
	private String emConnectivityInMinutes;
	
	@XmlElement(name = "lastDataSynchConnectivity")
	private String lastDataSynchConnectivity;
	
	@XmlElement(name = "lastDataSynchConnectivityInMinutes")
	private String lastDataSynchConnectivityInMinutes;
	
	@XmlElement(name = "gatewaysCriticalNo")
	private Integer gatewaysCriticalNo;
	
	@XmlElement(name = "gatewaysUnderObservationNo")
	private Integer gatewaysUnderObservationNo;
	
	@XmlElement(name = "gatewaysTotal")
	private Integer gatewaysTotal;
	
	@XmlElement(name = "sensorsCriticalNo")
	private Integer sensorsCriticalNo;
	
	@XmlElement(name = "sensorsUnderObservationNo")
	private Integer sensorsUnderObservationNo;
	
	@XmlElement(name = "sensorsTotal")
	private Integer sensorsTotal;
	
	
	public Long getEmInstanceId() {
		return emInstanceId;
	}
	public void setEmInstanceId(Long emInstanceId) {
		this.emInstanceId = emInstanceId;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getEmInstanceName() {
		return emInstanceName;
	}
	public void setEmInstanceName(String emInstanceName) {
		this.emInstanceName = emInstanceName;
	}
	public String getLastEmConnectivity() {
		return lastEmConnectivity;
	}
	public void setLastEmConnectivity(String lastEmConnectivity) {
		this.lastEmConnectivity = lastEmConnectivity;
	}
	public String getLastDataSynchConnectivity() {
		return lastDataSynchConnectivity;
	}
	public void setLastDataSynchConnectivity(String lastDataSynchConnectivity) {
		this.lastDataSynchConnectivity = lastDataSynchConnectivity;
	}
	public Integer getGatewaysCriticalNo() {
		return gatewaysCriticalNo;
	}
	public void setGatewaysCriticalNo(Integer gatewaysCriticalNo) {
		this.gatewaysCriticalNo = gatewaysCriticalNo;
	}
	public Integer getGatewaysUnderObservationNo() {
		return gatewaysUnderObservationNo;
	}
	public void setGatewaysUnderObservationNo(Integer gatewaysUnderObservationNo) {
		this.gatewaysUnderObservationNo = gatewaysUnderObservationNo;
	}
	public Integer getGatewaysTotal() {
		return gatewaysTotal;
	}
	public void setGatewaysTotal(Integer gatewaysTotal) {
		this.gatewaysTotal = gatewaysTotal;
	}
	public Integer getSensorsCriticalNo() {
		return sensorsCriticalNo;
	}
	public void setSensorsCriticalNo(Integer sensorsCriticalNo) {
		this.sensorsCriticalNo = sensorsCriticalNo;
	}
	public Integer getSensorsUnderObservationNo() {
		return sensorsUnderObservationNo;
	}
	public void setSensorsUnderObservationNo(Integer sensorsUnderObservationNo) {
		this.sensorsUnderObservationNo = sensorsUnderObservationNo;
	}
	public Integer getSensorsTotal() {
		return sensorsTotal;
	}
	public void setSensorsTotal(Integer sensorsTotal) {
		this.sensorsTotal = sensorsTotal;
	}
	public String getEmConnectivityInMinutes() {
		return emConnectivityInMinutes;
	}
	public void setEmConnectivityInMinutes(String emConnectivityInMinutes) {
		this.emConnectivityInMinutes = emConnectivityInMinutes;
	}
	public String getLastDataSynchConnectivityInMinutes() {
		return lastDataSynchConnectivityInMinutes;
	}
	public void setLastDataSynchConnectivityInMinutes(
			String lastDataSynchConnectivityInMinutes) {
		this.lastDataSynchConnectivityInMinutes = lastDataSynchConnectivityInMinutes;
	}
	

	
}
