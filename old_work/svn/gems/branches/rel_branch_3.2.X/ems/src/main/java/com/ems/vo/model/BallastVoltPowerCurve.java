/**
 * 
 */
package com.ems.vo.model;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Ballast;

/**
 * @author Sreedhar
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BallastVoltPowerCurve {

  @XmlElement(name = "status")
  private String status;
  @XmlElement(name = "fixtureName")
  private String fixtureName;
  @XmlElement(name = "ballast")
  private Ballast ballast;
  @XmlElement(name = "curveMap")
  private Collection<VoltPowerCurveValue> voltPowerCurveMap;
		
  /**
   * 
   */
  public BallastVoltPowerCurve() {
	
    // TODO Auto-generated constructor stub
  }
	
  /**
   * 
   */
  public BallastVoltPowerCurve(String fixtureName, Ballast ballast, List map) {
	    
    this.fixtureName = fixtureName;
    this.ballast = ballast;    
    this.voltPowerCurveMap = map;
    
  } //end of constructor
	
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
   * @return the ballast
   */
  public Ballast getBallast() {
  
  	return ballast;
  }
	
  /**
   * @param ballast the ballast to set
   */
  public void setBallast(Ballast ballast) {
  
  	this.ballast = ballast;
  }
	
  /**
   * @return the voltPowerCurveMap
   */
  public Collection<VoltPowerCurveValue> getVoltPowerCurveMap() {
  
  	return voltPowerCurveMap;
  }
	
  /**
   * @param voltPowerCurveMap the voltPowerCurveMap to set
   */
  public void setVoltPowerCurveMap(Collection<VoltPowerCurveValue> voltPowerCurveMap) {
  
  	this.voltPowerCurveMap = voltPowerCurveMap;
  }
  
  /**
   * @return the status
   */
  public String getStatus() {
  
  	return status;
  }
	
  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
  
  	this.status = status;
  }

} //end of class BallastVoltPowerCurve
