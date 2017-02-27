package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;

@Entity
@Table(name = "em_state", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmState implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private long id;
	private long emInstanceId;
	private EmStatus emStatus;
	private DatabaseState databaseState;
	private int failedAttempts;
	private Date setTime;
	private String log ;
	
	private String utcSetTime;
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_state_seq")
    @SequenceGenerator(name="em_state_seq", sequenceName="em_state_seq", allocationSize=1, initialValue=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	
	/**
	 * @return the emInstanceId
	 */
	@Column(name = "em_instance_id", nullable = false)
	@XmlElement(name = "emInstanceId")
	public long getEmInstanceId() {
		return emInstanceId;
	}
	/**
	 * @param emInstanceId the emInstanceId to set
	 */
	public void setEmInstanceId(long emInstanceId) {
		this.emInstanceId = emInstanceId;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name = "em_status", nullable = false)
	@XmlElement(name = "emStatus")
	public EmStatus getEmStatus() {
		return emStatus;
	}
	public void setEmStatus(EmStatus emStatus) {
		this.emStatus = emStatus;
	}
	@Enumerated(EnumType.STRING)
	@Column(name = "database_status", nullable = false)
	@XmlElement(name = "databaseState")
	public DatabaseState getDatabaseState() {
		return databaseState;
	}
	public void setDatabaseState(DatabaseState databaseState) {
		this.databaseState = databaseState;
	}
	@Column(name = "set_time")
	@XmlElement(name = "setTime")
	public Date getSetTime() {
		return setTime;
	}
	public void setSetTime(Date setTime) {
		this.setTime = setTime;
	}
	@Column(name = "log")
	@XmlElement(name = "log")
	public String getLog() {
		return log;
	}
	public void setLog(String log) {		
		if(log!=null && !log.isEmpty() && log.length()>3000){
			log = StringUtils.reverse(log);
			log = StringUtils.substring(log, 0, 2985);
			log = StringUtils.reverse(log);
			log = "truncated.." + log;
		}
		this.log = log;
	}
	/**
	 * @return the numberOfAttempts
	 */
	@Column(name = "failed_attempts")
	@XmlElement(name = "failedattempts")
	public int getFailedAttempts() {
		return failedAttempts;
	}
	/**
	 * @param numberOfAttempts the numberOfAttempts to set
	 */
	public void setFailedAttempts(int failedAttempts) {
		this.failedAttempts = failedAttempts;
	}
	
	public void setUtcSetTime(String utcSetTime) {
		this.utcSetTime = utcSetTime;
	}
	
	@Transient
	@XmlElement(name = "utcSetTime")
	public String getUtcSetTime() {
		return utcSetTime;
	}

}
