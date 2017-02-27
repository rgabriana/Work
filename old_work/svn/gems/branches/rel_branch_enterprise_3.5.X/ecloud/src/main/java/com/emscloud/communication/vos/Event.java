package com.emscloud.communication.vos;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.security.core.userdetails.User;

import com.emscloud.types.EventObjectType;
import com.emscloud.types.EventSeverity;
import com.emscloud.types.EventType;



@Entity
@Table(name = "event", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Event implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7246663588309034222L;
	private Long id;
	private Date eventTime;
	private EventType eventType;
	private String description;
	private EventSeverity severity;
	private Long objectId;
	private EventObjectType objectType;
	private Boolean requiresUserAction;
	private String resolutionComments;
	private User resolvedBy;
	private Date resolvedOn;
	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="event_seq")
    @SequenceGenerator(name="event_seq", sequenceName="event_seq", allocationSize=1, initialValue=1)
    @Column(name = "id")
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
	 * @return the eventTime
	 */
	@Column(name = "event_time")
	@XmlElement(name = "eventTime")
	public Date getEventTime() {
		return eventTime;
	}
	/**
	 * @param eventTime the eventTime to set
	 */
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
	
	
	/**
	 * @return the eventType
	 */
	@Enumerated(EnumType.STRING)
    @Column(name = "event_type", columnDefinition = "char")
    @XmlElement(name = "eventType")
	public EventType getEventType() {
		return eventType;
	}
	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	
	
	/**
	 * @return the description
	 */
	@Column(name = "description")
	@XmlElement(name = "description")
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * @return the severity
	 */
	@Enumerated(EnumType.STRING)
    @Column(name = "severity", columnDefinition = "char")
    @XmlElement(name = "severity")
	public EventSeverity getSeverity() {
		return severity;
	}
	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(EventSeverity severity) {
		this.severity = severity;
	}
	
	
	/**
	 * @return the objectId
	 */
	@Column(name = "object_id")
	@XmlElement(name = "objectId")
	public Long getObjectId() {
		return objectId;
	}
	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	
	
	/**
	 * @return the objectType
	 */
	@Enumerated(EnumType.STRING)
    @Column(name = "object_type", columnDefinition = "char")
    @XmlElement(name = "objectType")
	public EventObjectType getObjectType() {
		return objectType;
	}
	/**
	 * @param objectType the objectType to set
	 */
	public void setObjectType(EventObjectType objectType) {
		this.objectType = objectType;
	}
	
	
	/**
	 * @return the requiresUserAction
	 */
	@Column(name = "requires_user_action")
	@XmlElement(name = "requiresUserAction")
	public Boolean getRequiresUserAction() {
		return requiresUserAction;
	}
	/**
	 * @param requiresUserAction the requiresUserAction to set
	 */
	public void setRequiresUserAction(Boolean requiresUserAction) {
		this.requiresUserAction = requiresUserAction;
	}
	
	
	/**
	 * @return the resolutionComments
	 */
	@Column(name = "resolution_comments")
	@XmlElement(name = "resolutionComments")
	public String getResolutionComments() {
		return resolutionComments;
	}
	/**
	 * @param resolutionComments the resolutionComments to set
	 */
	public void setResolutionComments(String resolutionComments) {
		this.resolutionComments = resolutionComments;
	}
	
	
	/**
	 * @return the resolvedBy
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "resolved_by")
	@XmlElement(name = "resolvedBy")
	public User getResolvedBy() {
		return resolvedBy;
	}
	/**
	 * @param resolvedBy the resolvedBy to set
	 */
	public void setResolvedBy(User resolvedBy) {
		this.resolvedBy = resolvedBy;
	}
	
	
	/**
	 * @return the resolvedOn
	 */
	@Column(name = "resolved_on")
	@XmlElement(name = "resolvedOn")
	public Date getResolvedOn() {
		return resolvedOn;
	}
	/**
	 * @param resolvedOn the resolvedOn to set
	 */
	public void setResolvedOn(Date resolvedOn) {
		this.resolvedOn = resolvedOn;
	}
	
	

}
