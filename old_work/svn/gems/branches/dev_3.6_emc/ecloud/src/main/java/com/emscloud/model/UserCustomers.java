package com.emscloud.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="user_customers",schema="public")
public class UserCustomers implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;		
	private Users user;
	private Customer customer;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_customers_seq")
    @SequenceGenerator(name="user_customers_seq", sequenceName="user_customers_seq")
	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public UserCustomers()
	{
		
	}	
	
	public void setId(long id) {
		this.id = id;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	public Customer getCustomer() {
		return customer;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public Users getUser() {
		return user;
	}

}
