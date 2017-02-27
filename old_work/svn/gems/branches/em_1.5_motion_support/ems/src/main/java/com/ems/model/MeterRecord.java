/**
 * 
 */
package com.ems.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MeterRecord {
	@XmlElement(name = "powerused")
	private Float powerused;
	@XmlElement(name = "basepowerused")
	private Float basePowerUsed;
	@XmlElement(name = "savedpower")
	private Float savedPower;
	@XmlElement(name = "price")
	private Float price;
	@XmlElement(name = "cost")
	private Float cost;
	@XmlElement(name = "savedcost")
	private Float savedCost;
	@XmlElement(name = "powersavingpercent")
	private Integer powersavingpercent;
	@XmlElement(name = "occsavingpercent")
	private Integer occsavingpercent;
	@XmlElement(name = "tasktuneupsavingpercent")
	private Integer tasktuneupsavingpercent;
	@XmlElement(name = "ambientsavingpercent")
	private Integer ambientsavingpercent;
	@XmlElement(name = "manualsavingpercent")
	private Integer manualsavingpercent;
	@XmlElement(name = "avgload")
	private Float avgLoad;
	@XmlElement(name = "peakload")
	private Float peakLoad;
	@XmlElement(name = "minload")
	private Float minLoad;

	public MeterRecord() {

	}

	public MeterRecord(BigDecimal Doublpowerused, BigDecimal basePowerUsed,
			BigDecimal savedPower, Double price, Double cost, Double savedCost,
			BigDecimal powersavingpercent, BigDecimal occsavingpercent,
			BigDecimal tasktuneupsavingpercent,
			BigDecimal ambientsavingpercent, BigDecimal manualsavingpercent) {
		this.powerused = powerused.floatValue();
		this.basePowerUsed = basePowerUsed.floatValue();
		this.savedPower = savedPower.floatValue();
		this.price = price.floatValue();
		this.cost = cost.floatValue();
		this.savedCost = savedCost.floatValue();
		this.powersavingpercent = powersavingpercent.intValue();
		this.occsavingpercent = occsavingpercent.intValue();
		this.tasktuneupsavingpercent = tasktuneupsavingpercent.intValue();
		this.ambientsavingpercent = ambientsavingpercent.intValue();
		this.manualsavingpercent = manualsavingpercent.intValue();
	}

	/**
	 * @return the powerused
	 */
	public Float getPowerused() {
		return powerused;
	}

	/**
	 * @param powerused
	 *            the powerused to set
	 */
	public void setPowerused(Float powerused) {
		this.powerused = powerused;
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
	 * @return the powersavingpercent
	 */
	public Integer getPowersavingpercent() {
		return powersavingpercent;
	}

	/**
	 * @param powersavingpercent
	 *            the powersavingpercent to set
	 */
	public void setPowersavingpercent(Integer powersavingpercent) {
		this.powersavingpercent = powersavingpercent;
	}

	/**
	 * @return the occsavingpercent
	 */
	public Integer getOccsavingpercent() {
		return occsavingpercent;
	}

	/**
	 * @param occsavingpercent
	 *            the occsavingpercent to set
	 */
	public void setOccsavingpercent(Integer occsavingpercent) {
		this.occsavingpercent = occsavingpercent;
	}

	/**
	 * @return the tasktuneupsavingpercent
	 */
	public Integer getTasktuneupsavingpercent() {
		return tasktuneupsavingpercent;
	}

	/**
	 * @param tasktuneupsavingpercent
	 *            the tasktuneupsavingpercent to set
	 */
	public void setTasktuneupsavingpercent(Integer tasktuneupsavingpercent) {
		this.tasktuneupsavingpercent = tasktuneupsavingpercent;
	}

	/**
	 * @return the ambientsavingpercent
	 */
	public Integer getAmbientsavingpercent() {
		return ambientsavingpercent;
	}

	/**
	 * @param ambientsavingpercent
	 *            the ambientsavingpercent to set
	 */
	public void setAmbientsavingpercent(Integer ambientsavingpercent) {
		this.ambientsavingpercent = ambientsavingpercent;
	}

	/**
	 * @return the manualsavingpercent
	 */
	public Integer getManualsavingpercent() {
		return manualsavingpercent;
	}

	/**
	 * @param manualsavingpercent
	 *            the manualsavingpercent to set
	 */
	public void setManualsavingpercent(Integer manualsavingpercent) {
		this.manualsavingpercent = manualsavingpercent;
	}

	/**
	 * @return the avgLoad
	 */
	public Float getAvgLoad() {
		return avgLoad;
	}

	/**
	 * @param avgLoad
	 *            the avgLoad to set
	 */
	public void setAvgLoad(Float avgLoad) {
		this.avgLoad = avgLoad;
	}

	/**
	 * @return the peakLoad
	 */
	public Float getPeakLoad() {
		return peakLoad;
	}

	/**
	 * @param peakLoad
	 *            the peakLoad to set
	 */
	public void setPeakLoad(Float peakLoad) {
		this.peakLoad = peakLoad;
	}

	/**
	 * @return the minLoad
	 */
	public Float getMinLoad() {
		return minLoad;
	}

	/**
	 * @param minLoad
	 *            the minLoad to set
	 */
	public void setMinLoad(Float minLoad) {
		this.minLoad = minLoad;
	}
}
