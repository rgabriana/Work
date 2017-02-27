package com.adrcom.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="dr_event")
public class DrEvent {
	
	private Long id;
	private String eventId;
	private Long modificationNumber;
	private Long priority;
	private String marketContext;
	private String testEvent;
	private String vtnComment;
	private String eventStatus;
	private Date creationDateTime;
	private Date startDateTime;
	private String eventDuration;
	private String startAfter;
	private String notificationDuration;
	private String rampUpDuration;
	private String recoveryDuration;
	private String optType;
	private String vtnId;
	private String requestId;
	
	private Set<DrEventSignal> signals = new HashSet<DrEventSignal>();
	
	public DrEvent(){
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="adr_target_seq")
    @SequenceGenerator(name="adr_target_seq", sequenceName="adr_target_seq" , allocationSize=1, initialValue=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the eventId
	 */
	@Column(name = "event_id")
	@XmlElement(name = "eventId")
	public String getEventId() {
		return eventId;
	}

	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	/**
	 * @return the modificationNumber
	 */
	@Column(name = "modification_number")
	@XmlElement(name = "modificationNumber")
	public Long getModificationNumber() {
		return modificationNumber;
	}

	/**
	 * @param modificationNumber the modificationNumber to set
	 */
	public void setModificationNumber(Long modificationNumber) {
		this.modificationNumber = modificationNumber;
	}

	/**
	 * @return the priority
	 */
	@Column(name = "priority")
	@XmlElement(name = "priority")
	public Long getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Long priority) {
		this.priority = priority;
	}

	/**
	 * @return the marketContext
	 */
	@Column(name = "market_context")
	@XmlElement(name = "marketContext")
	public String getMarketContext() {
		return marketContext;
	}

	/**
	 * @param marketContext the marketContext to set
	 */
	public void setMarketContext(String marketContext) {
		this.marketContext = marketContext;
	}

	/**
	 * @return the testEvent
	 */
	@Column(name = "test_event")
	@XmlElement(name = "testEvent")
	public String getTestEvent() {
		return testEvent;
	}

	/**
	 * @param testEvent the testEvent to set
	 */
	public void setTestEvent(String testEvent) {
		this.testEvent = testEvent;
	}

	/**
	 * @return the vtnComment
	 */
	@Column(name = "vtn_comment")
	@XmlElement(name = "vtnComment")
	public String getVtnComment() {
		return vtnComment;
	}

	/**
	 * @param vtnComment the vtnComment to set
	 */
	public void setVtnComment(String vtnComment) {
		this.vtnComment = vtnComment;
	}

	/**
	 * @return the eventStatus
	 */
	@Column(name = "event_status")
	@XmlElement(name = "eventStatus")
	public String getEventStatus() {
		return eventStatus;
	}

	/**
	 * @param eventStatus the eventStatus to set
	 */
	public void setEventStatus(String eventStatus) {
		this.eventStatus = eventStatus;
	}

	/**
	 * @return the creationDateTime
	 */
	@Column(name = "creation_date_time")
	@XmlElement(name = "creationDateTime")
	public Date getCreationDateTime() {
		return creationDateTime;
	}

	/**
	 * @param creationDateTime the creationDateTime to set
	 */
	public void setCreationDateTime(Date creationDateTime) {
		this.creationDateTime = creationDateTime;
	}

	/**
	 * @return the startDateTime
	 */
	@Column(name = "start_date_time")
	@XmlElement(name = "startDateTime")
	public Date getStartDateTime() {
		return startDateTime;
	}

	/**
	 * @param startDateTime the startDateTime to set
	 */
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	/**
	 * @return the eventDuration
	 */
	@Column(name = "event_duration")
	@XmlElement(name = "eventDuration")
	public String getEventDuration() {
		return eventDuration;
	}

	/**
	 * @param eventDuration the eventDuration to set
	 */
	public void setEventDuration(String eventDuration) {
		this.eventDuration = eventDuration;
	}

	/**
	 * @return the startAfter
	 */
	@Column(name = "start_after")
	@XmlElement(name = "startAfter")
	public String getStartAfter() {
		return startAfter;
	}

	/**
	 * @param startAfter the startAfter to set
	 */
	public void setStartAfter(String startAfter) {
		this.startAfter = startAfter;
	}

	/**
	 * @return the notificationDuration
	 */
	@Column(name = "notification_duration")
	@XmlElement(name = "notificationDuration")
	public String getNotificationDuration() {
		return notificationDuration;
	}

	/**
	 * @param notificationDuration the notificationDuration to set
	 */
	public void setNotificationDuration(String notificationDuration) {
		this.notificationDuration = notificationDuration;
	}

	/**
	 * @return the rampUpDuration
	 */
	@Column(name = "ramp_up_duration")
	@XmlElement(name = "rampUpDuration")
	public String getRampUpDuration() {
		return rampUpDuration;
	}

	/**
	 * @param rampUpDuration the rampUpDuration to set
	 */
	public void setRampUpDuration(String rampUpDuration) {
		this.rampUpDuration = rampUpDuration;
	}

	/**
	 * @return the recoveryDuration
	 */
	@Column(name = "recovery_duration")
	@XmlElement(name = "recoveryDuration")
	public String getRecoveryDuration() {
		return recoveryDuration;
	}

	/**
	 * @param recoveryDuration the recoveryDuration to set
	 */
	public void setRecoveryDuration(String recoveryDuration) {
		this.recoveryDuration = recoveryDuration;
	}

	/**
	 * @return the optType
	 */
	@Column(name = "opt_type")
	@XmlElement(name = "optType")
	public String getOptType() {
		return optType;
	}

	/**
	 * @param optType the optType to set
	 */
	public void setOptType(String optType) {
		this.optType = optType;
	}

	/**
	 * @return the vtnId
	 */
	@Column(name = "vtn_id")
	@XmlElement(name = "vtnId")
	public String getVtnId() {
		return vtnId;
	}

	/**
	 * @param vtnId the vtnId to set
	 */
	public void setVtnId(String vtnId) {
		this.vtnId = vtnId;
	}

	/**
	 * @return the requestId
	 */
	@Column(name = "request_id")
	@XmlElement(name = "requestId")
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the signals
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "drEvent")
	public Set<DrEventSignal> getSignals() {
		return signals;
	}

	/**
	 * @param signals the signals to set
	 */
	public void setSignals(Set<DrEventSignal> signals) {
		this.signals = signals;
	}
	
}
