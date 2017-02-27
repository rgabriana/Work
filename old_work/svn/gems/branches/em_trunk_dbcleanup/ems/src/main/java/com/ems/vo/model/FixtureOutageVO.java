package com.ems.vo.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "fixture")
@XmlAccessorType(XmlAccessType.FIELD)
public class FixtureOutageVO {

	@XmlElement(name = "id")
	long fixtureId;
	@XmlElement(name = "name")
	String fixtureName;
	@XmlElement(name = "location")
	String location;
	@XmlElement(name = "outageSince")
	Date outageSince;
	@XmlElement(name = "description")
	String description;

	@XmlElement(name = "xaxis")
	private Integer xposition;
	@XmlElement(name = "yaxis")
	private Integer yposition;

	public FixtureOutageVO() {
		super();
	}

	public FixtureOutageVO(long fixtureId, String fixtureName, String location,
			 int xpos, int ypos, Date outageSince, String description) {
		this.fixtureId = fixtureId;
		this.fixtureName = fixtureName;
		this.location = location;
		this.xposition = xpos;
		this.yposition = ypos;
		this.outageSince = outageSince;
		this.description = description;
	}

	public long getFixtureId() {
		return fixtureId;
	}

	public void setFixtureId(long fixtureId) {
		this.fixtureId = fixtureId;
	}

	public String getFixtureName() {
		return fixtureName;
	}

	public void setFixtureName(String fixtureName) {
		this.fixtureName = fixtureName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getOutageSince() {
		return outageSince;
	}

	public void setOutageSince(Date outageSince) {
		this.outageSince = outageSince;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
