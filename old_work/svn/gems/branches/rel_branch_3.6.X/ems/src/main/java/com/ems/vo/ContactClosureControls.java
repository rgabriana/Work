package com.ems.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContactClosureControls {

	@XmlElement(name = "name")
	private String name;
	
	@XmlElement(name = "action")
	private int action;
	
	@XmlElement(name = "duration")
	private int duration;
	
	@XmlElement(name = "lastStatus")
	private int lastStatus;
	
	@XmlElement(name = "subAction")
	private String subAction;
	
	@XmlElement(name = "percentage")
	private int percentage;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getAction() {
		return action;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public int getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(int lastStatus) {
		this.lastStatus = lastStatus;
	}

	public void setSubAction(String subAction) {
		this.subAction = subAction;
	}

	public String getSubAction() {
		return subAction;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public int getPercentage() {
		return percentage;
	}

}
