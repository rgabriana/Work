package com.adrcom.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name="dr_event_signal")
public class DrEventSignal {
	
	private Long id;
	private DrEvent drEvent;
	private String signalId;
	private String signalName;
	private String signalType;
	private Float currentPayloadValue;
	
	private Set<DrEventSignalInterval> intervals = new HashSet<DrEventSignalInterval>();
	
	public DrEventSignal() {
	}
	
	public DrEventSignal(Long id, DrEvent drEvent, String signalId, 
			String signalName, String signalType, Float currentPayloadValue) {
		
		this.id = id;
		this.drEvent = drEvent;
		this.signalId = signalId;
		this.signalName = signalName;
		this.signalType = signalType;
		this.currentPayloadValue = currentPayloadValue;
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="dr_event_signal_seq")
    @SequenceGenerator(name="dr_event_signal_seq", sequenceName="dr_event_signal_seq", allocationSize=1, initialValue=1)
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
	 * @return the drEvent
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dr_event_id", nullable = false)
	public DrEvent getDrEvent() {
		return drEvent;
	}

	/**
	 * @param drEvent the drEvent to set
	 */
	public void setDrEvent(DrEvent drEvent) {
		this.drEvent = drEvent;
	}

	/**
	 * @return the signalId
	 */
	@Column(name = "signal_id")
	@XmlElement(name = "signalId")
	public String getSignalId() {
		return signalId;
	}

	/**
	 * @param signalId the signalId to set
	 */
	public void setSignalId(String signalId) {
		this.signalId = signalId;
	}

	/**
	 * @return the signalName
	 */
	@Column(name = "signal_name")
	@XmlElement(name = "signalName")
	public String getSignalName() {
		return signalName;
	}

	/**
	 * @param signalName the signalName to set
	 */
	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}

	/**
	 * @return the signalType
	 */
	@Column(name = "signal_type")
	@XmlElement(name = "signalType")
	public String getSignalType() {
		return signalType;
	}

	/**
	 * @param signalType the signalType to set
	 */
	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}

	/**
	 * @return the currentPayloadValue
	 */
	@Column(name = "current_payload_value")
	@XmlElement(name = "currentPayloadValue")
	public Float getCurrentPayloadValue() {
		return currentPayloadValue;
	}

	/**
	 * @param currentPayloadValue the currentPayloadValue to set
	 */
	public void setCurrentPayloadValue(Float currentPayloadValue) {
		this.currentPayloadValue = currentPayloadValue;
	}

	/**
	 * @return the intervals
	 */
	@OneToMany(mappedBy = "drEventSignal", orphanRemoval=true, cascade = {javax.persistence.CascadeType.ALL})
	public Set<DrEventSignalInterval> getIntervals() {
		return intervals;
	}

	/**
	 * @param intervals the intervals to set
	 */
	public void setIntervals(Set<DrEventSignalInterval> intervals) {
		this.intervals = intervals;
	}


}
