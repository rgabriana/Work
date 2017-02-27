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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "em_instance", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmInstance implements java.io.Serializable {

	private static final long serialVersionUID = -5404694993653717365L;
	private long id;
	private String version;
	private Customer customer;
	private String name;
	private String macId;
	private String timeZone;
	private String databaseName;
	private Boolean active;
	private Date lastConnectivityAt;
	private String utcLastConnectivityAt;
	
	private ReplicaServer replicaServer;
	
	private String contactName;
	
	
	private String contactEmail;
	
	private String address;
	
	private String contactPhone;
	
	private Boolean sppaEnabled;
	private Long latestEmStateId ;
	private Float sppaPrice;
	
	private Date certStartDate;
	private Date certEndDate;
	
	private Double taxRate;
	private BigDecimal blockPurchaseEnergy;
	private BigDecimal blockEnergyConsumed;

	private boolean sppaBillEnabled;
	private boolean taxable;
	private Long totalBilledNoOfDays;
	private String geoLocation;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replica_server_id")
	public ReplicaServer getReplicaServer() {
		return replicaServer;
	}

	public void setReplicaServer(ReplicaServer replicaServer) {
		this.replicaServer = replicaServer;
	}

	@Transient
	private String healthOfEmInstance;
	
	@Transient
	@XmlElement(name = "healthOfEmInstance")
	public String getHealthOfEmInstance() {
		return healthOfEmInstance;
	}

	public void setHealthOfEmInstance(String healthOfEmInstance) {
		this.healthOfEmInstance = healthOfEmInstance;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_instance_seq")
    @SequenceGenerator(name="em_instance_seq", sequenceName="em_instance_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}


	@Column(name = "version")
	@XmlElement(name = "version")
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Column(name = "name")
	@XmlElement(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "mac_id", nullable = false)
	@XmlElement(name = "macId")
	public String getMacId() {
		return this.macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}
	
	@Column(name = "time_zone")
	@XmlElement(name = "timeZone")
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	@Column(name = "last_connectivity_at")
	@XmlElement(name = "lastConnectivityAt")
	public Date getLastConnectivityAt() {
		return this.lastConnectivityAt;
	}

	public void setLastConnectivityAt(Date lastConnectivityAt) {
		this.lastConnectivityAt = lastConnectivityAt;
	}
	
	@Transient
	@XmlElement(name = "utcLastConnectivityAt")
	public String getUtcLastConnectivityAt() {
		return utcLastConnectivityAt;
	}

	public void setUtcLastConnectivityAt(String utcLastConnectivityAt) {
		this.utcLastConnectivityAt = utcLastConnectivityAt;
	}

	@Column(name = "database_name", length = 29)
	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	@Column(name = "active")
	@XmlElement(name = "active")
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	@Column(name = "contact_name")
	@XmlElement(name = "contactName")
	public String getContactName() {
		return contactName;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	@Column(name = "contact_email")
	@XmlElement(name = "contactEmail")
	public String getContactEmail() {
		return contactEmail;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "address")
	@XmlElement(name = "address")
	public String getAddress() {
		return address;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	@Column(name = "contact_phone")
	@XmlElement(name = "contactPhone")
	public String getContactPhone() {
		return contactPhone;
	}
	
	public void setSppaEnabled(Boolean sppaEnabled) {
		this.sppaEnabled = sppaEnabled;
	}

	@Column(name = "sppa_enabled")
	@XmlElement(name = "sppaEnabled")
	public Boolean getSppaEnabled() {
		return sppaEnabled;
	}

	@Column(name = "latest_em_state_id" , insertable =  false, updatable = false)
	@XmlElement(name = "latestEmStateId")
	public Long getLatestEmStateId() {
		return latestEmStateId;
	}

	public void setLatestEmStateId(Long latestEmStateId) {
		this.latestEmStateId = latestEmStateId;
	}
	
	@Column(name = "sppa_price")
	@XmlElement(name = "sppaPrice")
	public Float getSppaPrice() {
		return this.sppaPrice;
	}

	public void setSppaPrice(Float sppaPrice) {
		this.sppaPrice = sppaPrice;
	}

	@Column(name = "cert_start_date")
	@XmlElement(name = "certStartDate")
	public Date getCertStartDate() {
		return certStartDate;
	}

	public void setCertStartDate(Date certStartDate) {
		this.certStartDate = certStartDate;
	}

	@Column(name = "cert_end_date")
	@XmlElement(name = "certEndDate")
	public Date getCertEndDate() {
		return certEndDate;
	}

	public void setCertEndDate(Date certEndDate) {
		this.certEndDate = certEndDate;
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
	@Column(name = "sppa_bill_enabled")
	@XmlElement(name = "sppaBillEnabled")
	public boolean getSppaBillEnabled() {
		return sppaBillEnabled;
	}

	public void setSppaBillEnabled(boolean sppaBillEnabled) {
		this.sppaBillEnabled = sppaBillEnabled;
	}
	@Transient
	@XmlElement(name = "taxable")
	public boolean getTaxable() {
		if(taxRate!=null && taxRate>0)
		{
			taxable = true;
		}
		return taxable;
	}

	public void setTaxable(boolean taxable) {
		this.taxable = taxable;
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

}
