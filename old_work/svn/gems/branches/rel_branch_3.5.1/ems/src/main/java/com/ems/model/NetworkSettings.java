package com.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.util.Constants;



@XmlRootElement (name = "interfaces")
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkSettings implements java.io.Serializable {
	
	
	  @XmlElement(name = "id")
	  Long id ;
	  
	  @XmlElement(name = "configureIPV4")
	  String configureIPV4; 
	  
	
	  @XmlElement(name = "enablePort")
	  boolean enablePort; 
	  
	  @XmlElement(name = "is_dhcp_server")
	  boolean is_dhcp_server; 
	 
	  @XmlElement(name = "subnet_mask")
	  String subnet_mask;
	  
	  @XmlElement(name = "ipaddress")
	  String ipaddress;
	  
	  @XmlElement(name = "default_gateway")
	  String default_gateway;
	  
	  @XmlElement(name = "macaddress")
	  String macaddress;
	  
	  @XmlElement(name = "name")
	  String name;
	  
	  @XmlElement(name = "networkType")
	  String networkType;
	  
	  @XmlElement(name = "dns")
	  String dns;
	  
	  @XmlElement(name = "search_domain_fields")
	  String search_domain_fields;
	  
	  @XmlElement(name = "is_interface_dhcp")
	  boolean is_interface_dhcp; 
	  
	  public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConfigureIPV4() {
		return configureIPV4;
	}

	public void setConfigureIPV4(String configureIPV4) {
		this.configureIPV4 = configureIPV4;
	}

	public boolean isEnablePort() {
		return enablePort;
	}

	public void setEnablePort(boolean enablePort) {
		this.enablePort = enablePort;
	}

	public boolean isIs_dhcp_server() {
		return is_dhcp_server;
	}

	public void setIs_dhcp_server(boolean is_dhcp_server) {
		this.is_dhcp_server = is_dhcp_server;
	}

	public String getSubnet_mask() {
		return subnet_mask;
	}

	public void setSubnet_mask(String subnet_mask) {
		this.subnet_mask = subnet_mask;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getDefault_gateway() {
		return default_gateway;
	}

	public void setDefault_gateway(String default_gateway) {
		this.default_gateway = default_gateway;
	}

	public String getMacaddress() {
		return macaddress;
	}

	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
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

	
	public boolean isIs_interface_dhcp() {
		this.is_interface_dhcp = Constants.DHCP_INTERFACE.equalsIgnoreCase(getConfigureIPV4());
		return is_interface_dhcp;
	}

	public void setIs_interface_dhcp(boolean is_interface_dhcp) {
		this.is_interface_dhcp = is_interface_dhcp;
	}


	@XmlElement(name = "connected_status")
	  String connected_status;
	  
	

		     
}
