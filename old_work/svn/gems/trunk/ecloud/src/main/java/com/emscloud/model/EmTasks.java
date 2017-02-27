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

import com.communication.types.TaskCode;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;

@Entity
@Table(name = "em_tasks", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmTasks implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	private long id;
	private Long emInstanceId;
	private TaskCode taskCode;
	private TaskStatus taskStatus;
	private TaskProgressStatus progressStatus;
	private String parameters;
	private String priority;
	private Date startTime;
	private String utcStartTime;
	private Date offsetTime;
	private Integer numberOfAttempts;
	private Long emTasksUuid; 
	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_tasks_seq")
    @SequenceGenerator(name="em_tasks_seq", sequenceName="em_tasks_seq", allocationSize=1, initialValue=1)
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
	@Enumerated(EnumType.STRING)
	@Column(name = "task_code", nullable = false)
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
	@Enumerated(EnumType.STRING)
	@Column(name = "task_status", nullable = false)
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
	@Enumerated(EnumType.STRING)
	@Column(name = "progress_status")
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
	
	
	/**
	 * @return the parameters
	 */
	@Column(name = "parameters")
	@XmlElement(name = "parameters")
	public String getParameters() {
		return parameters;
	}
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	
	/**
	 * @return the priority
	 */
	@Column(name = "priority")
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
	@Column(name = "start_time")
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
	@Column(name = "offset_time")
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
	@Column(name = "number_of_attempts")
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
	
	/**
	 * @return the emTasksUuid id
	 */
	@Column(name = "em_tasks_uuid_id")
	@XmlElement(name = "emTasksUuidId")
	public Long getEmTasksUuid() {
		return emTasksUuid;
	}
	public void setEmTasksUuid(Long emTasksUuid) {
		this.emTasksUuid = emTasksUuid;
	}
	
	

}
