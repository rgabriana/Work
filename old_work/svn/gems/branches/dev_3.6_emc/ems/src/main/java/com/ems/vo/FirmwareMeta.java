/**
 * 
 */
package com.ems.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sreedhar.kamishetti
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FirmwareMeta {

	@XmlElementWrapper(name = "deviceFirmwares")
	@XmlElement(name = "deviceFirmware")
	List<DeviceFirmware> devices;

	/**
	 * @return the devices
	 */
	public List<DeviceFirmware> getDevices() {
		return devices;
	}

	/**
	 * @param devices the devices to set
	 */
	public void setDevices(List<DeviceFirmware> devices) {
		this.devices = devices;
	}
	
}
