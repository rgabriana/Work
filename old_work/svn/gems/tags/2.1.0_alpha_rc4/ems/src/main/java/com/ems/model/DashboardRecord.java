/**
 * 
 */
package com.ems.model;

import java.util.Date;

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
public class DashboardRecord {
    @XmlElement(name = "powerused")
    private Double powerused;
    @XmlElement(name = "basepowerused")
    private Double basePowerUsed;
    @XmlElement(name = "savedpower")
    private Double savedPower;
    @XmlElement(name = "price")
    private Float price;
    @XmlElement(name = "cost")
    private Float cost;
    @XmlElement(name = "savedcost")
    private Float savedCost;
    @XmlElement(name = "powersaving")
    private Double powersaving;
    @XmlElement(name = "occsaving")
    private Double occsaving;
    @XmlElement(name = "tasktuneupsaving")
    private Double tasktuneupsaving;
    @XmlElement(name = "ambientsaving")
    private Double ambientsaving;
    @XmlElement(name = "manualsaving")
    private Double manualsaving;
    @XmlElement(name = "avgload")
    private Float avgLoad;
    @XmlElement(name = "peakload")
    private Float peakLoad;
    @XmlElement(name = "minload")
    private Float minLoad;
    @XmlElement(name = "captureon")
    private Date captureOn;
    @XmlElement(name = "orgname")
    private String orgName;

	public DashboardRecord() {

    }

    /**
     * @return the powerused
     */
    public Double getPowerused() {
        return powerused;
    }

    /**
     * @param powerused
     *            the powerused to set
     */
    public void setPowerused(Double powerused) {
        this.powerused = powerused;
    }

    /**
     * @return the basePowerUsed
     */
    public Double getBasePowerUsed() {
        return basePowerUsed;
    }

    /**
     * @param basePowerUsed
     *            the basePowerUsed to set
     */
    public void setBasePowerUsed(Double basePowerUsed) {
        this.basePowerUsed = basePowerUsed;
    }

    /**
     * @return the savedPower
     */
    public Double getSavedPower() {
        return savedPower;
    }

    /**
     * @param savedPower
     *            the savedPower to set
     */
    public void setSavedPower(Double savedPower) {
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
     * @return the powersaving
     */
    public Double getPowersaving() {
        return powersaving;
    }

    /**
     * @param powersaving
     *            the powersaving to set
     */
    public void setPowersaving(Double powersaving) {
        this.powersaving = powersaving;
    }

    /**
     * @return the occsaving
     */
    public Double getOccsaving() {
        return occsaving;
    }

    /**
     * @param occsaving
     *            the occsaving to set
     */
    public void setOccsaving(Double occsaving) {
        this.occsaving = occsaving;
    }

    /**
     * @return the tasktuneupsaving
     */
    public Double getTasktuneupsaving() {
        return tasktuneupsaving;
    }

    /**
     * @param tasktuneupsaving
     *            the tasktuneupsaving to set
     */
    public void setTasktuneupsaving(Double tasktuneupsaving) {
        this.tasktuneupsaving = tasktuneupsaving;
    }

    /**
     * @return the ambientsaving
     */
    public Double getAmbientsaving() {
        return ambientsaving;
    }

    /**
     * @param ambientsaving
     *            the ambientsaving to set
     */
    public void setAmbientsaving(Double ambientsaving) {
        this.ambientsaving = ambientsaving;
    }

    /**
     * @return the manualsaving
     */
    public Double getManualsaving() {
        return manualsaving;
    }

    /**
     * @param manualsaving
     *            the manualsaving to set
     */
    public void setManualsaving(Double manualsaving) {
        this.manualsaving = manualsaving;
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

    /**
     * @return the captureOn
     */
    public Date getCaptureOn() {
        return captureOn;
    }

    /**
     * @param captureOn
     *            the captureOn to set
     */
    public void setCaptureOn(Date captureOn) {
        this.captureOn = captureOn;
    }
    
    public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
}
