/**
 * 
 */
package com.ems.vo;

/**
 * @author enlighted
 *
 */
public class SolarDataVO {
	private short civildawn = 0;
	private short sunrise = 0;
	private short solarNoon = 0;
	private short sunset = 0;
	private short civildusk = 0;
	
	public short getCivildawn() {
		return civildawn;
	}
	public void setCivildawn(short civildawn) {
		this.civildawn = civildawn;
	}
	public short getSunrise() {
		return sunrise;
	}
	public void setSunrise(short sunrise) {
		this.sunrise = sunrise;
	}
	public short getSolarNoon() {
		return solarNoon;
	}
	public void setSolarNoon(short solarNoon) {
		this.solarNoon = solarNoon;
	}
	public short getSunset() {
		return sunset;
	}
	public void setSunset(short sunset) {
		this.sunset = sunset;
	}
	public short getCivildusk() {
		return civildusk;
	}
	public void setCivildusk(short civildusk) {
		this.civildusk = civildusk;
	}
	@Override
	public String toString() {
		return "SolarDataVO [civildawn=" + civildawn + ", sunrise=" + sunrise
				+ ", solarNoon=" + solarNoon + ", sunset=" + sunset
				+ ", civildusk=" + civildusk + "]";
	}

}
