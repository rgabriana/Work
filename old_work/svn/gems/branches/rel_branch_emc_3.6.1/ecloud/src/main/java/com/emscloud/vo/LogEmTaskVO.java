package com.emscloud.vo;

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

import com.communication.types.TaskCode;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LogEmTaskVO implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	private long id;
	private Long emInstanceId;
	private TaskCode taskCode;
	private TaskStatus taskStatus;
	private TaskProgressStatus progressStatus;
	private String logNameParameters;
	private String logTypeParameters;
	private String priority;
	private Date startTime;
	private String utcStartTime;
	private Date offsetTime;
	private Integer numberOfAttempts;
	
	
	/**
	 * @return the id
	 */

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
	@XmlElement(name = "emInstanceId")
	public Long getEmInstanceId() {
		return emInstanceId;
	}
	/**
	 * @param emInstanceId the emInstanceId to set
	 */
	public void setEmInstanceId(Long emInstanceId) {
		this.emInstanceId = emInstanceId;
	}
	
	
	/**
	 * @return the taskCode
	 */
	@XmlElement(name = "taskCode")
	public TaskCode getTaskCode() {
		return taskCode;
	}
	/**
	 * @param taskCode the taskCode to set
	 */
	public void setTaskCode(TaskCode taskCode) {
		this.taskCode = taskCode;
	}
	
	
	/**
	 * @return the taskStatus
	 */

	@XmlElement(name = "taskStatus")
	public TaskStatus getTaskStatus() {
		return taskStatus;
	}
	/**
	 * @param taskStatus the taskStatus to set
	 */
	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}
	
	
	/**
	 * @return the progressStatus
	 */

	@XmlElement(name = "progressStatus")
	public TaskProgressStatus getProgressStatus() {
		return progressStatus;
	}
	/**
	 * @param progressStatus the progressStatus to set
	 */
	public void setProgressStatus(TaskProgressStatus progressStatus) {
		this.progressStatus = progressStatus;
	}
	@XmlElement(name = "logNameParameters")
	public String getLogNameParameters() {
		return logNameParameters;
	}
	public void setLogNameParameters(String logNameParameters) {
		this.logNameParameters = logNameParameters;
	}
	@XmlElement(name = "logTypeParameters")
	public String getLogTypeParameters() {
		return logTypeParameters;
	}
	public void setLogTypeParameters(String logTypeParameters) {
		this.logTypeParameters = logTypeParameters;
	}
	
	/**
	 * @return the priority
	 */
	@XmlElement(name = "priority")
	public String getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	
	/**
	 * @return the startTime
	 */
	@XmlElement(name = "startTime")
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	
	/**
	 * @return the offsetTime
	 */
	@XmlElement(name = "offsetTime")
	public Date getOffsetTime() {
		return offsetTime;
	}
	/**
	 * @param offsetTime the offsetTime to set
	 */
	public void setOffsetTime(Date offsetTime) {
		this.offsetTime = offsetTime;
	}
	
	
	/**
	 * @return the numberOfAttempts
	 */
	@XmlElement(name = "numberOfAttempts")
	public Integer getNumberOfAttempts() {
		return numberOfAttempts;
	}
	/**
	 * @param numberOfAttempts the numberOfAttempts to set
	 */
	public void setNumberOfAttempts(Integer numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}
	
	@Transient
	@XmlElement(name = "utcStartTime")
	public String getUtcStartTime() {
		return utcStartTime;
	}
	public void setUtcStartTime(String utcStartTime) {
		this.utcStartTime = utcStartTime;
	}
	
	

}
