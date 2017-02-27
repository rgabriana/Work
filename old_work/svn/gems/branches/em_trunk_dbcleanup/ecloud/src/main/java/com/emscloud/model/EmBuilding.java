package com.emscloud.model;


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
import javax.xml.bind.annotation.XmlElement;

@Entity
@Table(name = "em_building", schema = "public")
public class EmBuilding implements java.io.Serializable {

	private long id;
	private CloudBuilding cloudBuilding;
	private EmCampus emCampus;
	private Long emRemoteId;
	private String name;
	private Set<EmFloor> emFloors = new HashSet<EmFloor>(0);

	public EmBuilding() {
	}

	public EmBuilding(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public EmBuilding(long id, CloudBuilding cloudBuilding, EmCampus emCampus,
			Long emRemoteId, String name, Set<EmFloor> emFloors) {
		this.id = id;
		this.cloudBuilding = cloudBuilding;
		this.emCampus = emCampus;
		this.emRemoteId = emRemoteId;
		this.name = name;
		this.emFloors = emFloors;
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
	@JoinColumn(name = "cloud_building_id")
	public CloudBuilding getCloudBuilding() {
		return this.cloudBuilding;
	}

	public void setCloudBuilding(CloudBuilding cloudBuilding) {
		this.cloudBuilding = cloudBuilding;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "em_campus_id")
	public EmCampus getEmCampus() {
		return this.emCampus;
	}

	public void setEmCampus(EmCampus emCampus) {
		this.emCampus = emCampus;
	}

	@Column(name = "em_remote_id")
	@XmlElement(name = "emRemoteId")
	public Long getEmRemoteId() {
		return this.emRemoteId;
	}

	public void setEmRemoteId(Long emRemoteId) {
		this.emRemoteId = emRemoteId;
	}

	@Column(name = "name", nullable = false, length = 256)
	@XmlElement(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "emBuilding")
	public Set<EmFloor> getEmFloors() {
		return this.emFloors;
	}

	public void setEmFloors(Set<EmFloor> emFloors) {
		this.emFloors = emFloors;
	}

}
