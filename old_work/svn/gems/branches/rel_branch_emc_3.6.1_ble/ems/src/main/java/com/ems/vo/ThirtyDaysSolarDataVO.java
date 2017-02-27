/**
 * 
 */
package com.ems.vo;

import java.util.Date;

/**
 * @author enlighted
 *
 */
public class ThirtyDaysSolarDataVO {
	private Date startDate;
	private SolarDataVO[] thirtyDaysData;
	
	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the thirtyDayData
	 */
	public SolarDataVO[] getThirtysDayData() {
		return thirtyDaysData;
	}
	/**
	 * @param thirtyDayData the thirtyDayData to set
	 */
	public void setThirtyDaysData(SolarDataVO[] thirtyDayData) {
		this.thirtyDaysData = thirtyDayData;
	}

}
