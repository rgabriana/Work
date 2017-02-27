package com.emscloud.model;



import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "cloud_floor", schema = "public")
public class CloudFloor implements java.io.Serializable {

	private long id;
	private CloudBuilding cloudBuilding;
	private String name;
	private String description;
	private String floorplanUrl;
	private Long planMapId;
	private Integer noInstalledSensors;
	private Integer noInstalledFixtures;
	private Date floorPlanUploadedTime;
	private Set<EmFloor> emFloors = new HashSet<EmFloor>(0);
	private Set<Device> devices = new HashSet<Device>(0);

	public CloudFloor() {
	}

	public CloudFloor(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public CloudFloor(long id, CloudBuilding cloudBuilding, String name,
			String description, String floorplanUrl, Long planMapId,
			Integer noInstalledSensors, Integer noInstalledFixtures,
			Date floorPlanUploadedTime, Set<EmFloor> emFloors,
			Set<Device> devices) {
		this.id = id;
		this.cloudBuilding = cloudBuilding;
		this.name = name;
		this.description = description;
		this.floorplanUrl = floorplanUrl;
		this.planMapId = planMapId;
		this.noInstalledSensors = noInstalledSensors;
		this.noInstalledFixtures = noInstalledFixtures;
		this.floorPlanUploadedTime = floorPlanUploadedTime;
		this.emFloors = emFloors;
		this.devices = devices;
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
	@JoinColumn(name = "cloud_building_id")
	public CloudBuilding getCloudBuilding() {
		return this.cloudBuilding;
	}

	public void setCloudBuilding(CloudBuilding cloudBuilding) {
		this.cloudBuilding = cloudBuilding;
	}

	@Column(name = "name", nullable = false, length = 128)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", length = 512)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "floorplan_url")
	public String getFloorplanUrl() {
		return this.floorplanUrl;
	}

	public void setFloorplanUrl(String floorplanUrl) {
		this.floorplanUrl = floorplanUrl;
	}

	@Column(name = "plan_map_id")
	public Long getPlanMapId() {
		return this.planMapId;
	}

	public void setPlanMapId(Long planMapId) {
		this.planMapId = planMapId;
	}

	@Column(name = "no_installed_sensors")
	public Integer getNoInstalledSensors() {
		return this.noInstalledSensors;
	}

	public void setNoInstalledSensors(Integer noInstalledSensors) {
		this.noInstalledSensors = noInstalledSensors;
	}

	@Column(name = "no_installed_fixtures")
	public Integer getNoInstalledFixtures() {
		return this.noInstalledFixtures;
	}

	public void setNoInstalledFixtures(Integer noInstalledFixtures) {
		this.noInstalledFixtures = noInstalledFixtures;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "floor_plan_uploaded_time", length = 29)
	public Date getFloorPlanUploadedTime() {
		return this.floorPlanUploadedTime;
	}

	public void setFloorPlanUploadedTime(Date floorPlanUploadedTime) {
		this.floorPlanUploadedTime = floorPlanUploadedTime;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "cloudFloor")
	public Set<EmFloor> getEmFloors() {
		return this.emFloors;
	}

	public void setEmFloors(Set<EmFloor> emFloors) {
		this.emFloors = emFloors;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "cloudFloor")
	public Set<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}

}
