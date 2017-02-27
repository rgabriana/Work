package com.emscloud.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "em_health_monitor", schema = "public")
public class EmHealthMonitor implements java.io.Serializable{
	Long id;
	EmInstance emInstance;
	Date captureAt;
	Integer gatewaysCritical;
	Integer gatewaysUnderObservation;
	Integer gatewaysTotal;
	Integer sensorsCritical;
	Integer sensorsUnderObservation;
	Integer sensorsTotal;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_health_monitor_seq")
    @SequenceGenerator(name="em_health_monitor_seq", sequenceName="em_health_monitor_seq")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "em_instance_id")
	public EmInstance getEmInstance() {
		return emInstance;
	}
	public void setEmInstance(EmInstance emInstance) {
		this.emInstance = emInstance;
	}
	
	@Column(name = "capture_at")
	public Date getCaptureAt() {
		return captureAt;
	}
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	public Integer getGatewaysCritical() {
		return gatewaysCritical;
	}
	public void setGatewaysCritical(Integer gatewaysCritical) {
		this.gatewaysCritical = gatewaysCritical;
	}
	public Integer getGatewaysUnderObservation() {
		return gatewaysUnderObservation;
	}
	public void setGatewaysUnderObservation(Integer gatewaysUnderObservation) {
		this.gatewaysUnderObservation = gatewaysUnderObservation;
	}
	public Integer getGatewaysTotal() {
		return gatewaysTotal;
	}
	public void setGatewaysTotal(Integer gatewaysTotal) {
		this.gatewaysTotal = gatewaysTotal;
	}
	public Integer getSensorsCritical() {
		return sensorsCritical;
	}
	public void setSensorsCritical(Integer sensorsCritical) {
		this.sensorsCritical = sensorsCritical;
	}
	public Integer getSensorsUnderObservation() {
		return sensorsUnderObservation;
	}
	public void setSensorsUnderObservation(Integer sensorsUnderObservation) {
		this.sensorsUnderObservation = sensorsUnderObservation;
	}
	public Integer getSensorsTotal() {
		return sensorsTotal;
	}
	public void setSensorsTotal(Integer sensorsTotal) {
		this.sensorsTotal = sensorsTotal;
	}
	
	

}
