package com.emcloudinstance.vo;

import java.util.Date;

public class EnergyConsumptionHourlyVO {
	Long id;
	Long fixtureId;
	Date captureAt;
	Double powerUsed;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCaptureAt() {
		return captureAt;
	}
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	public Double getPowerUsed() {
		return powerUsed;
	}
	public void setPowerUsed(Double powerUsed) {
		this.powerUsed = powerUsed;
	}
	public Long getFixtureId() {
		return fixtureId;
	}
	public void setFixtureId(Long fixtureId) {
		this.fixtureId = fixtureId;
	}
	
	

}
