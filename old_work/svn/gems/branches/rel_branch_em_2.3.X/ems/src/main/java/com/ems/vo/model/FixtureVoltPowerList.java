package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.ems.model.Fixture;
import com.ems.model.FixtureCalibrationMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureVoltPowerList {
		
	@XmlElement(name = "fixtureCalibrationMaps")
	private List<FixtureCalibrationMap> fixtureCalibrationMap;
	@XmlElement(name = "fixtures")
	private List<Fixture> fixture;
	public List<FixtureCalibrationMap> getFixtureCalibrationMap() {
		return fixtureCalibrationMap;
	}
	public void setFixtureCalibrationMap(List<FixtureCalibrationMap> fixtureCalibrationMap) {
		this.fixtureCalibrationMap = fixtureCalibrationMap;
	}
	public List<Fixture> getFixture() {
		return fixture;
	}
	public void setFixture(List<Fixture> fixture) {
		this.fixture = fixture;
	}
}
