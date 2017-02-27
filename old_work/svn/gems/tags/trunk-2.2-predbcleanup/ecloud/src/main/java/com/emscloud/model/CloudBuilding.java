package com.emscloud.model;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "cloud_building", schema = "public")
public class CloudBuilding implements java.io.Serializable {

	private long id;
	private CloudCampus cloudCampus;
	private String name;
	private Set<EmBuilding> emBuildings = new HashSet<EmBuilding>(0);
	private Set<CloudFloor> cloudFloors = new HashSet<CloudFloor>(0);

	public CloudBuilding() {
	}

	public CloudBuilding(long id, CloudCampus cloudCampus, String name) {
		this.id = id;
		this.cloudCampus = cloudCampus;
		this.name = name;
	}

	public CloudBuilding(long id, CloudCampus cloudCampus, String name,
			Set<EmBuilding> emBuildings, Set<CloudFloor> cloudFloors) {
		this.id = id;
		this.cloudCampus = cloudCampus;
		this.name = name;
		this.emBuildings = emBuildings;
		this.cloudFloors = cloudFloors;
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
	@JoinColumn(name = "cloud_campus_id", nullable = false)
	public CloudCampus getCloudCampus() {
		return this.cloudCampus;
	}

	public void setCloudCampus(CloudCampus cloudCampus) {
		this.cloudCampus = cloudCampus;
	}

	@Column(name = "name", nullable = false, length = 256)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "cloudBuilding")
	public Set<EmBuilding> getEmBuildings() {
		return this.emBuildings;
	}

	public void setEmBuildings(Set<EmBuilding> emBuildings) {
		this.emBuildings = emBuildings;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "cloudBuilding")
	public Set<CloudFloor> getCloudFloors() {
		return this.cloudFloors;
	}

	public void setCloudFloors(Set<CloudFloor> cloudFloors) {
		this.cloudFloors = cloudFloors;
	}
	public List<CloudFloor> getFloorsList(Set<CloudFloor> setfloors){       
        List<CloudFloor> list = new ArrayList<CloudFloor>();  
        if(setfloors!=null)
        {
         	list.addAll(setfloors);
        }
        return list;
	}

}
