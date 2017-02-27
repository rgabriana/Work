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
@Table(name = "cloud_campus", schema = "public")
public class CloudCampus implements java.io.Serializable {

	private long id;
	private Customer customer;
	private String name;
	private String location;
	private String zipcode;
	private Set<CloudBuilding> cloudBuildings = new HashSet<CloudBuilding>(0);
	private Set<EmCampus> emCampuses = new HashSet<EmCampus>(0);

	public CloudCampus() {
	}

	public CloudCampus(long id, Customer customer, String name) {
		this.id = id;
		this.customer = customer;
		this.name = name;
	}

	public CloudCampus(long id, Customer customer, String name,
			String location, String zipcode, Set<CloudBuilding> cloudBuildings,
			Set<EmCampus> emCampuses) {
		this.id = id;
		this.customer = customer;
		this.name = name;
		this.location = location;
		this.zipcode = zipcode;
		this.cloudBuildings = cloudBuildings;
		this.emCampuses = emCampuses;
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
	@JoinColumn(name = "customer_id", nullable = false)
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Column(name = "name", nullable = false, length = 256)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "location", length = 256)
	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Column(name = "zipcode", length = 16)
	public String getZipcode() {
		return this.zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "cloudCampus")
	public Set<CloudBuilding> getCloudBuildings() {
		return this.cloudBuildings;
	}

	public void setCloudBuildings(Set<CloudBuilding> cloudBuildings) {
		this.cloudBuildings = cloudBuildings;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "cloudCampus")
	public Set<EmCampus> getEmCampuses() {
		return this.emCampuses;
	}

	public void setEmCampuses(Set<EmCampus> emCampuses) {
		this.emCampuses = emCampuses;
	}
	
	 public List<CloudBuilding> getBuildingsList(Set<CloudBuilding> setbuildings){       
        List<CloudBuilding> list = new ArrayList<CloudBuilding>();    
        if(setbuildings!=null)
        {
         	list.addAll(setbuildings);
        }
        return list;
	}

}
