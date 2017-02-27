package com.emscloud.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "facility_em_mapping", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FacilityEmMapping implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private long id;
	private long customerId;
	private long facilityId;
	private long emId;
	private long emFacilityType;
	private long emFacilityId;
		
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="facility_em_mapping_seq")
    @SequenceGenerator(name="facility_em_mapping_seq", sequenceName="facility_em_mapping_seq", allocationSize=1, initialValue=1)
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
	 * @return the customerId
	 */
	@Column(name = "cust_id", nullable = false)
	@XmlElement(name = "customerId")
	public long getCustomerId() {
		return customerId;
	}
	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
		
	/**
	 * @return the facilityId
	 */
	@Column(name = "facility_id", nullable = false)
	@XmlElement(name = "facilityId")
	public long getFacilityId() {
		return facilityId;
	}
	/**
	 * @param facilityId the facilityId to set
	 */
	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}
	
	/**
	 * @return the emFacilityType
	 */
	@Column(name = "em_facility_type", nullable = false)
	@XmlElement(name = "emFacilityType")
	public long getEmFacilityType() {
		return emFacilityType;
	}
	/**
	 * @param emFacilityType the emFacilityType to set
	 */
	public void setEmFacilityType(long emFacilityType) {
		this.emFacilityType = emFacilityType;
	}
	
	/**
	 * @return the emId
	 */
	@Column(name = "em_id", nullable = false)
	@XmlElement(name = "facilityId")
	public long getEmId() {
		return emId;
	}
	/**
	 * @param emId the emId to set
	 */
	public void setEmId(long emId) {
		this.emId = emId;
	}
	
	/**
	 * @return the emFacilityId
	 */
	@Column(name = "em_facility_id", nullable = false)
	@XmlElement(name = "emFacilityId")
	public long getEmFacilityId() {
		return emFacilityId;
	}
	/**
	 * @param emFacilityId the emFacilityId to set
	 */
	public void setEmFacilityId(long emFacilityId) {
		this.emFacilityId = emFacilityId;
	}
	
}
