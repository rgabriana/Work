/**
 * 
 */
package com.emscloud.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sreedhar.kamishetti
 *
 */

@Entity
@Table(name = "sppa_bill", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SppaBill implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5129040236930610997L;
	
	private Long id;
	private Date billStartDate;
	private Date billEndDate;
	private Date billCreationTime;
	private int noOfDays;
	private EmInstance emInstance;
	private BigDecimal baselineEnergy;
	private BigDecimal consumedEnergy;
	private Double baseCost;
	private Double sppaCost;
	private Double savedCost;
	
	private Double tax;
	private BigDecimal blockEnergyRemaining;
	private Long blockTermRemaining;
	
	/**
	 * 
	 */
	public SppaBill() {
		// TODO Auto-generated constructor stub
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="sppa_bill_seq")
  @SequenceGenerator(name="sppa_bill_seq", sequenceName="sppa_bill_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "bill_start_date")
	@XmlElement(name = "billStartDate")
	public Date getBillStartDate() {
		return billStartDate;
	}

	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}
	
	@Column(name = "bill_end_date")
	@XmlElement(name = "billEndDate")
	public Date getBillEndDate() {
		return billEndDate;
	}

	public void setBillEndDate(Date billEndDate) {
		this.billEndDate = billEndDate;
	}
	
	@Column(name = "bill_creation_time")
	@XmlElement(name = "billCreationTime")
	public Date getBillCreationTime() {
		return billCreationTime;
	}

	public void setBillCreationTime(Date billCreationTime) {
		this.billCreationTime = billCreationTime;
	}

	@Column(name = "no_of_days")
	@XmlElement(name = "noOfDays")
	public int getNoOfDays() {
		return noOfDays;
	}

	public void setNoOfDays(int noOfDays) {
		this.noOfDays = noOfDays;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "em_instance_id")
	@XmlElement(name = "emInstance")
	public EmInstance getEmInstance() {
		return emInstance;
	}

	public void setEmInstance(EmInstance emInstance) {
		this.emInstance = emInstance;
	}

	@Column(name = "baseline_energy")
	@XmlElement(name = "baselineEnergy")
	public BigDecimal getBaselineEnergy() {
		return baselineEnergy;
	}

	public void setBaselineEnergy(BigDecimal baselineEnergy) {
		this.baselineEnergy = baselineEnergy;
	}

	@Column(name = "consumed_energy")
	@XmlElement(name = "consumedEnergy")
	public BigDecimal getConsumedEnergy() {
		return consumedEnergy;
	}

	public void setConsumedEnergy(BigDecimal consumedEnergy) {
		this.consumedEnergy = consumedEnergy;
	}

	@Column(name = "base_cost")
	@XmlElement(name = "baseCost")
	public Double getBaseCost() {
		return baseCost;
	}

	public void setBaseCost(Double baseCost) {
		this.baseCost = baseCost;
	}

	@Column(name = "sppa_cost")
	@XmlElement(name = "sppaCost")
	public Double getSppaCost() {
		return sppaCost;
	}

	public void setSppaCost(Double sppaCost) {
		this.sppaCost = sppaCost;
	}

	@Column(name = "saved_cost")
	@XmlElement(name = "savedCost")
	public Double getSavedCost() {
		return savedCost;
	}

	public void setSavedCost(Double savedCost) {
		this.savedCost = savedCost;
	}

	@Column(name = "tax")
	@XmlElement(name = "tax")
	public Double getTax() {
		return tax;
	}

	/**
	 * @param tax the tax to set
	 */
	public void setTax(Double tax) {
		this.tax = tax;
	}

	@Column(name = "block_energy_remaining")
	@XmlElement(name = "blockEnergyRemaining")
	public BigDecimal getBlockEnergyRemaining() {
		return blockEnergyRemaining;
	}

	/**
	 * @param blockEnergyRemaining the blockEnergyRemaining to set
	 */
	public void setBlockEnergyRemaining(BigDecimal blockEnergyRemaining) {
		this.blockEnergyRemaining = blockEnergyRemaining;
	}

	@Column(name = "block_term_remaining")
	@XmlElement(name = "blockTermRemaining")
	public Long getBlockTermRemaining() {
		return blockTermRemaining;
	}

	/**
	 * @param blockTermRemaining the blockTermRemaining to set
	 */
	public void setBlockTermRemaining(Long blockTermRemaining) {
		this.blockTermRemaining = blockTermRemaining;
	}
	
} //end of class SppaBill