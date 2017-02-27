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
import javax.persistence.OneToOne;
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
	private long emState;
	private String version;
	private Customer customer;
	private String name;
	private String macId;
	private String timeZone;
	private String databaseName;
	private Boolean active;
	private Date lastConnectivityAt;
	private String utcLastConnectivityAt;
	
	private String syncConnectivity;
	
	private ReplicaServer replicaServer;
	
	private String contactName;
	
	
	private String contactEmail;
	
	private String address;
	
	private String contactPhone;
	
	private Boolean sppaEnabled;
	private Long latestEmStateId ;
	
	private Date certStartDate;
	private Date certEndDate;
	
	private Double taxRate;
	private BigDecimal blockEnergyConsumed;

	private boolean sppaBillEnabled;
	private boolean taxable;
	private Long totalBilledNoOfDays;
	
	private boolean openTunnelToCloud;
	private boolean browseEnabledFromCloud ;
	private Long tunnelPort;
	private String browsableLink ;
	
	private boolean openSshTunnelToCloud;
	private Long sshTunnelPort;
	
	private EmHealthMonitor latestEmsHealthMonitor;
	private Date emCommissionedDate;
	private Integer noOfEmergencyFixtures;
	private BigDecimal emergencyFixturesGuidelineLoad;
	private BigDecimal emergencyFixturesLoad;
	
	private String secretKey;
	private String ipAddress;
	private String apiKey;
	private Integer noOfFloors;
	private Integer noOfMappedFloors;
	
	private Date lastSuccessfulSyncTime;
	private Boolean pauseSyncStatus;
	
	private String replicaServerName;
	
	private String osArch="NA";
	private String osVersion="NA";
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replica_server_id")
	public ReplicaServer getReplicaServer() {
		return replicaServer;
	}

	public void setReplicaServer(ReplicaServer replicaServer) {
		this.replicaServer = replicaServer;
	}
	
	@Transient
	@XmlElement(name = "replicaServerName")
	public String getReplicaServerName() {
		return replicaServerName;
	}

	public void setReplicaServerName(String replicaServerName) {
		this.replicaServerName = replicaServerName;
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

	public void setSyncConnectivity(String syncConnectivity) {
		this.syncConnectivity = syncConnectivity;
	}
	
	@Transient
	@XmlElement(name = "syncConnectivity")
	public String getSyncConnectivity() {
		return syncConnectivity;
	}
	@Column(name = "open_tunnel_to_cloud")
	@XmlElement(name = "openTunnelToCloud")
	public boolean getOpenTunnelToCloud() {
		return openTunnelToCloud;
	}
	
	public void setOpenTunnelToCloud(boolean openTunnelToCloud) {
		this.openTunnelToCloud = openTunnelToCloud;
	}
	@Column(name = "browse_enabled_from_cloud")
	@XmlElement(name = "browseEnabledFromCloud")
	public boolean getBrowseEnabledFromCloud() {
		return browseEnabledFromCloud;
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

	public void setBrowseEnabledFromCloud(boolean browseEnabledFromCloud) {
		this.browseEnabledFromCloud = browseEnabledFromCloud;
	}
	@Column(name = "tunnel_port")
	@XmlElement(name = "tunnelPort")
	public Long getTunnelPort() {
		return tunnelPort;
	}

	public void setTunnelPort(Long tunnelPort) {
		this.tunnelPort = tunnelPort;
	}
	@Column(name = "browsable_link")
	@XmlElement(name = "browsableLink")
	public String getBrowsableLink() {
		return browsableLink;
	}

	public void setBrowsableLink(String browsableLink) {
		this.browsableLink = browsableLink;
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "latest_em_health_monitor_id")
	public EmHealthMonitor getLatestEmsHealthMonitor() {
		return latestEmsHealthMonitor;
	}

	public void setLatestEmsHealthMonitor(EmHealthMonitor latestEmsHealthMonitor) {
		this.latestEmsHealthMonitor = latestEmsHealthMonitor;
	}
	
	@Column(name = "open_ssh_tunnel_to_cloud")
	@XmlElement(name = "openSshTunnelToCloud")
	public boolean getOpenSshTunnelToCloud() {
		return openSshTunnelToCloud;
	}

	public void setOpenSshTunnelToCloud(boolean openSshTunnelToCloud) {
		this.openSshTunnelToCloud = openSshTunnelToCloud;
	}
	@Column(name = "ssh_tunnel_port")
	@XmlElement(name = "sshTunnelPort")
	public Long getSshTunnelPort() {
		return sshTunnelPort;
	}

	public void setSshTunnelPort(Long sshTunnelPort) {
		this.sshTunnelPort = sshTunnelPort;
	}

    /**
     * @return the emCommissionedDate
     */
	@Column(name = "em_commissioned_date")
    @XmlElement(name = "emCommissionedDate")
    public Date getEmCommissionedDate() {
        return emCommissionedDate;
    }

    /**
     * @param emCommissionedDate the emCommissionedDate to set
     */
    public void setEmCommissionedDate(Date emCommissionedDate) {
        this.emCommissionedDate = emCommissionedDate;
    }

    /**
     * @return the noOfEmergencyFixtures
     */
    /**
     * @return the emCommissionedDate
     */
    @Column(name = "no_of_emergency_fixtures")
    @XmlElement(name = "noOfEmergencyFixtures")
    public Integer getNoOfEmergencyFixtures() {
        return noOfEmergencyFixtures;
    }

    /**
     * @param noOfEmergencyFixtures the noOfEmergencyFixtures to set
     */
    public void setNoOfEmergencyFixtures(Integer noOfEmergencyFixtures) {
        this.noOfEmergencyFixtures = noOfEmergencyFixtures;
    }

    /**
     * @return the emergencyFixturesGuidelineLoad
     */
    /**
     * @return the emCommissionedDate
     */
    @Column(name = "emergency_fixtures_guideline_load")
    @XmlElement(name = "emergencyFixturesGuidelineLoad")
    public BigDecimal getEmergencyFixturesGuidelineLoad() {
        return emergencyFixturesGuidelineLoad;
    }

    /**
     * @param emergencyFixturesGuidelineLoad the emergencyFixturesGuidelineLoad to set
     */
    public void setEmergencyFixturesGuidelineLoad(BigDecimal emergencyFixturesGuidelineLoad) {
        this.emergencyFixturesGuidelineLoad = emergencyFixturesGuidelineLoad;
    }

    /**
     * @return the emergencyFixturesLoad
     */
    /**
     * @return the emCommissionedDate
     */
    @Column(name = "emergency_fixtures_load")
    @XmlElement(name = "emergencyFixturesLoad")
    public BigDecimal getEmergencyFixturesLoad() {
        return emergencyFixturesLoad;
    }

    /**
     * @param emergencyFixturesLoad the emergencyFixturesLoad to set
     */
    public void setEmergencyFixturesLoad(BigDecimal emergencyFixturesLoad) {
        this.emergencyFixturesLoad = emergencyFixturesLoad;
    }
    
    
    /**
	 * @return the secretKey
	 */
	@Column(name = "secret_key")
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @param secretKey the secretKey to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	
	@Column(name = "ip_address")
	@XmlElement(name = "ipAddress")
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}	

	
	@Column(name = "api_key")	
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	
	public void setNoOfFloors(Integer noOfFloors) {
		this.noOfFloors = noOfFloors;
	}

	@Column(name = "no_of_floors")
	@XmlElement(name = "noOfFloors")
	public Integer getNoOfFloors() {
		return noOfFloors;
	}

	
	public void setNoOfMappedFloors(Integer noOfMappedFloors) {
		this.noOfMappedFloors = noOfMappedFloors;
	}
	
	@Transient
	@XmlElement(name = "noOfMappedFloors")
	public Integer getNoOfMappedFloors() {
		return noOfMappedFloors;
	}

	@Column(name = "last_successful_sync_time")
	@XmlElement(name = "lastSuccessfulSyncTime")
	public Date getLastSuccessfulSyncTime() {
		return lastSuccessfulSyncTime;
	}

	public void setLastSuccessfulSyncTime(Date lastSuccessfulSyncTime) {
		this.lastSuccessfulSyncTime = lastSuccessfulSyncTime;
	}
	
	@Column(name = "pause_sync")
	@XmlElement(name = "pauseSyncStatus")
	public Boolean getPauseSyncStatus(){
		return pauseSyncStatus;
	}
	
	public void setPauseSyncStatus(Boolean pauseSyncStatus){
		this.pauseSyncStatus = pauseSyncStatus;
	}
	
	@Column(name = "em_state")
	@XmlElement(name = "emState")
	public long getEmState() {
		return emState;
	}
	
	public void setEmState(long emState) {
		this.emState = emState;
	}

	@Column(name = "em_os_arch")
	@XmlElement(name = "em_os_arch")
	public String getOsArch() {
		return osArch;
	}

	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}

	@Column(name = "em_os_version")
	@XmlElement(name = "em_os_version")
	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	
}
