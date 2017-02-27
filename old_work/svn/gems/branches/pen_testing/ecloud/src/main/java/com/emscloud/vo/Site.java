package com.emscloud.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Site implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	private long id;
	private String name;
	private String geoLocation;
	private String region;
	private Long facilityId;
	private Double squareFoot;
	
	/**
	 * @return the id
	 */

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
	 * @return the name
	 */
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the geoLocation
	 */
	@XmlElement(name = "geoLocation")
	public String getGeoLocation() {
		return geoLocation;
	}
	/**
	 * @param geoLocation the geoLocation to set
	 */
	public void setGeoLocation(String geoLocation) {
		this.geoLocation = geoLocation;
	}
	/**
	 * @return the region
	 */
	@XmlElement(name = "region")
	public String getRegion() {
		return region;
	}
	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}
	/**
	 * @return the facilityId
	 */
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
	 * @return the squareFoot
	 */
	@XmlElement(name = "squareFoot")
	public Double getSquareFoot() {
		return squareFoot;
	}
	/**
	 * @param squareFoot the squareFoot to set
	 */
	public void setSquareFoot(Double squareFoot) {
		this.squareFoot = squareFoot;
	}
	
}
