package com.emsdashboard.model;

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
 * @author Sameer Surjikar
 * 
 */

public class EnergyConsumption implements Serializable {

	    private Long id;
	    private Long gemsId;
	    private Long tenantsId ;
	    private BigDecimal powerUsed;
	    private BigDecimal basePowerUsed;
	    private Float cost;
	    private Float baseCost =0.0f;
	    private BigDecimal occSaving;
	    private BigDecimal tuneupSaving;
	    private BigDecimal ambientSaving;
	    private BigDecimal manualSaving;
	    private Long totalFixtureContributed ;
	    private Float price;
	    private BigDecimal savedPowerUsed;
	    private Float savedCost;
	    private Date captureAt;
    public EnergyConsumption() {
    }

    public EnergyConsumption(Long id, BigDecimal powerUsed, Date captureAt) {
        this.id = id;
        this.powerUsed = powerUsed;
        this.captureAt = captureAt;
    }

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
	 * @return the gemsId
	 */
	public Long getGemsId() {
		return gemsId;
	}

	/**
	 * @param gemsId the gemsId to set
	 */
	public void setGemsId(Long gemsId) {
		this.gemsId = gemsId;
	}

	/**
	 * @return the tenantsId
	 */
	public Long getTenantsId() {
		return tenantsId;
	}

	/**
	 * @param tenantsId the tenantsId to set
	 */
	public void setTenantsId(Long tenantsId) {
		this.tenantsId = tenantsId;
	}

	/**
	 * @return the powerUsed
	 */
	public BigDecimal getPowerUsed() {
		return powerUsed;
	}

	/**
	 * @param powerUsed the powerUsed to set
	 */
	public void setPowerUsed(BigDecimal powerUsed) {
		this.powerUsed = powerUsed;
	}

	/**
	 * @return the basePowerUsed
	 */
	public BigDecimal getBasePowerUsed() {
		return basePowerUsed;
	}

	/**
	 * @param basePowerUsed the basePowerUsed to set
	 */
	public void setBasePowerUsed(BigDecimal basePowerUsed) {
		this.basePowerUsed = basePowerUsed;
	}

	/**
	 * @return the cost
	 */
	public Float getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(Float cost) {
		this.cost = cost;
	}

	/**
	 * @return the baseCost
	 */
	public Float getBaseCost() {
		return baseCost;
	}

	/**
	 * @param baseCost the baseCost to set
	 */
	public void setBaseCost(Float baseCost) {
		this.baseCost = baseCost;
	}

	/**
	 * @return the occSaving
	 */
	public BigDecimal getOccSaving() {
		return occSaving;
	}

	/**
	 * @param occSaving the occSaving to set
	 */
	public void setOccSaving(BigDecimal occSaving) {
		this.occSaving = occSaving;
	}

	/**
	 * @return the tuneupSaving
	 */
	public BigDecimal getTuneupSaving() {
		return tuneupSaving;
	}

	/**
	 * @param tuneupSaving the tuneupSaving to set
	 */
	public void setTuneupSaving(BigDecimal tuneupSaving) {
		this.tuneupSaving = tuneupSaving;
	}

	/**
	 * @return the ambientSaving
	 */
	public BigDecimal getAmbientSaving() {
		return ambientSaving;
	}

	/**
	 * @param ambientSaving the ambientSaving to set
	 */
	public void setAmbientSaving(BigDecimal ambientSaving) {
		this.ambientSaving = ambientSaving;
	}

	/**
	 * @return the manualSaving
	 */
	public BigDecimal getManualSaving() {
		return manualSaving;
	}

	/**
	 * @param manualSaving the manualSaving to set
	 */
	public void setManualSaving(BigDecimal manualSaving) {
		this.manualSaving = manualSaving;
	}

	/**
	 * @return the totalFixtureContributed
	 */
	public Long getTotalFixtureContributed() {
		return totalFixtureContributed;
	}

	/**
	 * @param totalFixtureContributed the totalFixtureContributed to set
	 */
	public void setTotalFixtureContributed(Long totalFixtureContributed) {
		this.totalFixtureContributed = totalFixtureContributed;
	}

	/**
	 * @return the price
	 */
	public Float getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(Float price) {
		this.price = price;
	}

	/**
	 * @return the savedPowerUsed
	 */
	public BigDecimal getSavedPowerUsed() {
		return savedPowerUsed;
	}

	/**
	 * @param savedPowerUsed the savedPowerUsed to set
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
	 * @param savedCost the savedCost to set
	 */
	public void setSavedCost(Float savedCost) {
		this.savedCost = savedCost;
	}

	/**
	 * @return the captureAt
	 */
	public Date getCaptureAt() {
		return captureAt;
	}

	/**
	 * @param captureAt the captureAt to set
	 */
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}

    
}
