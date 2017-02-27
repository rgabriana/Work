package com.emscloud.model;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "device", schema = "public")
public class Device implements java.io.Serializable {

	private long id;
	private CloudFloor cloudFloor;
	private EmFloor emFloor;
	private long emLocalId;
	private String name;
	private long emInstanceFacilityId;
	private String deviceType;
	private Long x;
	private Long y;
	private String macAddress;
	private String deviceVersion;
	private Gateway gateway;
	private Fixture fixture;

	public Device() {
	}

	public Device(long id, CloudFloor cloudFloor, EmFloor emFloor,
			long emLocalId, String name, long emInstanceFacilityId) {
		this.id = id;
		this.cloudFloor = cloudFloor;
		this.emFloor = emFloor;
		this.emLocalId = emLocalId;
		this.name = name;
		this.emInstanceFacilityId = emInstanceFacilityId;
	}

	public Device(long id, CloudFloor cloudFloor, EmFloor emFloor,
			long emLocalId, String name, long emInstanceFacilityId,
			String deviceType, Long x, Long y, String macAddress,
			String deviceVersion, Gateway gateway, Fixture fixture) {
		this.id = id;
		this.cloudFloor = cloudFloor;
		this.emFloor = emFloor;
		this.emLocalId = emLocalId;
		this.name = name;
		this.emInstanceFacilityId = emInstanceFacilityId;
		this.deviceType = deviceType;
		this.x = x;
		this.y = y;
		this.macAddress = macAddress;
		this.deviceVersion = deviceVersion;
		this.gateway = gateway;
		this.fixture = fixture;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cloud_floor_id", nullable = false)
	public CloudFloor getCloudFloor() {
		return this.cloudFloor;
	}

	public void setCloudFloor(CloudFloor cloudFloor) {
		this.cloudFloor = cloudFloor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "em_floor_id", nullable = false)
	public EmFloor getEmFloor() {
		return this.emFloor;
	}

	public void setEmFloor(EmFloor emFloor) {
		this.emFloor = emFloor;
	}

	@Column(name = "em_local_id", nullable = false)
	public long getEmLocalId() {
		return this.emLocalId;
	}

	public void setEmLocalId(long emLocalId) {
		this.emLocalId = emLocalId;
	}

	@Column(name = "name", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "em_instance_facility_id", nullable = false)
	public long getEmInstanceFacilityId() {
		return this.emInstanceFacilityId;
	}

	public void setEmInstanceFacilityId(long emInstanceFacilityId) {
		this.emInstanceFacilityId = emInstanceFacilityId;
	}

	@Column(name = "device_type")
	public String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name = "x")
	public Long getX() {
		return this.x;
	}

	public void setX(Long x) {
		this.x = x;
	}

	@Column(name = "y")
	public Long getY() {
		return this.y;
	}

	public void setY(Long y) {
		this.y = y;
	}

	@Column(name = "mac_address")
	public String getMacAddress() {
		return this.macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	@Column(name = "device_version")
	public String getDeviceVersion() {
		return this.deviceVersion;
	}

	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = deviceVersion;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "device")
	public Gateway getGateway() {
		return this.gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "device")
	public Fixture getFixture() {
		return this.fixture;
	}

	public void setFixture(Fixture fixture) {
		this.fixture = fixture;
	}

}
