package com.emcloudinstance.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="fixtureHealthDataVO")
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureHealthDataVO {
	
	@XmlElement(name ="fixtureId") 
	  Long fixtureId ;
	@XmlElement(name = "fixtureName") 
      String fixtureName ;
	@XmlElement(name = "fixtureMac") 
      String fixtureMac ;
	@XmlElement(name = "fixtureVersion") 
      String fixtureVersion ;
	@XmlElement(name = "lastFixtureConnectivity") 
      String lastFixtureConnectivity ;
	@XmlElement(name = "location") 
      String location ;
	
	public Long getFixtureId() {
		return fixtureId;
	}
	public void setFixtureId(Long fixtureId) {
		this.fixtureId = fixtureId;
	}
	public String getFixtureName() {
		return fixtureName;
	}
	public void setFixtureName(String fixtureName) {
		this.fixtureName = fixtureName;
	}
	public String getFixtureMac() {
		return fixtureMac;
	}
	public void setFixtureMac(String fixtureMac) {
		this.fixtureMac = fixtureMac;
	}
	public String getFixtureVersion() {
		return fixtureVersion;
	}
	public void setFixtureVersion(String fixtureVersion) {
		this.fixtureVersion = fixtureVersion;
	}
	public String getLastFixtureConnectivity() {
		return lastFixtureConnectivity;
	}
	public void setLastFixtureConnectivity(String lastFixtureConnectivity) {
		this.lastFixtureConnectivity = lastFixtureConnectivity;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
