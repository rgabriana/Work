package com.emscloud.model.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DetailedProfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -876165539235174234L;
	
	public Long profileId;
	public String profileName;
	public Boolean weekdayMonday;
	public Boolean weekdayTuesday;
	public Boolean weekdayWednesday;
	public Boolean weekdayThursday;
	public Boolean weekdayFriday;
	public Boolean weekdaySaturday;
	public Boolean weekdaySunday;
	public String startMorning;
	public String startDay;
	public String startEvening;
	public String startNight;
	public Integer advancedDimLingerLightLevel;
	public Integer advancedLingerTime;
	
	public Profile weekdayMorning;
	public Profile weekdayDay;
	public Profile weekdayEvening;
	public Profile weekdayNight;
	public Profile weekendMorning;
	public Profile weekendDay;
	public Profile weekendEvening;
	public Profile weekendNight;
	/**
	 * @return the profileId
	 */
	@XmlElement(name = "profileId")
	public Long getProfileId() {
		return profileId;
	}
	/**
	 * @param profileId the profileId to set
	 */
	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}
	/**
	 * @return the profileName
	 */
	@XmlElement(name = "profileName")
	public String getProfileName() {
		return profileName;
	}
	/**
	 * @param profileName the profileName to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	/**
	 * @return the weekdayMonday
	 */
	@XmlElement(name = "weekdayMonday")
	public Boolean getWeekdayMonday() {
		return weekdayMonday;
	}
	/**
	 * @param weekdayMonday the weekdayMonday to set
	 */
	public void setWeekdayMonday(Boolean weekdayMonday) {
		this.weekdayMonday = weekdayMonday;
	}
	/**
	 * @return the weekdayTuesday
	 */
	@XmlElement(name = "weekdayTuesday")
	public Boolean getWeekdayTuesday() {
		return weekdayTuesday;
	}
	/**
	 * @param weekdayTuesday the weekdayTuesday to set
	 */
	public void setWeekdayTuesday(Boolean weekdayTuesday) {
		this.weekdayTuesday = weekdayTuesday;
	}
	/**
	 * @return the weekdayWednesday
	 */
	@XmlElement(name = "weekdayWednesday")
	public Boolean getWeekdayWednesday() {
		return weekdayWednesday;
	}
	/**
	 * @param weekdayWednesday the weekdayWednesday to set
	 */
	public void setWeekdayWednesday(Boolean weekdayWednesday) {
		this.weekdayWednesday = weekdayWednesday;
	}
	/**
	 * @return the weekdayThursday
	 */
	@XmlElement(name = "weekdayThursday")
	public Boolean getWeekdayThursday() {
		return weekdayThursday;
	}
	/**
	 * @param weekdayThursday the weekdayThursday to set
	 */
	public void setWeekdayThursday(Boolean weekdayThursday) {
		this.weekdayThursday = weekdayThursday;
	}
	/**
	 * @return the weekdayFriday
	 */
	@XmlElement(name = "weekdayFriday")
	public Boolean getWeekdayFriday() {
		return weekdayFriday;
	}
	/**
	 * @param weekdayFriday the weekdayFriday to set
	 */
	public void setWeekdayFriday(Boolean weekdayFriday) {
		this.weekdayFriday = weekdayFriday;
	}
	/**
	 * @return the weekdaySaturday
	 */
	@XmlElement(name = "weekdaySaturday")
	public Boolean getWeekdaySaturday() {
		return weekdaySaturday;
	}
	/**
	 * @param weekdaySaturday the weekdaySaturday to set
	 */
	public void setWeekdaySaturday(Boolean weekdaySaturday) {
		this.weekdaySaturday = weekdaySaturday;
	}
	/**
	 * @return the weekdaySunday
	 */
	@XmlElement(name = "weekdaySunday")
	public Boolean getWeekdaySunday() {
		return weekdaySunday;
	}
	/**
	 * @param weekdaySunday the weekdaySunday to set
	 */
	public void setWeekdaySunday(Boolean weekdaySunday) {
		this.weekdaySunday = weekdaySunday;
	}
	/**
	 * @return the startMorning
	 */
	@XmlElement(name = "startMorning")
	public String getStartMorning() {
		return startMorning;
	}
	/**
	 * @param startMorning the startMorning to set
	 */
	public void setStartMorning(String startMorning) {
		this.startMorning = startMorning;
	}
	/**
	 * @return the startDay
	 */
	@XmlElement(name = "startDay")
	public String getStartDay() {
		return startDay;
	}
	/**
	 * @param startDay the startDay to set
	 */
	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}
	/**
	 * @return the startEvening
	 */
	@XmlElement(name = "startEvening")
	public String getStartEvening() {
		return startEvening;
	}
	/**
	 * @param startEvening the startEvening to set
	 */
	public void setStartEvening(String startEvening) {
		this.startEvening = startEvening;
	}
	/**
	 * @return the startNight
	 */
	@XmlElement(name = "startNight")
	public String getStartNight() {
		return startNight;
	}
	/**
	 * @param startNight the startNight to set
	 */
	public void setStartNight(String startNight) {
		this.startNight = startNight;
	}
	/**
	 * @return the advancedDimLingerLightLevel
	 */
	@XmlElement(name = "advancedDimLingerLightLevel")
	public Integer getAdvancedDimLingerLightLevel() {
		return advancedDimLingerLightLevel;
	}
	/**
	 * @param advancedDimLingerLightLevel the advancedDimLingerLightLevel to set
	 */
	public void setAdvancedDimLingerLightLevel(Integer advancedDimLingerLightLevel) {
		this.advancedDimLingerLightLevel = advancedDimLingerLightLevel;
	}
	/**
	 * @return the advancedLingerTime
	 */
	@XmlElement(name = "advancedLingerTime")
	public Integer getAdvancedLingerTime() {
		return advancedLingerTime;
	}
	/**
	 * @param advancedLingerTime the advancedLingerTime to set
	 */
	public void setAdvancedLingerTime(Integer advancedLingerTime) {
		this.advancedLingerTime = advancedLingerTime;
	}
	/**
	 * @return the weekdayMorning
	 */
	@XmlElement(name = "weekdayMorning")
	public Profile getWeekdayMorning() {
		return weekdayMorning;
	}
	/**
	 * @param weekdayMorning the weekdayMorning to set
	 */
	public void setWeekdayMorning(Profile weekdayMorning) {
		this.weekdayMorning = weekdayMorning;
	}
	/**
	 * @return the weekdayDay
	 */
	@XmlElement(name = "weekdayDay")
	public Profile getWeekdayDay() {
		return weekdayDay;
	}
	/**
	 * @param weekdayDay the weekdayDay to set
	 */
	public void setWeekdayDay(Profile weekdayDay) {
		this.weekdayDay = weekdayDay;
	}
	/**
	 * @return the weekdayEvening
	 */
	@XmlElement(name = "weekdayEvening")
	public Profile getWeekdayEvening() {
		return weekdayEvening;
	}
	/**
	 * @param weekdayEvening the weekdayEvening to set
	 */
	public void setWeekdayEvening(Profile weekdayEvening) {
		this.weekdayEvening = weekdayEvening;
	}
	/**
	 * @return the weekdayNight
	 */
	@XmlElement(name = "weekdayNight")
	public Profile getWeekdayNight() {
		return weekdayNight;
	}
	/**
	 * @param weekdayNight the weekdayNight to set
	 */
	public void setWeekdayNight(Profile weekdayNight) {
		this.weekdayNight = weekdayNight;
	}
	/**
	 * @return the weekendMorning
	 */
	@XmlElement(name = "weekendMorning")
	public Profile getWeekendMorning() {
		return weekendMorning;
	}
	/**
	 * @param weekendMorning the weekendMorning to set
	 */
	public void setWeekendMorning(Profile weekendMorning) {
		this.weekendMorning = weekendMorning;
	}
	/**
	 * @return the weekendDay
	 */
	@XmlElement(name = "weekendDay")
	public Profile getWeekendDay() {
		return weekendDay;
	}
	/**
	 * @param weekendDay the weekendDay to set
	 */
	public void setWeekendDay(Profile weekendDay) {
		this.weekendDay = weekendDay;
	}
	/**
	 * @return the weekendEvening
	 */
	@XmlElement(name = "weekendEvening")
	public Profile getWeekendEvening() {
		return weekendEvening;
	}
	/**
	 * @param weekendEvening the weekendEvening to set
	 */
	public void setWeekendEvening(Profile weekendEvening) {
		this.weekendEvening = weekendEvening;
	}
	/**
	 * @return the weekendNight
	 */
	@XmlElement(name = "weekendNight")
	public Profile getWeekendNight() {
		return weekendNight;
	}
	/**
	 * @param weekendNight the weekendNight to set
	 */
	public void setWeekendNight(Profile weekendNight) {
		this.weekendNight = weekendNight;
	}
	

}
