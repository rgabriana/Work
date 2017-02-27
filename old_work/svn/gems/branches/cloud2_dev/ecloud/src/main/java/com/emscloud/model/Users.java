package com.emscloud.model;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.types.Status;

@Entity
@Table(name = "users", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Users implements java.io.Serializable {

	@XmlElement(name = "id")
	private Long id;
	private Customer customer;
	@XmlElement(name = "email")
	private String email;
	private String password;
	private String salt;
	@XmlElement(name = "firstname")
	private String firstName;
	@XmlElement(name = "lastname")
	private String lastName;
	private Date createdOn;
	@XmlElement(name = "roletype")
	private String roleType;
	@XmlElement(name = "status")
	private Status status;	


	public Users() {
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="users_seq")
    @SequenceGenerator(name="users_seq", sequenceName="users_seq")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Column(name = "email", nullable = false)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "password", nullable = false)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Column(name = "salt", nullable = false)
	public String getSalt() {
		return salt;
	}


	public void setSalt(String salt) {
		this.salt = salt;
	}


	@Column(name = "first_name")
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "last_name")
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "created_on", length = 13)
	public Date getCreatedOn() {
		return this.createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@Column(name = "role_type")
	public String getRoleType() {
		return this.roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "char")
	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

/*	public void setUserCustomers(Set<UserCustomers> userCustomers) {
		this.userCustomers = userCustomers;
	}	
	
	@OneToMany(fetch = FetchType.LAZY)	
	@JoinColumn(name = "id")
	public Set<UserCustomers> getUserCustomers() {
		return userCustomers;
	}*/	
}
