package com.ems.vo.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "FixtureLampStatusVO")
@XmlAccessorType(XmlAccessType.FIELD)
public class FixtureLampStatusVO {

	@XmlElement(name = "id")
	long fixtureId;
	@XmlElement(name = "name")
	String fixtureName;
	@XmlElement(name = "outageSince")
	Date eventTime;
	@XmlElement(name = "description")
	String description;
	/**
	 * 1 - Fx Curve
	 * 2 - Ballast Curve
	 */
	@XmlElement(name = "curveType")
	Long curveType;
	@XmlElement(name = "xaxis")
	private Integer xposition;
	@XmlElement(name = "yaxis")
	private Integer yposition;
	@XmlElement(name = "location")
	String location;
	@XmlElement(name = "displayLabel")
	String displayLabel;
	
	String fixtureStatus;
	
	public static final String UNCHARACTERISED = "Uncharacterized";
    public static final String GENERIC_FROM_BALLAST = "Generic from ballast";
    public static final String INDIVIDUAL = "Individual";
    
    public static final String FIXTURE_OUT = "Fixture out";
    public static final String LAMP_OUT = "Lamp out";
    public static final String OPERATIONAL = "Operational";
    
	public FixtureLampStatusVO() {
		super();
	}

	public FixtureLampStatusVO(long fixtureId, String fixtureName, Date eventTime,String description) {
		this.fixtureId = fixtureId;
		this.fixtureName = fixtureName;
		this.eventTime = eventTime;
		this.description = description;
	}

	/**
	 * @return the fixtureId
	 */
	public long getFixtureId() {
		return fixtureId;
	}

	/**
	 * @param fixtureId the fixtureId to set
	 */
	public void setFixtureId(long fixtureId) {
		this.fixtureId = fixtureId;
	}

	/**
	 * @return the fixtureName
	 */
	public String getFixtureName() {
		return fixtureName;
	}

	/**
	 * @param fixtureName the fixtureName to set
	 */
	public void setFixtureName(String fixtureName) {
		this.fixtureName = fixtureName;
	}

	/**
	 * @return the eventTime
	 */
	public Date getEventTime() {
		return eventTime;
	}

	/**
	 * @param eventTime the eventTime to set
	 */
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	/**
	 * @return the curveType
	 */
	public Long getCurveType() {
		return curveType;
	}

	/**
	 * @param curveType the curveType to set
	 */
	public void setCurveType(Long curveType) {
		this.curveType = curveType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getXposition() {
		return xposition;
	}

	public void setXposition(Integer xposition) {
		this.xposition = xposition;
	}

	/**
	 * @return the yposition
	 */
	public Integer getYposition() {
		return yposition;
	}

	/**
	 * @param yposition the yposition to set
	 */
	public void setYposition(Integer yposition) {
		this.yposition = yposition;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the displayLabel
	 */
	public String getDisplayLabel() {
		return displayLabel;
	}

	/**
	 * @param displayLabel the displayLabel to set
	 */
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	/**
	 * @return the fixtureStatus
	 */
	public String getFixtureStatus() {
		return fixtureStatus;
	}

	/**
	 * @param fixtureStatus the fixtureStatus to set
	 */
	public void setFixtureStatus(String fixtureStatus) {
		this.fixtureStatus = fixtureStatus;
	}

}
