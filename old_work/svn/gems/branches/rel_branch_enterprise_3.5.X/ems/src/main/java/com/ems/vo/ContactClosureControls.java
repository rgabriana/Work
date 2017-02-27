package com.ems.vo;

public class ContactClosureControls {

	private String name;
	
	private int action;
	
	private int duration;
	
	private int lastStatus;
	
	private String subAction;

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

}
