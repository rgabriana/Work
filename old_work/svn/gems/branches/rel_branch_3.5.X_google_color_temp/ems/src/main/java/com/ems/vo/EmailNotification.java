package com.ems.vo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EmailNotification {
	
	private Boolean enabled;
	
	private String eventTypeList;
	
	private String severityList;
	
	private String emailList;
	
	private String time;
	
	private String weeklyRecurrence;
	
	private Boolean enableOneHourNotification;
		
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEventTypeList(String eventTypeList) {
		this.eventTypeList = eventTypeList;
	}

	public String getEventTypeList() {
		return eventTypeList;
	}

	public void setSeverityList(String severityList) {
		this.severityList = severityList;
	}

	public String getSeverityList() {
		return severityList;
	}

	public void setEmailList(String emailList) {
		this.emailList = emailList;
	}

	public String getEmailList() {
		return emailList;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return time;
	}

	public void setWeeklyRecurrence(String weeklyRecurrence) {
		this.weeklyRecurrence = weeklyRecurrence;
	}

	public String getWeeklyRecurrence() {
		return weeklyRecurrence;
	}

	public void setEnableOneHourNotification(Boolean enableOneHourNotification) {
		this.enableOneHourNotification = enableOneHourNotification;
	}

	public Boolean getEnableOneHourNotification() {
		return enableOneHourNotification;
	}

}
