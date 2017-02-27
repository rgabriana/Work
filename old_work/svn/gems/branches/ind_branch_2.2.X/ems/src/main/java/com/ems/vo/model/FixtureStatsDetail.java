/**
 * 
 */
package com.ems.vo.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

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
public class FixtureStatsDetail {
	
  @XmlElement(name = "id")
  private BigInteger id;  
  @XmlElement(name = "power")
  private BigDecimal power;
  @XmlElement(name = "occupancy")
  private BigInteger occupancy;
  @XmlElement(name = "temperature")
  private Short temperature;
  @XmlElement(name = "statsTime")
  private Date statsTime;
  @XmlElement(name = "avgVolts")
  private Short avgVolts;
  @XmlElement(name = "avgAmbient")
  private Short avgAmbient;
	
  /**
   * 
   */
  public FixtureStatsDetail() {
    
    // TODO Auto-generated constructor stub
  }
	
  /**
   * 
   */
  public FixtureStatsDetail(BigInteger id, BigDecimal power, BigInteger occupancy, 
      Short temp, Date statsTime, Short avgVolts, Short avgAmbient) {
	  
    this.id = id;    
    this.power = power;
    this.occupancy = occupancy;
    this.temperature = temp;
    this.statsTime = statsTime;
    this.avgVolts = avgVolts;
    this.avgAmbient = avgAmbient;
    
  } //end of constructor
	
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
   * @return the power
   */
  public BigDecimal getPower() {
  
  	return power;
  }
	
  /**
   * @param power the power to set
   */
  public void setPower(BigDecimal power) {
  
  	this.power = power;
  }
	
  /**
   * @return the occupancy
   */
  public BigInteger getOccupancy() {
  
  	return occupancy;
  }
	
  /**
   * @param occupancy the occupancy to set
   */
  public void setOccupancy(BigInteger occupancy) {
  
  	this.occupancy = occupancy;
  }
	
  /**
   * @return the temperature
   */
  public Short getTemperature() {
  
  	return temperature;
  }
	
  /**
   * @param temperature the temperature to set
   */
  public void setTemperature(Short temperature) {
  
  	this.temperature = temperature;
  }
  
  /**
   * @return the statsTime
   */
  public Date getStatsTime() {
  
  	return statsTime;
  }
	
  /**
   * @param statsTime the statsTime to set
   */
  public void setStatsTime(Date statsTime) {
  
  	this.statsTime = statsTime;
  }
  
  /**
   * @return the avgVolts
   */
  public Short getAvgVolts() {
  
  	return avgVolts;
  }
	
  /**
   * @param avgVolts the avgVolts to set
   */
  public void setAvgVolts(Short avgVolts) {
  
  	this.avgVolts = avgVolts;
  }
  
  /**
   * @return the avgAmbient
   */
  public Short getAvgAmbient() {
  
  	return avgAmbient;
  }
	
  /**
   * @param avgAmbient the avgAmbient to set
   */
  public void setAvgAmbient(Short avgAmbient) {
  
  	this.avgAmbient = avgAmbient;
  }
			
} //end of class FixtureStatsDetail
