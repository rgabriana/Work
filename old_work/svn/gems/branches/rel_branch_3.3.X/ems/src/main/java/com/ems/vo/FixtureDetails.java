package com.ems.vo;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Fixture;
import com.ems.model.FixtureClass;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureDetails implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1092664413786174432L;
	
	@XmlElement(name = "fixture")
	private Fixture fixture;
	
	@XmlElement(name = "temperatureunit")
	private String temperatureunit;
	
	@XmlElement(name = "fixtureclasses")
	private List<FixtureClass> fixtureclasses;
	
	@XmlElement(name = "originalProfileFromId")
	private Long originalProfileFromId;
	
	@XmlElement(name = "currentProfileId")
	private Long currentProfileId;
	
	@XmlElement(name = "groupList")
	private  List<String> groupList;
	
	@XmlElement(name = "characterizationStatus")
	private String characterizationStatus;
	
	@XmlElement(name = "fixtureStatus")
	private String fixtureStatus;

	public Fixture getFixture() {
		return fixture;
	}

	public void setFixture(Fixture fixture) {
		this.fixture = fixture;
	}

	public String getTemperatureunit() {
		return temperatureunit;
	}

	public void setTemperatureunit(String temperatureunit) {
		this.temperatureunit = temperatureunit;
	}

	public List<FixtureClass> getFixtureclasses() {
		return fixtureclasses;
	}

	public void setFixtureclasses(List<FixtureClass> fixtureclasses) {
		this.fixtureclasses = fixtureclasses;
	}

	public String getCharacterizationStatus() {
		return characterizationStatus;
	}

	public void setCharacterizationStatus(String characterizationStatus) {
		this.characterizationStatus = characterizationStatus;
	}

	public String getFixtureStatus() {
		return fixtureStatus;
	}
	
	public Long getOriginalProfileFromId() {
		return originalProfileFromId;
	}

	public void setOriginalProfileFromId(Long originalProfileFromId) {
		this.originalProfileFromId = originalProfileFromId;
	}

	public Long getCurrentProfileId() {
		return currentProfileId;
	}

	public void setCurrentProfileId(Long currentProfileId) {
		this.currentProfileId = currentProfileId;
	}

	public void setFixtureStatus(String fixtureStatus) {
		this.fixtureStatus = fixtureStatus;
	}

	public void setGroupList(List<String> groupList) {
		this.groupList = groupList;
	}

	public List<String> getGroupList() {
		return groupList;
	}

}
