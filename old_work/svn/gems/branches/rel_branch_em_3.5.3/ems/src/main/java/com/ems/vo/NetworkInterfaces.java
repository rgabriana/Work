package com.ems.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "interfaces")
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkInterfaces {
	@XmlElement(name = "name")
	String name;
	@XmlElement(name = "macaddress")
	String macaddress;
	@XmlElement(name = "ipaddress")
	String ipaddress;
	@XmlElement(name = "subnet_mask")
	String subnet_mask;
	@XmlElement(name = "default_gateway")
	String default_gateway;
	@XmlElement(name = "dns")
	String dns;
	@XmlElement(name = "search_domain_fields")
	String search_domain_fields;
	@XmlElement(name = "connected_status")
	String connected_status;
	@XmlElement(name = "is_dhcp_server")
	String is_dhcp_server;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMacaddress() {
		return macaddress;
	}
	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}
	public String getIpaddress() {
		return ipaddress;
	}
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	public String getSubnet_mask() {
		return subnet_mask;
	}
	public void setSubnet_mask(String subnet_mask) {
		this.subnet_mask = subnet_mask;
	}
	public String getDefault_gateway() {
		return default_gateway;
	}
	public void setDefault_gateway(String default_gateway) {
		this.default_gateway = default_gateway;
	}
	public String getDns() {
		return dns;
	}
	public void setDns(String dns) {
		this.dns = dns;
	}
	public String getSearch_domain_fields() {
		return search_domain_fields;
	}
	public void setSearch_domain_fields(String search_domain_fields) {
		this.search_domain_fields = search_domain_fields;
	}
	public String getConnected_status() {
		return connected_status;
	}
	public void setConnected_status(String connected_status) {
		this.connected_status = connected_status;
	}
	public String getIs_dhcp_server() {
		return is_dhcp_server;
	}
	public void setIs_dhcp_server(String is_dhcp_server) {
		this.is_dhcp_server = is_dhcp_server;
	}
	
	
	

}


