package com.emscloud.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@Entity
@XmlRootElement
@IdClass(EnergyDataPK.class)
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AggregatedStatsData implements Serializable {

	private static final long serialVersionUID = 1029630101450441670L;
	@Id
	@Column(name = "level_id")
	private Long levelId;
	@Id
	@Column(name = "capture_at")
	private Date captureAt;
	@Column(name = "cust_id")
	private Long custId;
	@Column(name = "base_energy")
	private Double baseEnergy;
	@Column(name = "energy")
	private BigDecimal energy;
	  @Column(name = "saved_energy")
	private BigDecimal savedEnergy;
	
	@Column(name = "occ_savings")
	private BigDecimal occSavings;
	@Column(name = "ambient_savings")
	private BigDecimal ambientSavings;
	@Column(name = "tuneup_savings")
	private BigDecimal tuneupSavings;
	@Column(name = "manual_savings")
	private BigDecimal manualSavings;
	@Column(name = "base_cost")
	private Double baseCost;    
	@Column(name = "cost")
	private Double cost;
	@Column(name = "saved_cost")
	private Double savedCost;
	@Column(name = "price")
	private Float price;  
	
	@Column(name = "min_temp")
	private Float minTemp;
	@Column(name = "avg_temp")
	private Float avgTemp;
	@Column(name = "max_temp")
	private Float maxTemp;
	@Column(name = "min_amb")
	private Float minAmb;
	@Column(name = "avg_amb")
	private Float avgAmb;
	@Column(name = "max_amb")
	private Float maxAmb;
	@Column(name = "motion_events")
	private Long motionEvents;
	public AggregatedStatsData() {
	}
    
	/**
	 * @return the bldId
	 */
	@XmlElement(name = "levelId")
	public Long getLevelId() {
		return levelId;
	}

	/**
	 * @param levelId
	 *            the levelId to set
	 */
	public void setLevelId(Long levelId) {
		this.levelId = levelId;
	}
    
	/**
	 * @return the custId
	 */
	@XmlElement(name = "custId")
	public Long getCustId() {
		return custId;    
	}

	/**
	 * @param custId
	 *            the custId to set
	 */	
	public void setCustId(Long custId) {
		this.custId = custId;
	}

	/**
	 * @return the energy
	 */
	@XmlElement(name = "energy")
	public BigDecimal getEnergy() {
		return energy;
	}

	/**
	 * @param energy
	 *            the energy to set
	 */	
	public void setEnergy(BigDecimal energy) {
		this.energy = energy;
	}

	/**
	 * @return the captureAt
	 */
	@XmlElement(name = "captureAt")
	public Date getCaptureAt() {
		return captureAt;
	}

	/**
	 * @param captureAt
	 *            the captureAt to set
	 */
	
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
  
	/**
	 * @return the cost
	 */	
	@XmlElement(name = "cost")
	public Double getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(Double cost) {
		this.cost = cost;
	}

	/**
	 * @return the price
	 */	
	@XmlElement(name = "price")
	public Float getPrice() {
		return price;
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(Float price) {
		this.price = price;
	}

	/**
	 * @return the baseEnergy
	 */
	@XmlElement(name = "baseEnergy")
	public Double getBaseEnergy() {
		return baseEnergy;
	}

	/**
	 * @param baseEnergy
	 *            the baseEnergy to set
	 */
	public void setBaseEnergy(Double baseEnergy) {
		this.baseEnergy = baseEnergy;
	}

	/**
	 * @return the baseCost
	 */
	@XmlElement(name = "baseCost")
	public Double getBaseCost() {
		return baseCost;
	}

	/**
	 * @param baseCost
	 *            the baseCost to set
	 */
	public void setBaseCost(Double baseCost) {
		this.baseCost = baseCost;
	}

	/**
	 * @return the manualSavings
	 */
	@XmlElement(name = "manualSavings")
	public BigDecimal getManualSavings() {
		return manualSavings;
	}

	/**
	 * @param manualSavings
	 *            the manualSavings to set
	 */
	public void setManualSavings(BigDecimal manualSavings) {
		this.manualSavings = manualSavings;
	}
    
	/**
	 * @return the occSavings
	 */
	@XmlElement(name = "occSavings")
	public BigDecimal getOccSavings() {
		return occSavings;
	}	

	/**
	 * @param occSavings
	 *            the occSavings to set
	 */
	public void setOccSavings(BigDecimal occSavings) {
		this.occSavings = occSavings;
	}	
   
	/**
	 * @return the ambientSavings
   	*/
	@XmlElement(name = "ambient_savings")
  public BigDecimal getAmbientSavings() {
  	return ambientSavings;
  }

  /**
   * @param ambientSavings
   *            the ambientSavings to set
   */
  public void setAmbientSavings(BigDecimal ambientSavings) {
  	this.ambientSavings = ambientSavings;
  }
  
  /**
  * @return the tuneupSavings
  */
	@XmlElement(name = "tuneupSavings")
  public BigDecimal getTuneupSavings() {
  	return tuneupSavings;
  }

  /**
   * @param tuneupSavings
   *            the tuneupSavings to set
   */
  public void setTuneupSavings(BigDecimal tuneupSavings) {
  	this.tuneupSavings = tuneupSavings;
  }
  
  /**
   * @return the savedEnergy
   */
	@XmlElement(name = "savedEnergy")
  public BigDecimal getSavedEnergy() {
  	 return savedEnergy;
   }

   /**
    * @param savedEnergy
    *            the savedEnergy to set
    */
   public void setSavedEnergy(BigDecimal savedEnergy) {
  	 this.savedEnergy = savedEnergy;
   }

  /**
   * @return the savedCost
   */
 	@XmlElement(name = "savedCost")
  public Double getSavedCost() {
  	return savedCost;
  }

  /**
   * @param savedCost
   *            the savedCost to set
   */
  public void setSavedCost(Double savedCost) {
  	this.savedCost = savedCost;
  }
	/**
	 * @return the minTemp
	 */
	@XmlElement(name = "minTemp")
	public Float getMinTemp() {
		return minTemp;
	}

	/**
	 * @param minTemp the minTemp to set
	 */
	public void setMinTemp(Float minTemp) {
		this.minTemp = minTemp;
	}

	/**
	 * @return the avgTemp
	 */
	@XmlElement(name = "avgTemp")
	public Float getAvgTemp() {
		return avgTemp;
	}

	/**
	 * @param avgTemp the avgTemp to set
	 */
	public void setAvgTemp(Float avgTemp) {
		this.avgTemp = avgTemp;
	}

	/**
	 * @return the maxTemp
	 */
	@XmlElement(name = "maxTemp")
	public Float getMaxTemp() {
		return maxTemp;
	}

	/**
	 * @param maxTemp the maxTemp to set
	 */
	public void setMaxTemp(Float maxTemp) {
		this.maxTemp = maxTemp;
	}

	/**
	 * @return the minAmb
	 */
	@XmlElement(name = "minAmb")
	public Float getMinAmb() {
		return minAmb;
	}

	/**
	 * @param minAmb the minAmb to set
	 */
	public void setMinAmb(Float minAmb) {
		this.minAmb = minAmb;
	}

	/**
	 * @return the avgAmb
	 */
	@XmlElement(name = "avgAmb")
	public Float getAvgAmb() {
		return avgAmb;
	}

	/**
	 * @param avgAmb the avgAmb to set
	 */
	public void setAvgAmb(Float avgAmb) {
		this.avgAmb = avgAmb;
	}

	/**
	 * @return the maxAmb
	 */
	@XmlElement(name = "maxAmb")
	public Float getMaxAmb() {
		return maxAmb;
	}

	/**
	 * @param maxAmb the maxAmb to set
	 */
	public void setMaxAmb(Float maxAmb) {
		this.maxAmb = maxAmb;
	}

	/**
	 * @return the motionBits
	 */
	@XmlElement(name = "motionEvents")
	public Long getMotionEvents() {
		return motionEvents;
	}

	/**
	 * @param motionEvents the motionEvents to set
	 */
	public void setMotionEvents(Long motionEvents) {
		this.motionEvents = motionEvents;
	}
}
