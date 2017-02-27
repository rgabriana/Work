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
import javax.xml.bind.annotation.XmlElement;


@Entity
@Table(name = "em_floor", schema = "public")
public class EmFloor implements java.io.Serializable {

	private long id;
	private CloudFloor cloudFloor;
	private EmBuilding emBuilding;
	private Long emRemoteId;
	private String name;
	private String description;
	private String floorplanUrl;
	private Long planMapId;
	private Integer noInstalledSensors;
	private Integer noInstalledFixtures;
	private Date floorPlanUploadedTime;
	private Set<Device> devices = new HashSet<Device>(0);

	public EmFloor() {
	}

	public EmFloor(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public EmFloor(long id, CloudFloor cloudFloor, EmBuilding emBuilding,
			Long emRemoteId, String name, String description,
			String floorplanUrl, Long planMapId, Integer noInstalledSensors,
			Integer noInstalledFixtures, Date floorPlanUploadedTime,
			Set<Device> devices) {
		this.id = id;
		this.cloudFloor = cloudFloor;
		this.emBuilding = emBuilding;
		this.emRemoteId = emRemoteId;
		this.name = name;
		this.description = description;
		this.floorplanUrl = floorplanUrl;
		this.planMapId = planMapId;
		this.noInstalledSensors = noInstalledSensors;
		this.noInstalledFixtures = noInstalledFixtures;
		this.floorPlanUploadedTime = floorPlanUploadedTime;
		this.devices = devices;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cloud_floor_id")
	public CloudFloor getCloudFloor() {
		return this.cloudFloor;
	}

	public void setCloudFloor(CloudFloor cloudFloor) {
		this.cloudFloor = cloudFloor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "em_building_id")
	public EmBuilding getEmBuilding() {
		return this.emBuilding;
	}

	public void setEmBuilding(EmBuilding emBuilding) {
		this.emBuilding = emBuilding;
	}

	@Column(name = "em_remote_id")
	@XmlElement(name = "emRemoteId")
	public Long getEmRemoteId() {
		return this.emRemoteId;
	}

	public void setEmRemoteId(Long emRemoteId) {
		this.emRemoteId = emRemoteId;
	}

	@Column(name = "name", nullable = false, length = 128)
	@XmlElement(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", length = 512)
	@XmlElement(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "floorplan_url")
	@XmlElement(name = "floorplanUrl")
	public String getFloorplanUrl() {
		return this.floorplanUrl;
	}

	public void setFloorplanUrl(String floorplanUrl) {
		this.floorplanUrl = floorplanUrl;
	}

	@Column(name = "plan_map_id")
	@XmlElement(name = "planMapId")
	public Long getPlanMapId() {
		return this.planMapId;
	}

	public void setPlanMapId(Long planMapId) {
		this.planMapId = planMapId;
	}

	@Column(name = "no_installed_sensors")
	@XmlElement(name = "noInstalledSensors")
	public Integer getNoInstalledSensors() {
		return this.noInstalledSensors;
	}

	public void setNoInstalledSensors(Integer noInstalledSensors) {
		this.noInstalledSensors = noInstalledSensors;
	}

	@Column(name = "no_installed_fixtures")
	@XmlElement(name = "noInstalledFixtures")
	public Integer getNoInstalledFixtures() {
		return this.noInstalledFixtures;
	}

	public void setNoInstalledFixtures(Integer noInstalledFixtures) {
		this.noInstalledFixtures = noInstalledFixtures;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "floor_plan_uploaded_time", length = 29)
	@XmlElement(name = "floorPlanUploadedTime")
	public Date getFloorPlanUploadedTime() {
		return this.floorPlanUploadedTime;
	}

	public void setFloorPlanUploadedTime(Date floorPlanUploadedTime) {
		this.floorPlanUploadedTime = floorPlanUploadedTime;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "emFloor")
	public Set<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}

}
