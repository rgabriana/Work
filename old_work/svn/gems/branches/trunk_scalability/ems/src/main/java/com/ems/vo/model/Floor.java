/**
 * 
 */
package com.ems.vo.model;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sreedhar
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Floor {
  
  @XmlElement(name = "id")
  private BigInteger id;
  @XmlElement(name = "name")
  private String name;
  @XmlElement(name = "building")
  private BigInteger buildingId;
  @XmlElement(name = "campus")
  private BigInteger campusId;
  @XmlElement(name = "company")
  private BigInteger companyId;
  @XmlElement(name = "description")
  private String description;
  @XmlElement(name = "floorPlanUrl")
  private String floorPlanUrl;
  
  /**
   * 
   */
  public Floor() {
  
    // TODO Auto-generated constructor stub
  }

  
  /**
   * @return the id
   */
  public BigInteger getId() {
  
    return id;
  }

  
  /**
   * @param id the id to set
   */
  public void setId(BigInteger id) {
  
    this.id = id;
  }

  
  /**
   * @return the name
   */
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
   * @return the buildingId
   */
  public BigInteger getBuildingId() {
  
    return buildingId;
  }

  
  /**
   * @param buildingId the buildingId to set
   */
  public void setBuildingId(BigInteger buildingId) {
  
    this.buildingId = buildingId;
  }

  
  /**
   * @return the campusId
   */
  public BigInteger getCampusId() {
  
    return campusId;
  }

  
  /**
   * @param campusId the campusId to set
   */
  public void setCampusId(BigInteger campusId) {
  
    this.campusId = campusId;
  }

  
  /**
   * @return the companyId
   */
  public BigInteger getCompanyId() {
  
    return companyId;
  }

  
  /**
   * @param companyId the companyId to set
   */
  public void setCompanyId(BigInteger companyId) {
  
    this.companyId = companyId;
  }

  
  /**
   * @return the description
   */
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
   * @return the floorPlanUrl
   */
  public String getFloorPlanUrl() {
  
    return floorPlanUrl;
  }

  
  /**
   * @param floorPlanUrl the floorPlanUrl to set
   */
  public void setFloorPlanUrl(String floorPlanUrl) {
  
    this.floorPlanUrl = floorPlanUrl;
  }
    
} //end of class Floor
