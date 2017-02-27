package com.enlightedportal.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "customer")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Customer implements Serializable {
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="customer_seq")
    @SequenceGenerator(name="customer_seq", sequenceName="customer_seq")

    @Column(name = "id")
	 @XmlElement(name = "id")
    private Long id;

    @Column(name = "name")
    @XmlElement(name = "name")
    private String name;

    @Column(name = "address")
    @XmlElement(name = "address")
    private String address;
    
    @Column(name = "email")
    @XmlElement(name = "email")
    private String email;
    
    @Column(name = "contact")
    @XmlElement(name = "contact")
    private String contact;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

   
}
