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
public class GroupECRecord {
    @XmlElement(name="id")
	private Integer i;
    @XmlElement(name="name")
	private String name;
    @XmlElement(name="powerused")
	private Float powerUsed;
    @XmlElement(name="basepowerused")
	private Float basePowerUsed;
    @XmlElement(name="savedpower")
    private Float savedPower;
    @XmlElement(name="savedcost")
    private Float savedCost;
    @XmlElement(name="totalfixtures")
    private Integer totalFixtures;
    @XmlElement(name="drsensitivity")
    private Integer drSensitivity;

	public GroupECRecord() {
	}

	public GroupECRecord(Integer i, String name, BigDecimal powerUsed,
			BigDecimal basePowerUsed, BigDecimal savedPower, Float savedCost,
			Integer totalFixtures) {
		this.i = i;
		this.name = name;
		this.powerUsed = powerUsed.floatValue();
		this.basePowerUsed = basePowerUsed.floatValue();
		this.savedPower = savedPower.floatValue();
		this.savedCost = savedCost;
		this.totalFixtures = totalFixtures;
	}

	/**
	 * @return the i
	 */
	public Integer getI() {
		return i;
	}

	/**
	 * @param i
	 *            the i to set
	 */
	public void setI(Integer i) {
		this.i = i;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the totalFixtures
	 */
	public Integer getTotalFixtures() {
		return totalFixtures;
	}

	/**
	 * @param totalFixtures
	 *            the totalFixtures to set
	 */
	public void setTotalFixtures(Integer totalFixtures) {
		this.totalFixtures = totalFixtures;
	}

    /**
     * @return the drSensitivity
     */
    public Integer getDrSensitivity() {
        return drSensitivity;
    }

    /**
     * @param drSensitivity the drSensitivity to set
     */
    public void setDrSensitivity(Integer drSensitivity) {
        this.drSensitivity = drSensitivity;
    }

	public String toString() {
		return "(" + this.i + ", " + this.name + ", " + this.powerUsed + ", "
				+ this.basePowerUsed + ", " + this.savedPower + ", "
				+ this.savedCost + ", " + this.totalFixtures + ")";
	}
}
