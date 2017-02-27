package com.emscloud.model;



import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "gateway", schema = "public")
public class Gateway implements java.io.Serializable {

	private long id;
	private Device device;
	private String ipAddress;
	private String snapAddress;
	private Integer noOfCommissionedSensors;
	private String bootLoaderVersion;
	private String firmwareVersion;
	private Set<Fixture> fixtures = new HashSet<Fixture>(0);

	public Gateway() {
	}

	public Gateway(Device device) {
		this.device = device;
	}

	public Gateway(Device device, String ipAddress, String snapAddress,
			Integer noOfCommissionedSensors, String bootLoaderVersion,
			String firmwareVersion, Set<Fixture> fixtures) {
		this.device = device;
		this.ipAddress = ipAddress;
		this.snapAddress = snapAddress;
		this.noOfCommissionedSensors = noOfCommissionedSensors;
		this.bootLoaderVersion = bootLoaderVersion;
		this.firmwareVersion = firmwareVersion;
		this.fixtures = fixtures;
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

	@Column(name = "ip_address")
	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "snap_address")
	public String getSnapAddress() {
		return this.snapAddress;
	}

	public void setSnapAddress(String snapAddress) {
		this.snapAddress = snapAddress;
	}

	@Column(name = "no_of_commissioned_sensors")
	public Integer getNoOfCommissionedSensors() {
		return this.noOfCommissionedSensors;
	}

	public void setNoOfCommissionedSensors(Integer noOfCommissionedSensors) {
		this.noOfCommissionedSensors = noOfCommissionedSensors;
	}

	@Column(name = "boot_loader_version", length = 50)
	public String getBootLoaderVersion() {
		return this.bootLoaderVersion;
	}

	public void setBootLoaderVersion(String bootLoaderVersion) {
		this.bootLoaderVersion = bootLoaderVersion;
	}

	@Column(name = "firmware_version", length = 50)
	public String getFirmwareVersion() {
		return this.firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "gateway")
	public Set<Fixture> getFixtures() {
		return this.fixtures;
	}

	public void setFixtures(Set<Fixture> fixtures) {
		this.fixtures = fixtures;
	}

}
