package com.emsdashboard.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Avgrecord{

	@XmlElement(name="id")
	private Integer i;
	@XmlElement(name="EN")
	private Float EN;
	@XmlElement(name="price")
	private Float price;
	@XmlElement(name="cost")
	private Float cost;
	@XmlElement(name="basepowerused")
	private Float basePowerUsed;
	@XmlElement(name="basecost")
	private Float baseCost;
	@XmlElement(name="captureon")
	private Date captureOn;

	public Avgrecord() {
	}
	
	public Avgrecord(BigDecimal EN,Double price,Double cost,Float basePowerUsed,Float baseCost,Date captureOn) {
		this.EN = EN.floatValue();
		this.price = price.floatValue();
		this.cost = cost.floatValue();
		this.basePowerUsed = basePowerUsed;
		this.baseCost = baseCost;
		this.captureOn = captureOn;
	}
	
	/**
	 * @return the i
	 */
	public Integer getI() {
		return i;
	}

	/**
	 * @param i the i to set
	 */
	public void setI(Integer i) {
		this.i = i;
	}

	/**
	 * @return the eN
	 */
	public Float getEN() {
		return EN;
	}

	/**
	 * @param eN the eN to set
	 */
	public void setEN(Float eN) {
		EN = eN;
	}

	public String toString() {
		return "i: " + i + " " + EN;
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
	 * @return the captureOn
	 */
	public Date getCaptureOn() {
		return captureOn;
	}

	/**
	 * @param captureOn the captureOn to set
	 */
	public void setCaptureOn(Date captureOn) {
		this.captureOn = captureOn;
	}

	/**
	 * @return the basePowerUsed
	 */
	public Float getBasePowerUsed() {
		return basePowerUsed;
	}

	/**
	 * @param basePowerUsed the basePowerUsed to set
	 */
	public void setBasePowerUsed(Float basePowerUsed) {
		this.basePowerUsed = basePowerUsed;
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
}


