package com.enlightedinc.vo;

import java.util.Date;

public class BacnetLicenseInstance {
	
	private int noofdevices;
	
	private Date timeStamp;
	
	private String productId;
	
	private int noOfEmBaseLicenses;
	
	private String emBaseLicenseProductId;
	
	private int noOfEmGroupPointBaseLicenses;
	
	private String emGroupPointLicenseProductId;
	
	private int noOfEmSensorPointBaseLicenses;
	
	private String emSensorPointLicenseProductId;

	public void setNoofdevices(int noofdevices) {
		this.noofdevices = noofdevices;
	}
	
	public int getNoofdevices() {
		return noofdevices;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductId() {
		return productId;
	}
	
	public int getNoOfEmBaseLicenses() {
		return noOfEmBaseLicenses;
	}

	public void setNoOfEmBaseLicenses(int noOfEmBaseLicenses) {
		this.noOfEmBaseLicenses = noOfEmBaseLicenses;
	}

	public String getEmBaseLicenseProductId() {
		return emBaseLicenseProductId;
	}

	public void setEmBaseLicenseProductId(String emBaseLicenseProductId) {
		this.emBaseLicenseProductId = emBaseLicenseProductId;
	}
	
	public int getNoOfEmGroupPointBaseLicenses() {
		return noOfEmGroupPointBaseLicenses;
	}

	public void setNoOfEmGroupPointBaseLicenses(int noOfEmGroupPointBaseLicenses) {
		this.noOfEmGroupPointBaseLicenses = noOfEmGroupPointBaseLicenses;
	}

	public String getEmGroupPointLicenseProductId() {
		return emGroupPointLicenseProductId;
	}

	public void setEmGroupPointLicenseProductId(String emGroupPointLicenseProductId) {
		this.emGroupPointLicenseProductId = emGroupPointLicenseProductId;
	}

	public int getNoOfEmSensorPointBaseLicenses() {
		return noOfEmSensorPointBaseLicenses;
	}

	public void setNoOfEmSensorPointBaseLicenses(int noOfEmSensorPointBaseLicenses) {
		this.noOfEmSensorPointBaseLicenses = noOfEmSensorPointBaseLicenses;
	}

	public String getEmSensorPointLicenseProductId() {
		return emSensorPointLicenseProductId;
	}

	public void setEmSensorPointLicenseProductId(
			String emSensorPointLicenseProductId) {
		this.emSensorPointLicenseProductId = emSensorPointLicenseProductId;
	}
}
