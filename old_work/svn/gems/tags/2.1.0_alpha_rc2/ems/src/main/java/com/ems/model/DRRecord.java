/**
 * 
 */
package com.ems.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yogesh
 * 
 */
public class DRRecord {
	private Date Day;
	private Float powerUsed;
	private Float basePowerUsed;
	private Float savedPower;
	private Float savedCost;
	private Float baseCost;
	private Float avgPrice;

	public DRRecord() {
	}

	public DRRecord(Date Day, BigDecimal powerUsed, BigDecimal basePowerUsed,
			BigDecimal savedPower, Float savedCost, Float baseCost,
			Float avgPrice) {
		this.Day = Day;
		this.powerUsed = powerUsed.floatValue();
		this.basePowerUsed = basePowerUsed.floatValue();
		this.savedPower = savedPower.floatValue();
		this.savedCost = savedCost;
		this.baseCost = baseCost;
		this.avgPrice = avgPrice;
	}

	/**
	 * @return the day
	 */
	public Date getDay() {
		return Day;
	}

	/**
	 * @param day
	 *            the day to set
	 */
	public void setDay(Date day) {
		Day = day;
	}

	/**
	 * @return the powerUsed
	 */
	public Float getPowerUsed() {
		return powerUsed;
	}

	/**
	 * @param powerUsed
	 *            the powerUsed to set
	 */
	public void setPowerUsed(Float powerUsed) {
		this.powerUsed = powerUsed;
	}

	/**
	 * @return the basePowerUsed
	 */
	public Float getBasePowerUsed() {
		return basePowerUsed;
	}

	/**
	 * @param basePowerUsed
	 *            the basePowerUsed to set
	 */
	public void setBasePowerUsed(Float basePowerUsed) {
		this.basePowerUsed = basePowerUsed;
	}

	/**
	 * @return the savedPower
	 */
	public Float getSavedPower() {
		return savedPower;
	}

	/**
	 * @param savedPower
	 *            the savedPower to set
	 */
	public void setSavedPower(Float savedPower) {
		this.savedPower = savedPower;
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
	 * @return the avgPrice
	 */
	public Float getAvgPrice() {
		return avgPrice;
	}

	/**
	 * @param avgPrice
	 *            the avgPrice to set
	 */
	public void setAvgPrice(Float avgPrice) {
		this.avgPrice = avgPrice;
	}

	/**
	 * Returns String representation of this object
	 */
	public String toString() {
		return "(" + this.Day + ", " + this.powerUsed + ", "
				+ this.basePowerUsed + ", " + this.savedPower + ", "
				+ this.savedCost + ", " + this.baseCost + ", " + this.avgPrice
				+ ")";
	}
}
