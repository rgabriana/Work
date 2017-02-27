/**
 * 
 */
package com.ems.vo.model;

import java.math.BigDecimal;

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
public class Sensor {
	
  @XmlElement(name = "id")
  private Long id;
  @XmlElement(name = "name")
  private String fixtureName;
  @XmlElement(name = "power")
  private BigDecimal power;
  @XmlElement(name = "occupancy")
  private Long occupancy;
  @XmlElement(name = "temperature")
  private Short temperature;
  @XmlElement(name = "lightLevel")
  private Short lightLevel;
  @XmlElement(name = "captureTime")
  private String captureTime;
  	
	/**
	 * 
	 */
	public Sensor() {
	
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 */
	public Sensor(Long id, String fixtureName, BigDecimal power, Long occupancy, Short temp) {
		
		this.id = id;
		this.fixtureName = fixtureName;
		this.power = power;
		this.occupancy = occupancy;
		this.temperature = temp;
		
	} //end of constructor
	
  /**
   * @return the id
   */
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
   * @return the fixtureName
   */
  public String getFixtureName() {
  
  	return fixtureName;
  }
	
  /**
   * @param fixtureName the fixtureName to set
   */
  public void setFixtureName(String fixtureName) {
  
  	this.fixtureName = fixtureName;
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
  public Long getOccupancy() {
  
  	return occupancy;
  }
	
  /**
   * @param occupancy the occupancy to set
   */
  public void setOccupancy(Long occupancy) {
  
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
   * @return the lightLevel
   */
  public Short getLightLevel() {
  
    return lightLevel;
  }
  
  /**
   * @param lightLevel the lightLevel to set
   */
  public void setLightLevel(Short lightLevel) {
  
    this.lightLevel = lightLevel;
  }
  
  /**
   * @return the captureTime
   */
  public String getCaptureTime() {
  
    return captureTime;
  }
  
  /**
   * @param captureTime the captureTime to set
   */
  public void setCaptureTime(String captureTime) {
  
    this.captureTime = captureTime;
  }

} //end of class Sensor
