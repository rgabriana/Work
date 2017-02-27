package com.emscloud.model;



import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "customer", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Customer implements java.io.Serializable {

	private long id;
	private String name;
	private String address;
	private String email;
	private String contact;
	private Set<EmInstance> emInstances = new HashSet<EmInstance>(0);
	public Customer() {
	}

	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="customer_seq")
    @SequenceGenerator(name="customer_seq", sequenceName="customer_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "name", nullable = false)
	 @XmlElement(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "address", nullable = false)
	 @XmlElement(name = "address")
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "email")
	 @XmlElement(name = "email")
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "phone")
	@XmlElement(name = "contact")
	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	public Set<EmInstance> getEmInstances() {
		return this.emInstances;
	}

	public void setEmInstances(Set<EmInstance> emInstances) {
		this.emInstances = emInstances;
	}

	

}
