package com.enlightedinc.vo;

import java.util.Date;

public class EmLicenseInstance {
	
	private int noofdevices;
	
	private Date timeStamp;
	
	private String productId;

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
  
}
