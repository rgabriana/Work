/**
 * 
 */
package com.ems.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;


/**
 * @author Sreedhar
 *
 */
public class FixtureCurrentData {
  
  private Long id;
  @XmlElement(name = "wattage")
  private Integer wattage;
  @XmlElement(name = "lightlevel")
  private Integer dimmerControl;
  @XmlElement(name = "currentstate")
  private String currentState;
  @XmlElement(name = "lastoccupancyseen")
  private Integer lastOccupancySeen;
  @XmlElement(name = "ambientlight")
  private Integer lightLevel;
  @XmlElement(name = "bulblife")
  private Double bulbLife;
  @XmlElement(name = "lastconnectivityat")
  public Date lastConnectivityAt;
  @XmlElement(name = "laststatsrcvdtime")
  private Date lastStatsRcvdTime;
  private Short profileChecksum;
  private Short globalProfileChecksum;
  @XmlElement(name = "currapp")
  public Short currApp;  
  @XmlElement(name = "avgtemperature")
  private Short avgTemperature;
  private BigDecimal baselinePower;
   
  public FixtureCurrentData() { }
    
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
   * @return the wattage
   */
  public Integer getWattage() {
  
    return wattage;
  }
  
  /**
   * @param wattage the wattage to set
   */
  public void setWattage(Integer wattage) {
  
    this.wattage = wattage;
  }
  
  /**
   * @return the dimmerControl
   */
  public Integer getDimmerControl() {
  
    return dimmerControl;
  }
  
  /**
   * @param dimmerControl the dimmerControl to set
   */
  public void setDimmerControl(Integer dimmerControl) {
  
    this.dimmerControl = dimmerControl;
  }
  
  /**
   * @return the currentState
   */
  public String getCurrentState() {
  
    return currentState;
  }
  
  /**
   * @param currentState the currentState to set
   */
  public void setCurrentState(String currentState) {
  
    this.currentState = currentState;
  }
  
  /**
   * @return the lastOccupancySeen
   */
  public Integer getLastOccupancySeen() {
  
    return lastOccupancySeen;
  }
  
  /**
   * @param lastOccupancySeen the lastOccupancySeen to set
   */
  public void setLastOccupancySeen(Integer lastOccupancySeen) {
  
    this.lastOccupancySeen = lastOccupancySeen;
  }
  
  /**
   * @return the lightLevel
   */
  public Integer getLightLevel() {
  
    return lightLevel;
  }
  
  /**
   * @param lightLevel the lightLevel to set
   */
  public void setLightLevel(Integer lightLevel) {
  
    this.lightLevel = lightLevel;
  }
  
  /**
   * @return the bulbLife
   */
  public Double getBulbLife() {
  
    return bulbLife;
  }
  
  /**
   * @param bulbLife the bulbLife to set
   */
  public void setBulbLife(Double bulbLife) {
  
    this.bulbLife = bulbLife;
  }
  
  /**
   * @return the lastConnectivityAt
   */
  public Date getLastConnectivityAt() {
  
    return lastConnectivityAt;
  }
  
  /**
   * @param lastConnectivityAt the lastConnectivityAt to set
   */
  public void setLastConnectivityAt(Date lastConnectivityAt) {
  
    this.lastConnectivityAt = lastConnectivityAt;
  }
  
  /**
   * @return the lastStatsRcvdTime
   */
  public Date getLastStatsRcvdTime() {
  
    return lastStatsRcvdTime;
  }
  
  /**
   * @param lastStatsRcvdTime the lastStatsRcvdTime to set
   */
  public void setLastStatsRcvdTime(Date lastStatsRcvdTime) {
  
    this.lastStatsRcvdTime = lastStatsRcvdTime;
  }
  
  /**
   * @return the profileChecksum
   */
  public Short getProfileChecksum() {
  
    return profileChecksum;
  }
  
  /**
   * @param profileChecksum the profileChecksum to set
   */
  public void setProfileChecksum(Short profileChecksum) {
  
    this.profileChecksum = profileChecksum;
  }
  
  /**
   * @return the globalProfileChecksum
   */
  public Short getGlobalProfileChecksum() {
  
    return globalProfileChecksum;
  }
  
  /**
   * @param globalProfileChecksum the globalProfileChecksum to set
   */
  public void setGlobalProfileChecksum(Short globalProfileChecksum) {
  
    this.globalProfileChecksum = globalProfileChecksum;
  }
  
  /**
   * @return the currApp
   */
  public Short getCurrApp() {
  
    return currApp;
  }
  
  /**
   * @param currApp the currApp to set
   */
  public void setCurrApp(Short currApp) {
  
    this.currApp = currApp;
  }
  
  /**
   * @return the avgTemperature
   */
  public Short getAvgTemperature() {
  
    return avgTemperature;
  }
  
  /**
   * @param avgTemperature the avgTemperature to set
   */
  public void setAvgTemperature(Short avgTemperature) {
  
    this.avgTemperature = avgTemperature;
  }
  
  /**
   * @return the baselinePower
   */
  public BigDecimal getBaselinePower() {
  
    return baselinePower;
  }
  
  /**
   * @param baselinePower the baselinePower to set
   */
  public void setBaselinePower(BigDecimal baselinePower) {
  
    this.baselinePower = baselinePower;
  }
     
}
