package com.emscloud.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.persistence.CascadeType;

@Entity
@Table(name = "facility", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Facility implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private long id;
	//private long customerId;
	private String name;
	//private String hierarchy;
	private Float locX;
	private Float locY;
	private Double squareFoot;
			
	//private Facility parentFacility;
		
  private Set<Facility> childFacilities = new HashSet<Facility>();

	private long type;
	private String level;
	
	private static HashMap<Long, String> facilityTypeMap = new HashMap<Long, String>();
	
	static {
		
		facilityTypeMap.put(1L, "Organization");
		facilityTypeMap.put(2L, "Campus");
		facilityTypeMap.put(3L, "Building");
		facilityTypeMap.put(4L, "Floor");
		facilityTypeMap.put(5L, "Region");
		facilityTypeMap.put(6L, "Zone");
		facilityTypeMap.put(7L, "Area");
		facilityTypeMap.put(8L, "Room");
		facilityTypeMap.put(9L, "Site");
		
	}
	
		
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="facility_seq")
    @SequenceGenerator(name="facility_seq", sequenceName="facility_seq", allocationSize=1, initialValue=1)
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
//	@Column(name = "customer_id", nullable = false)
//	@XmlElement(name = "customerId")
//	public long getCustomerId() {
//		return customerId;
//	}
//	/**
//	 * @param emInstanceId the emInstanceId to set
//	 */
//	public void setCustomerId(long customerId) {
//		this.customerId = customerId;
//	}
		
	@Column(name = "name", nullable = false)
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
/*
	@ManyToOne(cascade={CascadeType.ALL}, fetch=FetchType.LAZY )
	@JoinColumn(name = "parent_id")	
	@XmlElement(name = "parentFacility")
	public Facility getParentFacility() {
		return parentFacility;
	}
	public void setParentFacility(Facility parentFacility) {
		this.parentFacility = parentFacility;
	}
	*/
	/**
	 * @return the childFacilities
	 */	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name = "parent_id")
	@XmlElement(name= "childFacilities")
	public Set<Facility> getChildFacilities() {
		return childFacilities;
	}
	/**
	 * @param childFacilities the childFacilities to set
	 */
	public void setChildFacilities(Set<Facility> childFacilities) {
		this.childFacilities = childFacilities;
	}
	
	@Column(name = "type")	
	public long getType() {
		return type;
	}
	public void setType(long type) {
		this.type = type;
		this.level = facilityTypeMap.get(type);
	}
	
	@Transient
	@XmlElement(name = "level")
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	/**
	 * @return the locX
	 */
	@Column(name = "locX")
	public Float getLocX() {
		return locX;
	}
	/**
	 * @param locX the locX to set
	 */
	public void setLocX(Float locX) {
		this.locX = locX;
	}
	/**
	 * @return the locY
	 */
	@Column(name = "locY")
	public Float getLocY() {
		return locY;
	}
	/**
	 * @param locY the locY to set
	 */	
	public void setLocY(Float locY) {
		this.locY = locY;
	}
	/**
	 * @return the squareFoot
	 */
	@Column(name = "square_foot")
	public Double getSquareFoot() {
		return squareFoot;
	}
	/**
	 * @param squareFoot the squareFoot to set
	 */
	public void setSquareFoot(Double squareFoot) {
		this.squareFoot = squareFoot;
	}
	
	/*
	@Column(name = "hierarchy")	
	@XmlElement(name = "hierarchy")
	public String getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}
	*/
	
}
