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
import org.codehaus.jackson.map.annotate.JsonSerialize;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonSerialize(
		include=JsonSerialize.Inclusion.NON_NULL
)
public class SensorData {
	
	private static SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	@XmlElement(name = "sensorId")
	private Long sensorId;
	
	@XmlElement(name = "timestamp")
	private Date timestamp;
	
	/*
	@XmlElement(name = "date")
	private String date;
	
	@XmlElement(name = "hour")
	private Short hour;
	
	@XmlElement(name = "weekday")
	private Boolean weekday;
	*/
	
	@XmlElement(name = "timeSpan")
	private String timeSpan;
	
	@XmlElement(name = "motionBits")
	private Long motionBits;
	
	@XmlElement(name = "motionEvents")
	private Integer motionEvents;
	
	@XmlElement(name = "avgTemp")
	private Float avgTemp;
	
	@XmlElement(name = "avgVolts")
	private Float avgVolts;
	
	@XmlElement(name = "avgAmb")
	private Integer avgAmb;
	
	@XmlElement(name = "energy")
	private BigDecimal energy;
	@XmlElement(name = "baseEnergy")
	private BigDecimal baseEnergy;
	@XmlElement(name = "savedAmbEnergy")
	private BigDecimal savedAmbEnergy;
	@XmlElement(name = "savedOccEnergy")
	private BigDecimal savedOccEnergy;
	@XmlElement(name = "savedTaskTunedEnergy")
	private BigDecimal savedTaskTunedEnergy;
	
	/**
	 * @return the sensorId
	 */
	public Long getSensorId() {
		return sensorId;
	}
	/**
	 * @param sensorId the sensorId to set
	 */
	public void setSensorId(Long sensorId) {
		this.sensorId = sensorId;
	}
	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return fullDateFormat.format(timestamp);
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		Calendar cal = Calendar.getInstance();
		cal.setTime(timestamp);
		/*
		this.hour = (short)cal.get(Calendar.HOUR);
		String dayName = dayNameFormat.format(cal.getTime());
		if(dayName.compareTo("Sat") == 0 || dayName.compareTo("Sun") == 0) {
			weekday = false;
		} else {
			weekday = true;
		}
		date = dateFormat.format(cal.getTime());
		*/
	}
	
//	/**
//	 * @return the date
//	 */
//	public String getDate() {
//		return date;
//	}
//	/**
//	 * @param date the date to set
//	 */
//	public void setDate(String date) {
//		this.date = date;
//	}
//	/**
//	 * @return the hour
//	 */
//	public Short getHour() {
//		return hour;
//	}
//	/**
//	 * @param hour the hour to set
//	 */
//	public void setHour(Short hour) {
//		this.hour = hour;
//	}
//	/**
//	 * @return the weekday
//	 */
//	public Boolean getWeekday() {
//		return weekday;
//	}
//	/**
//	 * @param weekday the weekday to set
//	 */
//	public void setWeekday(Boolean weekday) {
//		this.weekday = weekday;
//	}
	
	/**
	 * @return the timeSpan
	 */
	public String getTimeSpan() {
		return timeSpan;
	}
	/**
	 * @param timeSpan the timeSpan to set
	 */
	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}
	/**
	 * @return the motionBits
	 */
	public Long getMotionBits() {
		return motionBits;
	}
	/**
	 * @param motionBits the motionBits to set
	 */
	public void setMotionBits(Long motionBits) {
		this.motionBits = motionBits;
		this.motionEvents = Long.bitCount(motionBits);
	}
	/**
	 * @return the motionEvents
	 */
	public Integer getMotionEvents() {
		return motionEvents;
	}
	/**
	 * @param motionEvents the motionEvents to set
	 */
	public void setMotionEvents(Integer motionEvents) {
		this.motionEvents = motionEvents;
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
	 * @return the avgVolts
	 */
	public Float getAvgVolts() {
		return avgVolts;
	}
	/**
	 * @param avgVolts the avgVolts to set
	 */
	public void setAvgVolts(Float avgVolts) {
		this.avgVolts = avgVolts;
	}
	/**
	 * @return the avgAmb
	 */
	public Integer getAvgAmb() {
		return avgAmb;
	}
	/**
	 * @param avgAmb the avgAmb to set
	 */
	public void setAvgAmb(Integer avgAmb) {
		this.avgAmb = avgAmb;
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
	
} //end of class SensorData
