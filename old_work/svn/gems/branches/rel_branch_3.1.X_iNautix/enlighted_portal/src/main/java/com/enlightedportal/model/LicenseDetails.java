package com.enlightedportal.model;




import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "license_detail")
public class LicenseDetails {

	@Id
	 @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="license_detail_seq")
    @SequenceGenerator(name="license_detail_seq", sequenceName="license_detail_seq")
    @Column(name = "id")
    private Long id;
	
	@Column(name = "customer_id")
	  private Long customerId;
	 
    @Column(name = "level")
	    private String level;
    
    @Column(name = "start_date")
    private String startDate;
    
    @Column(name = "end_date")
    private String endDate;
    
    @Column(name = "created_on")
    private Date createdOn;
    
    @Column(name = "mac_id")
    private String macId;
    
    @Column(name = "api_key")
    private byte[] apiKey;
    
    @Column(name = "status")
    private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getMacId() {
		return macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}

	public byte[] getApiKey() {
		return apiKey;
	}

	public void setApiKey(byte[] apiKey) {
		this.apiKey = apiKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
	
}
