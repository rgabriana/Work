package com.communicator.uem.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EcSyncVo {

	private Long custId;
	private Long levelId;
	private Date captureAt;
	private Double baseEnergy;
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
	private Long motionEvents;
	private Boolean zbUpdate;

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
	 * @param minTemp
	 *            the minTemp to set
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
	 * @param avgTemp
	 *            the avgTemp to set
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
	 * @param maxTemp
	 *            the maxTemp to set
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
	 * @param minAmb
	 *            the minAmb to set
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
	 * @param avgAmb
	 *            the avgAmb to set
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
	 * @param maxAmb
	 *            the maxAmb to set
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
	 * @param motionEvents
	 *            the motionEvents to set
	 */
	public void setMotionEvents(Long motionEvents) {
		this.motionEvents = motionEvents;
	}
	
	@XmlElement(name = "zbUpdate")
	public Boolean getZbUpdate() {
		return zbUpdate;
	}

	public void setZbUpdate(Boolean zbUpdate) {
		this.zbUpdate = zbUpdate;
	}

}
