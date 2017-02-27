package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sharad.Mahajan
 *
 */

@Entity
@Table(name = "bill_payments", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BillPayments implements Serializable {
	private Long id;
	private Customer customer;
	private Date paymentDate;
	private Double paymentAmount;
	public BillPayments() {
		// TODO Auto-generated constructor stub
	}
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="bill_payments_seq")
	@SequenceGenerator(name="bill_payments_seq", sequenceName="bill_payments_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@XmlElement(name = "customer")
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	@Column(name = "payment_date")
	@XmlElement(name = "paymentDate")
	public Date getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}
	@Column(name = "payment_amount")
	@XmlElement(name = "paymentAmount")
	public Double getPaymentAmount() {
		return paymentAmount;
	}
	public void setPaymentAmount(Double paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	
}
