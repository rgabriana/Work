package com.emscloud.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.emscloud.types.FacilityType;

@Entity
@Table(name = "facility", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonSerialize(
		include=JsonSerialize.Inclusion.NON_NULL
)
public class Facility implements Serializable,Comparable<Facility>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private Long id;
	private String name;
	private Long customerId;
	private FacilityType level;
	private Long planMapId;
	private Long parentId;
	private Float locX;
	private Float locY;
	private Double squareFoot;
	private Integer type;
	//private String level;
		
	private Set<Facility> childFacilities = new HashSet<Facility>();

	private static HashMap<Integer, String> facilityTypeMap = new HashMap<Integer, String>();	
	static {
		
		facilityTypeMap.put(1, "ORGANIZATION");
		facilityTypeMap.put(2, "CAMPUS");
		facilityTypeMap.put(3, "BUILDING");
		facilityTypeMap.put(4, "FLOOR");
		facilityTypeMap.put(5, "REGION");
		facilityTypeMap.put(6, "ZONE");
		facilityTypeMap.put(7, "AREA");
		facilityTypeMap.put(8, "ROOM");
		facilityTypeMap.put(9, "SITE");
		
	}
		
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="facility_seq")
    @SequenceGenerator(name="facility_seq", sequenceName="facility_seq", allocationSize=1, initialValue=1)
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
	
	
	@Column(name = "name", nullable = false)
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
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
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;		
		this.level = FacilityType.getFacilityType(type.intValue());
	}
	
//	@Transient
//	@XmlElement(name = "level")
//	public String getLevel() {
//		return level;
//	}
//	public void setLevel(String level) {
//		this.level = level;
//	}
	/**
	 * @return the customerId
	 */
	@Column(name = "customer_id")
	@XmlElement(name = "customerId")
	public Long getCustomerId() {
		return customerId;
	}
	
	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	
	
	/**
	 * @return the planMapId
	 */
	@Column(name = "plan_map_id")
	public Long getPlanMapId() {
		return planMapId;
	}
	
	/**
	 * @param planMapId the planMapId to set
	 */
	public void setPlanMapId(Long planMapId) {
		this.planMapId = planMapId;
	}
	
	
	/**
	 * @return the parentId
	 */
	@Column(name = "parent_id")
	@XmlElement(name = "parentId")
	public Long getParentId() {
		return parentId;
	}
	
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(Long parentId) {
		this.parentId = parentId;
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
	
	/**
	 * @return the level
	 */
    @Enumerated(EnumType.STRING)
    @Transient
    @XmlElement(name = "level")
	public FacilityType getLevel() {
		return level;		
	}
	
	/**
	 * @param level the level to set
	 */
	public void setLevel(FacilityType level) {
		this.level = level;
	}
	/**
	 * @return the locX
	 */
	@Column(name = "locX")
	@XmlElement(name = "locX")
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
	@XmlElement(name = "locY")
	public Float getLocY() {
		return locY;
	}
	/**
	 * @param locY the locY to set
	 */
	public void setLocY(Float locY) {
		this.locY = locY;
	}
	
	@Override
	public int compareTo(Facility arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static Comparator<Facility> FacilityNameComparator 
	    = new Comparator<Facility>() {
	
		public int compare(Facility facility1, Facility facility2) {
		
		String facilityName1 = facility1.getName().toUpperCase();
		String facilityName2 = facility2.getName().toUpperCase();
		
		//ascending order
		return facilityName1.compareTo(facilityName2);
		
		//descending order
		//return facilityName2.compareTo(facilityName1);
		}
	
	};
			
}
