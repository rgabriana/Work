package com.emscloud.model;

import java.io.Serializable;

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

import com.emscloud.types.FacilityType;



@Entity
@Table(name = "facility_em_mapping", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FacilityEmMapping implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private Long id;
	private Long facilityId;
	private Long emId;
	private Long emFacilityType;
	private FacilityType facilityType;
	private Long emFacilityId;
	private String emFacilityPath;
	private Long custId;
	
	@Transient
	private String cloudFacilityNodePath;
	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="facility_em_mapping_seq")
    @SequenceGenerator(name="facility_em_mapping_seq", sequenceName="facility_em_mapping_seq", allocationSize=1, initialValue=1)
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
	 * @return the facilityId
	 */
	@Column(name = "facility_id", nullable = false)
	@XmlElement(name = "facilityId")
	public Long getFacilityId() {
		return facilityId;
	}
	/**
	 * @param facilityId the facilityId to set
	 */
	public void setFacilityId(Long facilityId) {
		this.facilityId = facilityId;
	}
	
	/**
	 * @return the emId
	 */
	@Column(name = "em_id", nullable = false)
	@XmlElement(name = "emId")
	public Long getEmId() {
		return emId;
	}
	/**
	 * @param emId the emId to set
	 */
	public void setEmId(Long emId) {
		this.emId = emId;
	}
	
	/**
	 * @return the emFacilityId
	 */
	@Column(name = "em_facility_id", nullable = false)
	@XmlElement(name = "emFacilityId")
	public Long getEmFacilityId() {
		return emFacilityId;
	}
	/**
	 * @param emFacilityId the emFacilityId to set
	 */
	public void setEmFacilityId(Long emFacilityId) {
		this.emFacilityId = emFacilityId;
	}
	
	
	/**
	 * @return the facilityType
	 */
    @Enumerated(EnumType.STRING)
    @Transient
    @XmlElement(name = "facilityType")
	public FacilityType getFacilityType() {
		return facilityType;
	}
	/**
	 * @param facilityType the facilityType to set
	 */
	public void setFacilityType(FacilityType facilityType) {
		this.facilityType = facilityType;
	}
		
	/**
	 * @return the emFacilityType
	 */
	@Column(name = "em_facility_type", nullable = false)
	@XmlElement(name = "emFacilityType")
	public Long getEmFacilityType() {
		return emFacilityType;
	}
	/**
	 * @param emFacilityType the emFacilityType to set
	 */
	public void setEmFacilityType(Long emFacilityType) {
		this.emFacilityType = emFacilityType;
		this.facilityType = FacilityType.getFacilityType(emFacilityType.intValue());
	}
	
	
	public void setEmFacilityPath(String emFacilityPath) {
		this.emFacilityPath = emFacilityPath;
	}
	
	/**
	 * @return the emFacilityPath
	 */
	@Column(name = "em_facility_path")
	@XmlElement(name = "emFacilityPath")
	public String getEmFacilityPath() {
		return emFacilityPath;
	}
	
	public void setCustId(Long custId) {
		this.custId = custId;
	}
	
	@Column(name = "cust_id", nullable = false)
	@XmlElement(name = "custId")
	public Long getCustId() {
		return custId;
	}
	
	public void setCloudFacilityNodePath(String cloudFacilityNodePath) {
		this.cloudFacilityNodePath = cloudFacilityNodePath;
	}
	
	@Transient
	@XmlElement(name = "cloudFacilityNodePath")
	public String getCloudFacilityNodePath() {
		return cloudFacilityNodePath;
	}
	
			
}
