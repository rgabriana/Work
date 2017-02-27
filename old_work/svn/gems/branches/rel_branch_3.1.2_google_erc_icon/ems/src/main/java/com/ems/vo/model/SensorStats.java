/**
 * 
 */
package com.ems.vo.model;

import java.util.Date;
import java.util.List;

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
public class SensorStats {
  
  @XmlElement(name = "captureTime")
  private String captureTime;
  @XmlElement(name = "sensorList")
  private List<Sensor> sensorList;
    
  /**
   * 
   */
  public SensorStats() {
  
    // TODO Auto-generated constructor stub
  }
  
  /**
   * @return the sensorList
   */
  public List<Sensor> getSensorList() {
  
    return sensorList;
  }
  
  /**
   * @param sensorList the sensorList to set
   */
  public void setSensorList(List<Sensor> sensorList) {
  
    this.sensorList = sensorList;
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
    
} //end of class SensorStats
