package com.emcloudinstance.vo;

public class FixtureDiagnosticsVO {	
	Long fixtureId;
	Long fixtureDiagnosticReferenceId;
	Long fixtureDiagnosticsId;
	Integer hourOfDay;
	Double powerUsedAverage;
	Double powerUsedVariance;
	public Long getFixtureId() {
		return fixtureId;
	}
	public void setFixtureId(Long fixtureId) {
		this.fixtureId = fixtureId;
	}
	public Long getFixtureDiagnosticReferenceId() {
		return fixtureDiagnosticReferenceId;
	}
	public void setFixtureDiagnosticReferenceId(Long fixtureDiagnosticReferenceId) {
		this.fixtureDiagnosticReferenceId = fixtureDiagnosticReferenceId;
	}
	public Long getFixtureDiagnosticsId() {
		return fixtureDiagnosticsId;
	}
	public void setFixtureDiagnosticsId(Long fixtureDiagnosticsId) {
		this.fixtureDiagnosticsId = fixtureDiagnosticsId;
	}
	public Integer getHourOfDay() {
		return hourOfDay;
	}
	public void setHourOfDay(Integer hourOfDay) {
		this.hourOfDay = hourOfDay;
	}
	public Double getPowerUsedAverage() {
		return powerUsedAverage;
	}
	public void setPowerUsedAverage(Double powerUsedAverage) {
		this.powerUsedAverage = powerUsedAverage;
	}
	public Double getPowerUsedVariance() {
		return powerUsedVariance;
	}
	public void setPowerUsedVariance(Double powerUsedVariance) {
		this.powerUsedVariance = powerUsedVariance;
	}
	
	
	
	
	

}
