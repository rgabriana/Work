package com.emscloud.model;



import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "fixture", schema = "public")
public class Fixture implements java.io.Serializable {

	private long id;
	private Device device;
	private Gateway gateway;
	private String snapAddress;
	private String firmwareVersion;
	private String bootloaderVersion;
	private String state;
	private String cuVersion;
	private Integer versionSynced;
	private Date bulbsLastServiceDate;
	private Date ballastLastServiceDate;
	private String ipAddress;
	private BigDecimal baselinePower;

	public Fixture() {
	}

	public Fixture(Device device, Gateway gateway) {
		this.device = device;
		this.gateway = gateway;
	}

	public Fixture(Device device, Gateway gateway, String snapAddress,
			String firmwareVersion, String bootloaderVersion, String state,
			String cuVersion, Integer versionSynced, Date bulbsLastServiceDate,
			Date ballastLastServiceDate, String ipAddress,
			BigDecimal baselinePower) {
		this.device = device;
		this.gateway = gateway;
		this.snapAddress = snapAddress;
		this.firmwareVersion = firmwareVersion;
		this.bootloaderVersion = bootloaderVersion;
		this.state = state;
		this.cuVersion = cuVersion;
		this.versionSynced = versionSynced;
		this.bulbsLastServiceDate = bulbsLastServiceDate;
		this.ballastLastServiceDate = ballastLastServiceDate;
		this.ipAddress = ipAddress;
		this.baselinePower = baselinePower;
	}

	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "device"))
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gateway_id", nullable = false)
	public Gateway getGateway() {
		return this.gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	@Column(name = "snap_address")
	public String getSnapAddress() {
		return this.snapAddress;
	}

	public void setSnapAddress(String snapAddress) {
		this.snapAddress = snapAddress;
	}

	@Column(name = "firmware_version", length = 20)
	public String getFirmwareVersion() {
		return this.firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	@Column(name = "bootloader_version", length = 20)
	public String getBootloaderVersion() {
		return this.bootloaderVersion;
	}

	public void setBootloaderVersion(String bootloaderVersion) {
		this.bootloaderVersion = bootloaderVersion;
	}

	@Column(name = "state")
	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Column(name = "cu_version", length = 20)
	public String getCuVersion() {
		return this.cuVersion;
	}

	public void setCuVersion(String cuVersion) {
		this.cuVersion = cuVersion;
	}

	@Column(name = "version_synced")
	public Integer getVersionSynced() {
		return this.versionSynced;
	}

	public void setVersionSynced(Integer versionSynced) {
		this.versionSynced = versionSynced;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "bulbs_last_service_date", length = 13)
	public Date getBulbsLastServiceDate() {
		return this.bulbsLastServiceDate;
	}

	public void setBulbsLastServiceDate(Date bulbsLastServiceDate) {
		this.bulbsLastServiceDate = bulbsLastServiceDate;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "ballast_last_service_date", length = 13)
	public Date getBallastLastServiceDate() {
		return this.ballastLastServiceDate;
	}

	public void setBallastLastServiceDate(Date ballastLastServiceDate) {
		this.ballastLastServiceDate = ballastLastServiceDate;
	}

	@Column(name = "ip_address")
	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "baseline_power")
	public BigDecimal getBaselinePower() {
		return this.baselinePower;
	}

	public void setBaselinePower(BigDecimal baselinePower) {
		this.baselinePower = baselinePower;
	}

}
