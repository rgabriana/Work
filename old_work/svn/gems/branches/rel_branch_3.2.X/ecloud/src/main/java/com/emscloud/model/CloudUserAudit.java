package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "cloud_users_audit", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CloudUserAudit implements Serializable {
	
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "users")
	private Users users;
	
	@XmlElement(name = "username")
	private String username;
	
	@XmlElement(name = "description")
	private String description;
	
	@XmlElement(name = "username")
	private String actionType;
	
	private Date logTime;
	
	@XmlElement(name = "ipAddress")
	private String ipAddress;
	
	public CloudUserAudit() {
		
	}
	
	public CloudUserAudit(Long id, Long userId, String username, String description, String actionType, Date logTime, String ipAddress) {
		this.id = id;
		this.username = username;
		this.description = description;
		this.actionType = actionType;
		this.logTime = logTime;
		Users users = new Users();
		users.setId(userId);
		this.users = users;
		this.ipAddress = ipAddress;
	}
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="cloud_users_audit_seq")
    @SequenceGenerator(name="cloud_users_audit_seq", sequenceName="cloud_users_audit_seq")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id")
	public Users getUser() {
		return users;
	}
	
	public void setUser(Users users) {
		this.users = users;
	}
	
	@Column(name = "username", nullable = false)
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(name = "description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "action_type", nullable = false)
	public String getActionType() {
		return actionType;
	}
	
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "log_time", length = 30, nullable = false)
	public Date getLogTime() {
		return logTime;
	}
	
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	@Column(name = "ip_address", nullable = false)
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
