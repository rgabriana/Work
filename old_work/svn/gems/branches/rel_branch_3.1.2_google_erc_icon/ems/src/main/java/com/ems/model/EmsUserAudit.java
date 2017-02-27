package com.ems.model;

import java.io.Serializable;
import java.util.Date;
public class EmsUserAudit implements Serializable {
	
	private static final long serialVersionUID = 8153690457372151431L;
	private Long id;
	private User user;
	private String username;
	private String description;
	private String actionType;
	private Date logTime;
	private String ipAddress;
	
	public EmsUserAudit() {
		
	}
	
	public EmsUserAudit(Long id, Long userId, String username, String description, String actionType, Date logTime, String ipAddress) {
		this.id = id;
		this.username = username;
		this.description = description;
		this.actionType = actionType;
		this.logTime = logTime;
		User user = new User();
		user.setId(userId);
		this.user = user;
		this.ipAddress = ipAddress;
	}
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public Date getLogTime() {
		return logTime;
	}
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
