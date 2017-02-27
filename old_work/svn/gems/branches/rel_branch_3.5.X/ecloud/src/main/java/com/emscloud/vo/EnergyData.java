/**
 * 
 */
package com.emscloud.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sreedhar.kamishetti
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EnergyData {

	@XmlElement(name = "name")
	private String name;	
	@XmlElement(name = "levelId")
	private Long levelId;	
	@XmlElement(name = "timestamp")
	private Date timestamp;		
	@XmlElement(name = "timeSpan")
	private String timeSpan;
			
	@XmlElement(name = "baseEnergy")
	private double baseEnergy;
	@XmlElement(name = "energy")
	private BigDecimal energy;
	@XmlElement(name = "cost")
	private Double cost;
	@XmlElement(name = "savedEnergy")
	private BigDecimal savedEnergy;
	@XmlElement(name = "savedCost")
	private Double savedCost;
	@XmlElement(name = "savedAmbEnergy")
	private BigDecimal savedAmbEnergy;
	@XmlElement(name = "savedOccEnergy")
	private BigDecimal savedOccEnergy;
	@XmlElement(name = "savedTaskTunedEnergy")
	private BigDecimal savedTaskTunedEnergy;
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
	 * @return the levelId
	 */
	public Long getLevelId() {
		return levelId;
	}
	/**
	 * @param levelId the levelId to set
	 */
	public void setLevelId(Long levelId) {
		this.levelId = levelId;
	}
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the timeSpan
	 */
	public String getTimeSpan() {
		return timeSpan;
	}
	/**
	 * @param timeSpan the timeSpan to set
	 */
	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}
	/**
	 * @return the baseEnergy
	 */
	public double getBaseEnergy() {
		return baseEnergy;
	}
	/**
	 * @param baseEnergy the baseEnergy to set
	 */
	public void setBaseEnergy(double baseEnergy) {
		this.baseEnergy = baseEnergy;
	}
	/**
	 * @return the energy
	 */
	public BigDecimal getEnergy() {
		return energy;
	}
	/**
	 * @param energy the energy to set
	 */
	public void setEnergy(BigDecimal energy) {
		this.energy = energy;
	}
	/**
	 * @return the cost
	 */
	public Double getCost() {
		return cost;
	}
	/**
	 * @param cost the cost to set
	 */
	public void setCost(Double cost) {
		this.cost = cost;
	}
	/**
	 * @return the savedEnergy
	 */
	public BigDecimal getSavedEnergy() {
		return savedEnergy;
	}
	/**
	 * @param savedEnergy the savedEnergy to set
	 */
	public void setSavedEnergy(BigDecimal savedEnergy) {
		this.savedEnergy = savedEnergy;
	}
	/**
	 * @return the savedCost
	 */
	public Double getSavedCost() {
		return savedCost;
	}
	/**
	 * @param savedCost the savedCost to set
	 */
	public void setSavedCost(Double savedCost) {
		this.savedCost = savedCost;
	}
	/**
	 * @return the savedAmbEnergy
	 */
	public BigDecimal getSavedAmbEnergy() {
		return savedAmbEnergy;
	}
	/**
	 * @param savedAmbEnergy the savedAmbEnergy to set
	 */
	public void setSavedAmbEnergy(BigDecimal savedAmbEnergy) {
		this.savedAmbEnergy = savedAmbEnergy;
	}
	/**
	 * @return the savedOccEnergy
	 */
	public BigDecimal getSavedOccEnergy() {
		return savedOccEnergy;
	}
	/**
	 * @param savedOccEnergy the savedOccEnergy to set
	 */
	public void setSavedOccEnergy(BigDecimal savedOccEnergy) {
		this.savedOccEnergy = savedOccEnergy;
	}
	/**
	 * @return the savedTaskTunedEnergy
	 */
	public BigDecimal getSavedTaskTunedEnergy() {
		return savedTaskTunedEnergy;
	}
	/**
	 * @param savedTaskTunedEnergy the savedTaskTunedEnergy to set
	 */
	public void setSavedTaskTunedEnergy(BigDecimal savedTaskTunedEnergy) {
		this.savedTaskTunedEnergy = savedTaskTunedEnergy;
	}
	
} //end of class EnergyData
