package com.enlightedinc.vo;

import java.util.Date;

public class OccupancySensor {
	private Boolean enabled;
	
	private Date timeStamp;

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	
}