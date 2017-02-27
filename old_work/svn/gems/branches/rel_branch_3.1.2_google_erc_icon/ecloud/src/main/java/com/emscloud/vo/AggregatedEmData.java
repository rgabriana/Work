package com.emscloud.vo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AggregatedEmData {
	
	private static SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
	private static SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@XmlElement(name = "siteName")
	private String siteName;
	
	@XmlElement(name = "emName")
	private String emName;
	
	@XmlElement(name = "date")
	private Date date;	
		
	@XmlElement(name = "weekday")
	private Boolean weekday;
					
	@XmlElement(name = "avgTemp")
	private Float avgTemp;
			
	@XmlElement(name = "energy")
	private BigDecimal energy;
	
	@XmlElement(name = "savedEnergy")
	private BigDecimal savedEnergy;
	
	@XmlElement(name = "baseEnergy")
	private BigDecimal baseEnergy;
	
	@XmlElement(name = "savedAmbEnergy")
	private BigDecimal savedAmbEnergy;
	
	@XmlElement(name = "savedOccEnergy")
	private BigDecimal savedOccEnergy;
	
	@XmlElement(name = "savedTaskTunedEnergy")
	private BigDecimal savedTaskTunedEnergy;
		
	/**
	 * @return the siteName
	 */
	public String getSiteName() {
		return siteName;
	}
	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	
	/**
	 * @return the emName
	 */
	public String getEmName() {
		return emName;
	}
	/**
	 * @param emName the emName to set
	 */
	public void setEmName(String emName) {
		this.emName = emName;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return fullDateFormat.format(date);
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setDate(Date date) {
		this.date = date;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);		
		String dayName = dayNameFormat.format(cal.getTime());
		if(dayName.compareTo("Sat") == 0 || dayName.compareTo("Sun") == 0) {
			weekday = false;
		} else {
			weekday = true;
		}
		
	} //end of method setTimestamp
		
	/**
	 * @return the weekday
	 */
	public Boolean getWeekday() {
		return weekday;
	}
	/**
	 * @param weekday the weekday to set
	 */
	public void setWeekday(Boolean weekday) {
		this.weekday = weekday;
	}
		
	/**
	 * @return the avgTemp
	 */
	public Float getAvgTemp() {
		return avgTemp;
	}
	/**
	 * @param avgTemp the avgTemp to set
	 */
	public void setAvgTemp(Float avgTemp) {
		this.avgTemp = avgTemp;
	}

	/**
	 * @return the energy
	 */
	public BigDecimal getEnergy() {
		return energy;
	}
	/**
	 * @param energy the energy to set
	 */
	public void setEnergy(BigDecimal energy) {
		this.energy = energy;
	}
	/**
	 * @return the baseEnergy
	 */
	public BigDecimal getBaseEnergy() {
		return baseEnergy;
	}
	/**
	 * @param baseEnergy the baseEnergy to set
	 */
	public void setBaseEnergy(BigDecimal baseEnergy) {
		this.baseEnergy = baseEnergy;
	}
	/**
	 * @return the savedAmbEnergy
	 */
	public BigDecimal getSavedAmbEnergy() {
		return savedAmbEnergy;
	}
	/**
	 * @param savedAmbEnergy the savedAmbEnergy to set
	 */
	public void setSavedAmbEnergy(BigDecimal savedAmbEnergy) {
		this.savedAmbEnergy = savedAmbEnergy;
	}
	
	/**
	 * @return the savedEnergy
	 */
	public BigDecimal getSavedEnergy() {
		return savedEnergy;
	}
	/**
	 * @param savedEnergy the savedEnergy to set
	 */
	public void setSavedEnergy(BigDecimal savedEnergy) {
		this.savedEnergy = savedEnergy;
	}
	
	/**
	 * @return the savedOccEnergy
	 */
	public BigDecimal getSavedOccEnergy() {
		return savedOccEnergy;
	}
	/**
	 * @param savedOccEnergy the savedOccEnergy to set
	 */
	public void setSavedOccEnergy(BigDecimal savedOccEnergy) {
		this.savedOccEnergy = savedOccEnergy;
	}
	/**
	 * @return the savedTaskTunedEnergy
	 */
	public BigDecimal getSavedTaskTunedEnergy() {
		return savedTaskTunedEnergy;
	}
	/**
	 * @param savedTaskTunedEnergy the savedTaskTunedEnergy to set
	 */
	public void setSavedTaskTunedEnergy(BigDecimal savedTaskTunedEnergy) {
		this.savedTaskTunedEnergy = savedTaskTunedEnergy;
	}
	
} //end of class AggregatedEmData
