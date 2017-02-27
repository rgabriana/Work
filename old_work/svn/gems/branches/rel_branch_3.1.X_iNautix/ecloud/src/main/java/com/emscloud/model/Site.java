package com.emscloud.model;



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


@Entity
@Table(name = "site", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Site implements java.io.Serializable {

	private static final long serialVersionUID = -5404694993653717365L;
	
	private long id;	
	private Customer customer;
	private String geoLocation;
	private String name;
	private String poNumber;
	
	private Float sppaPrice;
	private Double taxRate;
	
	private BigDecimal blockPurchaseEnergy;
	private BigDecimal blockEnergyConsumed;
	private Long totalBilledNoOfDays;
	
	private Date billStartDate;
		
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="site_seq")
  @SequenceGenerator(name="site_seq", sequenceName="site_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Column(name = "geo_location")
	@XmlElement(name = "geoLocation")
	public String getGeoLocation() {
		return geoLocation;
	}

	/**
	 * @param geoLocation the geoLocation to set
	 */
	public void setGeoLocation(String geoLocation) {
		this.geoLocation = geoLocation;
	}
	
	@Column(name = "name")
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "block_purchase_energy")
	@XmlElement(name = "blockPurchaseEnergy")
	public BigDecimal getBlockPurchaseEnergy() {
		return blockPurchaseEnergy;
	}

	/**
	 * @param blockPurchaseEnergy the blockPurchaseEnergy to set
	 */
	public void setBlockPurchaseEnergy(BigDecimal blockPurchaseEnergy) {
		this.blockPurchaseEnergy = blockPurchaseEnergy;
	}

	@Column(name = "block_energy_consumed")
	@XmlElement(name = "blockEnergyConsumed")
	public BigDecimal getBlockEnergyConsumed() {
		return blockEnergyConsumed;
	}

	/**
	 * @param blockEnergyConsumed the blockEnergyConsumed to set
	 */
	public void setBlockEnergyConsumed(BigDecimal blockEnergyConsumed) {
		this.blockEnergyConsumed = blockEnergyConsumed;
	}
	
	@Column(name = "total_billed_no_of_days")
	@XmlElement(name = "totalBilledNoOfDays")
	public Long getTotalBilledNoOfDays() {
		return totalBilledNoOfDays;
	}

	/**
	 * @param totalBilledNoOfDays the totalBilledNoOfDays to set
	 */
	public void setTotalBilledNoOfDays(Long totalBilledNoOfDays) {
		this.totalBilledNoOfDays = totalBilledNoOfDays;
	}

	@Column(name = "sppa_price")
	@XmlElement(name = "sppaPrice")
	public Float getSppaPrice() {
		return this.sppaPrice;
	}

	public void setSppaPrice(Float sppaPrice) {
		this.sppaPrice = sppaPrice;
	}
	
	@Column(name = "tax_rate")
	@XmlElement(name = "taxRate")
	public Double getTaxRate() {
		return taxRate;
	}

	/**
	 * @param taxRate the taxRate to set
	 */
	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}
	
	@Column(name = "po_number")
	@XmlElement(name = "poNumber")
	public String getPoNumber() {
		return poNumber;
	}

	/**
	 * @param poNumber the poNumber to set
	 */
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	
	@Column(name = "bill_start_date")
	@XmlElement(name = "billStartDate")
	public Date getBillStartDate() {
		return billStartDate;
	}

	/**
	 * @param billStartDate the billStartDate to set
	 */
	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}
} //end of class Site
