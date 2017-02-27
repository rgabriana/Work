package com.ems.model;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="plugloadProfileConfiguration")
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadProfileConfiguration implements Serializable{
	
	   private static final long serialVersionUID = 4378793820912895414L;
	
	@XmlElement(name = "id")
    private Long id;
	
	@XmlElement(name = "weekDays")
    private Set<WeekdayPlugload> weekDays;
	
	public Set<WeekdayPlugload> getWeekDays() {
		return weekDays;
	}
	public void setWeekDays(Set<WeekdayPlugload> weekDays) {
		this.weekDays = weekDays;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMorningTime() {
		return morningTime;
	}
	public void setMorningTime(String morningTime) {
		this.morningTime = morningTime;
	}
	public String getDayTime() {
		return dayTime;
	}
	public void setDayTime(String dayTime) {
		this.dayTime = dayTime;
	}
	public String getEveningTime() {
		return eveningTime;
	}
	public void setEveningTime(String eveningTime) {
		this.eveningTime = eveningTime;
	}
	public String getNightTime() {
		return nightTime;
	}
	public void setNightTime(String nightTime) {
		this.nightTime = nightTime;
	}
	
	@XmlElement(name = "morningTime")
	private String morningTime;
	@XmlElement(name = "dayTime")
	private String dayTime;
	@XmlElement(name = "eveningTime")
	private String eveningTime;
	@XmlElement(name = "nightTime")
	private String nightTime;
	
	public void copyPCTimingsFrom(PlugloadProfileConfiguration target) {
		//System.out.println("day times is "+target.getDayTime());
        this.setDayTime(target.getDayTime());
        this.setEveningTime(target.getEveningTime());
        this.setMorningTime(target.getMorningTime());
        this.setNightTime(target.getNightTime());
    }
}
