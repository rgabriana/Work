package com.ems.vo.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gateway")
@XmlAccessorType(XmlAccessType.FIELD)
public class GatewayOutageVO {

	@XmlElement(name = "id")
	long gatewayId;
	@XmlElement(name = "name")
	String gatewayName;
	@XmlElement(name = "location")
	String location;
	@XmlElement(name = "lastconnectivityat")
	Date outageSince;
	@XmlElement(name = "description")
	String description;

	@XmlElement(name = "xaxis")
	private Integer xposition;
	@XmlElement(name = "yaxis")
	private Integer yposition;

	public GatewayOutageVO() {
		super();
	}

	public GatewayOutageVO(long gatewayId, String gatewayName, String location,
			 int xpos, int ypos, Date outageSince) {
		this.gatewayId = gatewayId;
		this.gatewayName = gatewayName;
		this.location = location;
		this.xposition = xpos;
		this.yposition = ypos;
		this.outageSince = outageSince;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getOutageSince() {
		return outageSince;
	}

	public void setOutageSince(Date outageSince) {
		this.outageSince = outageSince;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(long gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public Integer getXposition() {
		return xposition;
	}

	public void setXposition(Integer xposition) {
		this.xposition = xposition;
	}

	public Integer getYposition() {
		return yposition;
	}

	public void setYposition(Integer yposition) {
		this.yposition = yposition;
	}

}
