package com.enlightedinc.adr.model;

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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.OnDelete;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="dr_event_signal_interval")
public class DrEventSignalInterval {
	
	private Long id;
	private DrEventSignal drEventSignal;
	private String intervalDuration;
	private String uid;
	private Float payloadValue;
	
	public DrEventSignalInterval() {
	}
	
	public DrEventSignalInterval(Long id, DrEventSignal drEventSignal, String intervalDuration, String uid, Float payloadValue) {
		this.id = id;
		this.drEventSignal = drEventSignal;
		this.intervalDuration = intervalDuration;
		this.uid = uid;
		this.setPayloadValue(payloadValue);
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="dr_event_signal_interval_seq")
    @SequenceGenerator(name="dr_event_signal_interval_seq", sequenceName="dr_event_signal_interval_seq", allocationSize=1, initialValue=1)
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
	 * @return the drEventSignal
	 */
	@ManyToOne
	@JoinColumn(name = "dr_event_signal_id", nullable = false, insertable = true, updatable = false)
	public DrEventSignal getDrEventSignal() {
		return drEventSignal;
	}

	/**
	 * @param drEventSignal the drEventSignal to set
	 */
	public void setDrEventSignal(DrEventSignal drEventSignal) {
		this.drEventSignal = drEventSignal;
	}

	/**
	 * @return the intervalDuration
	 */
	@Column(name = "interval_duration")
	@XmlElement(name = "intervalDuration")
	public String getIntervalDuration() {
		return intervalDuration;
	}

	/**
	 * @param intervalDuration the intervalDuration to set
	 */
	public void setIntervalDuration(String intervalDuration) {
		this.intervalDuration = intervalDuration;
	}

	/**
	 * @return the uid
	 */
	@Column(name = "uid")
	@XmlElement(name = "uid")
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the payloadValue
	 */
	@Column(name = "payload_value")
	@XmlElement(name = "payloadValue")
	public Float getPayloadValue() {
		return payloadValue;
	}

	/**
	 * @param payloadValue the payloadValue to set
	 */
	public void setPayloadValue(Float payloadValue) {
		this.payloadValue = payloadValue;
	}
	

}
