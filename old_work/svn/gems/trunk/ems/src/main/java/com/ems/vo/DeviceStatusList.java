package com.ems.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceStatusList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 431776385828817164L;
	
	public static final int DEFAULT_ROWS = 10;
	
	@XmlElement(name = "page")
	private int page;
	@XmlElement(name = "total")
	private long total;
	@XmlElement(name = "records")
	private long records;	
	
	@XmlElementWrapper(name = "deviceFixtures")
	List<DeviceFixture> deviceFixtureList = null;
	
	@XmlElementWrapper(name = "deviceGateways")
	List<DeviceGateway> deviceGatewayList = null;
	
	@XmlElementWrapper(name = "deviceWdss")
	List<DeviceWds> deviceWdsList = null;	
	
	@XmlElementWrapper(name = "devicePlugloads")
	List<DevicePlugload> devicePlugloadList = null;

	public DeviceStatusList() {
		// TODO Auto-generated constructor stub
		deviceFixtureList = new ArrayList<DeviceFixture>();
		deviceGatewayList = new ArrayList<DeviceGateway>();
		deviceWdsList = new ArrayList<DeviceWds>();
		devicePlugloadList = new ArrayList<DevicePlugload>();
	}
	
	public List<DeviceFixture> getDeviceFixtureList() {
		return deviceFixtureList;
	}

	public void setDeviceFixtureList(List<DeviceFixture> deviceFixtureList) {
		this.deviceFixtureList = deviceFixtureList;
	}

	public List<DeviceGateway> getDeviceGatewayList() {
		return deviceGatewayList;
	}

	public void setDeviceGatewayList(List<DeviceGateway> deviceGatewayList) {
		this.deviceGatewayList = deviceGatewayList;
	}

	public List<DeviceWds> getDeviceWdsList() {
		return deviceWdsList;
	}

	public void setDeviceWdsList(List<DeviceWds> deviceWdsList) {
		this.deviceWdsList = deviceWdsList;
	}

	public List<DevicePlugload> getDevicePlugloadList() {
		return devicePlugloadList;
	}

	public void setDevicePlugloadList(List<DevicePlugload> devicePlugloadList) {
		this.devicePlugloadList = devicePlugloadList;
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public long getRecords() {
		return records;
	}
	public void setRecords(long records) {
		this.records = records;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}

    
}
