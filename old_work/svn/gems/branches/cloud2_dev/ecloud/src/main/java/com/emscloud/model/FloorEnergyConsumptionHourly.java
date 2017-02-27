package com.emscloud.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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
@Table(name = "floor_Energy_consumption_hourly", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FloorEnergyConsumptionHourly implements Serializable {

	private static final long serialVersionUID = 1029630101450441670L;
	private Long id;
	private Long custId;
	private Long floorId;
	private Date captureAt;
	private BigDecimal basePowerUsed;
	private BigDecimal powerUsed;
	private BigDecimal savedPowerUsed;
	
	private BigDecimal occSavings;
	private BigDecimal ambientSavings;
	private BigDecimal tuneupSavings;
	private BigDecimal manualSavings;
	private Float baseCost;    
	private Float cost;
	private Float savedCost;
	private Float price;  
	
	public FloorEnergyConsumptionHourly() {
	}
    
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="floor_energy_consumption_hourly_seq")
    @SequenceGenerator(name="floor_energy_consumption_hourly_seq", sequenceName="floor_energy_consumption_hourly_seq")
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
	 * @return the floorId
	 */
	@Column(name = "floor_id")
	@XmlElement(name = "floorId")
	public Long getFloorId() {
		return floorId;
	}

	/**
	 * @param floorId
	 *            the floorId to set
	 */
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
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
	 * @return the powerUsed
	 */
	@Column(name = "energy")
	@XmlElement(name = "energy")
	public BigDecimal getPowerUsed() {
		return powerUsed;
	}

	/**
	 * @param powerUsed
	 *            the powerUsed to set
	 */	
	public void setPowerUsed(BigDecimal powerUsed) {
		this.powerUsed = powerUsed;
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
	public Float getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(Float cost) {
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
	 * @return the basePowerUsed
	 */
	@Column(name = "base_energy")
	@XmlElement(name = "baseEnergy")
	public BigDecimal getBasePowerUsed() {
		return basePowerUsed;
	}

	/**
	 * @param basePowerUsed
	 *            the basePowerUsed to set
	 */
	public void setBasePowerUsed(BigDecimal basePowerUsed) {
		this.basePowerUsed = basePowerUsed;
	}

	/**
	 * @return the baseCost
	 */
	public Float getBaseCost() {
		return baseCost;
	}

	/**
	 * @param baseCost
	 *            the baseCost to set
	 */
	public void setBaseCost(Float baseCost) {
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
   * @return the savedPowerUsed
   */
   public BigDecimal getSavedPowerUsed() {
  	 return savedPowerUsed;
   }

   /**
    * @param savedPowerUsed
    *            the savedPowerUsed to set
    */
   public void setSavedPowerUsed(BigDecimal savedPowerUsed) {
  	 this.savedPowerUsed = savedPowerUsed;
   }

  /**
   * @return the savedCost
   */
  public Float getSavedCost() {
  	return savedCost;
  }

  /**
   * @param savedCost
   *            the savedCost to set
   */
  public void setSavedCost(Float savedCost) {
  	this.savedCost = savedCost;
  }

}
