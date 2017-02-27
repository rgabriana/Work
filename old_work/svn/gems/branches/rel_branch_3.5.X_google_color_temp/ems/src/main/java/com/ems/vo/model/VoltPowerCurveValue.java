/**
 * 
 */
package com.ems.vo.model;

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
public class VoltPowerCurveValue {

  @XmlElement(name = "volts")
  private Double volts;
  @XmlElement(name = "load")
  private Double load = 0.0;
  @XmlElement(name = "value")
  private Double curveValue;
  private int noOfReadings = 0;
		
  /**
   * 
   */
  public VoltPowerCurveValue() {
    
    // TODO Auto-generated constructor stub
  }
	
  /**
   * 
   */
  public VoltPowerCurveValue(Double volts, Double load, Double curveValue) {
  
    this.volts = volts;
    this.load = load;
    this.curveValue = curveValue;
		
  } //end of constructor
	
  /**
   * @return the volts
   */
  public Double getVolts() {
  
    return volts;
  }
	
  /**
   * @param volts the volts to set
   */
  public void setVolts(Double volts) {
  
    this.volts = volts;
  }
  
  /**
   * @return the load
   */
  public Double getLoad() {
  
    return load;
  }
	
  /**
   * @param load the load to set
   */
  public void setLoad(Double load) {
  
  	this.load = load;
  }
	
  /**
   * @return the curveValue
   */
  public Double getCurveValue() {
  
  	return curveValue;
  }
	
  /**
   * @param curveValue the curveValue to set
   */
  public void setCurveValue(Double curveValue) {
  
  	this.curveValue = curveValue;
  }
  
  public void addLoad(Double load) {
    
    this.load += load;
    noOfReadings++;
    
  }
  
  public Double getAverageLoad() {
    
    return load/noOfReadings;
    
  }
  
} //end of class VoltPowerCurveValue
