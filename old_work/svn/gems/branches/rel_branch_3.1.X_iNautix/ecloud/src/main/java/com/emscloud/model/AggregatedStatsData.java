package com.emscloud.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Entity
@XmlRootElement
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AggregatedStatsData implements Serializable {

	private static final long serialVersionUID = 1029630101450441670L;
	private Long id;
	private Long custId;
	private Long levelId;
	private Date captureAt;
	private double baseEnergy;
	private BigDecimal energy;
	private BigDecimal savedEnergy;
	
	private BigDecimal occSavings;
	private BigDecimal ambientSavings;
	private BigDecimal tuneupSavings;
	private BigDecimal manualSavings;
	private Double baseCost;    
	private Double cost;
	private Double savedCost;
	private Float price;  
	
	private Float minTemp;
	private Float avgTemp;
	private Float maxTemp;
	private Float minAmb;
	private Float avgAmb;
	private Float maxAmb;
	
	public AggregatedStatsData() {
	}
    
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="floor_energy_consumption_daily_seq")
    @SequenceGenerator(name="floor_energy_consumption_daily_seq", sequenceName="floor_energy_consumption_daily_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
    
	/**
	 * @return the bldId
	 */
	@Column(name = "level_id")
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
	@Column(name = "cust_id")
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
	@Column(name = "energy")
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
	@Column(name = "capture_at")
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
	@Column(name = "cost")
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
	@Column(name = "base_energy")
	@XmlElement(name = "baseEnergy")
	public double getBaseEnergy() {
		return baseEnergy;
	}

	/**
	 * @param baseEnergy
	 *            the baseEnergy to set
	 */
	public void setBaseEnergy(double baseEnergy) {
		this.baseEnergy = baseEnergy;
	}

	/**
	 * @return the baseCost
	 */
	@Column(name = "base_cost")
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
	@Column(name = "manual_savings")
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
	@Column(name = "occ_savings")
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
	@Column(name = "ambient_savings")
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
  @Column(name = "tuneup_savings")
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
  @Column(name = "saved_energy")
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
  @Column(name = "saved_cost")
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
  @Column(name = "min_temp")
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
	@Column(name = "avg_temp")
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
	@Column(name = "max_temp")
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
	@Column(name = "min_amb")
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
	@Column(name = "avg_amb")
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
	@Column(name = "max_amb")
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
}
