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
@Table(name = "em_campus", schema = "public")
public class EmCampus implements java.io.Serializable {

	private long id;
	private CloudCampus cloudCampus;
	private EmInstance emInstance;
	private Long emRemoteId;
	private String name;
	private String location;
	private String zipcode;
	private Set<EmBuilding> emBuildings = new HashSet<EmBuilding>(0);

	public EmCampus() {
	}

	public EmCampus(long id, EmInstance emInstance, String name) {
		this.id = id;
		this.emInstance = emInstance;
		this.name = name;
	}

	public EmCampus(long id, CloudCampus cloudCampus, EmInstance emInstance,
			Long emRemoteId, String name, String location, String zipcode,
			Set<EmBuilding> emBuildings) {
		this.id = id;
		this.cloudCampus = cloudCampus;
		this.emInstance = emInstance;
		this.emRemoteId = emRemoteId;
		this.name = name;
		this.location = location;
		this.zipcode = zipcode;
		this.emBuildings = emBuildings;
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
	@JoinColumn(name = "cloud_campus_id")
	public CloudCampus getCloudCampus() {
		return this.cloudCampus;
	}

	public void setCloudCampus(CloudCampus cloudCampus) {
		this.cloudCampus = cloudCampus;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "em_instance_id", nullable = false)
	public EmInstance getEmInstance() {
		return this.emInstance;
	}

	public void setEmInstance(EmInstance emInstance) {
		this.emInstance = emInstance;
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

	@Column(name = "location", length = 256)
	@XmlElement(name = "location")
	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Column(name = "zipcode", length = 16)
	@XmlElement(name = "zipcode")
	public String getZipcode() {
		return this.zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "emCampus")
	public Set<EmBuilding> getEmBuildings() {
		return this.emBuildings;
	}

	public void setEmBuildings(Set<EmBuilding> emBuildings) {
		this.emBuildings = emBuildings;
	}

}
