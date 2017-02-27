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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.emscloud.types.FacilityType;



@Entity
@Table(name = "em_facility", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmFacility implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3136831640400822913L;
	private Long id;
	private Long emId;
	private FacilityType type;
	private Long emFacilityId;
	private String emFacilityName;
	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_facility_seq")
    @SequenceGenerator(name="em_facility_seq", sequenceName="em_facility_seq", allocationSize=1, initialValue=1)
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
	 * @return the type
	 */
	@Enumerated(EnumType.STRING)
    @Column(name = "em_facility_type", columnDefinition = "char")
    @XmlElement(name = "type")
	public FacilityType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(FacilityType type) {
		this.type = type;
	}
	
	
	/**
	 * @return the emFacilityId
	 */
	@Column(name = "em_facility_id")
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
	 * @return the emFacilityName
	 */
	@Column(name = "em_facility_name")
	@XmlElement(name = "emFacilityName")
	public String getEmFacilityName() {
		return emFacilityName;
	}
	/**
	 * @param emFacilityName the emFacilityName to set
	 */
	public void setEmFacilityName(String emFacilityName) {
		this.emFacilityName = emFacilityName;
	}
	
	

}
