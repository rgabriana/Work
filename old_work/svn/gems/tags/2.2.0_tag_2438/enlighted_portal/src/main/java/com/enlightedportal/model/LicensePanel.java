package com.enlightedportal.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LicensePanel {
	

	 @XmlElement(name = "customer")
	 String customer ;
	 @XmlElement(name = "macid")
	 String macId ;
	 
	 @XmlElement(name = "startdate")
	 String StartDate ;
	 
	 @XmlElement(name = "enddate")
	 String endDate ;
	 
	 @XmlElement(name = "downloadrestpath")
	 String downloadRestPath ;
	 

	public String getMacId() {
		return macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getStartDate() {
		return StartDate;
	}

	public void setStartDate(String startDate) {
		StartDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getDownloadRestPath() {
		return downloadRestPath;
	}

	public void setDownloadRestPath(String downloadRestPath) {
		this.downloadRestPath = downloadRestPath;
	}



	
	 
	
	
	 
	 

}
