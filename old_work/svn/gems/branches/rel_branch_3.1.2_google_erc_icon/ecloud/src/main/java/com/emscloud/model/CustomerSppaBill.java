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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sharad.Mahajan
 *
 */

@Entity
@Table(name = "customer_sppa_bill", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CustomerSppaBill implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5129040236930610997L;
	
	private Long id;
	private Date billStartDate;
	private Date billEndDate;
	private Date billCreationTime;
	private Customer customer;
	
	private Integer noOfDays;	
	private BigDecimal baselineEnergy;
	private BigDecimal consumedEnergy;
	private Double baseCost;
	private Double sppaCost;
	private Double savedCost;
	
	private Integer billStatus;
	@Transient
	private Double tax;
	
	private Double currentCharges;	
	private Double totalAmtDue;
	private Double paymentReceived;
	private Double prevAmtDue;
	
	/**
	 * 
	 */
	public CustomerSppaBill() {
		// TODO Auto-generated constructor stub
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="customer_sppa_bill_seq")
	@SequenceGenerator(name="customer_sppa_bill_seq", sequenceName="customer_sppa_bill_seq")
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@XmlElement(name = "customer")
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	@Column(name = "no_of_days")
	@XmlElement(name = "noOfDays")
	public Integer getNoOfDays() {
		return noOfDays;
	}

	public void setNoOfDays(Integer noOfDays) {
		this.noOfDays = noOfDays;
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
	
	@Column(name = "bill_status")
	@XmlElement(name = "billStatus")
	public Integer getBillStatus() {
		return billStatus;
	}

	public void setBillStatus(Integer billStatus) {
		this.billStatus = billStatus;
	}
	@Transient
	@XmlElement(name = "tax")
	public Double getTax() {
		return tax;
	}

	public void setTax(Double tax) {
		this.tax = tax;
	}
	
	/**
	 * @return the currentCharges
	 */
	@Column(name = "current_charges")
	@XmlElement(name="currentCharges")
	public Double getCurrentCharges() {
		return currentCharges;
	}

	/**
	 * @param currentCharges the currentCharges to set
	 */
	public void setCurrentCharges(Double currentCharges) {
		this.currentCharges = currentCharges;
	}
	
	/**
	 * @return the totalAmtDue
	 */	
	@Column(name = "total_amt_due")
	@XmlElement(name="totalAmtDue")
	public Double getTotalAmtDue() {
		return totalAmtDue;
	}

	/**
	 * @param totalAmtDue the totalAmtDue to set
	 */
	public void setTotalAmtDue(Double totalAmtDue) {
		this.totalAmtDue = totalAmtDue;
	}
	
	/**
	 * @return the paymentReceived
	 */	
	@Column(name = "amount_received")
	@XmlElement(name="paymentReceived")
	public Double getPaymentReceived() {
		return paymentReceived;
	}

	/**
	 * @param paymentReceived the paymentReceived to set
	 */
	public void setPaymentReceived(Double paymentReceived) {
		this.paymentReceived = paymentReceived;
	}
	
	/**
	 * @return the prevAmtDue
	 */		
	@Column(name = "prevamtdue")
	@XmlElement(name="prevAmtDue")
	public Double getPrevAmtDue() {
		return prevAmtDue;
	}

	/**
	 * @param prevAmtDue the prevAmtDue to set
	 */
	public void setPrevAmtDue(Double prevAmtDue) {
		this.prevAmtDue = prevAmtDue;
	}
	
} //end of class CustomerSppaBill
