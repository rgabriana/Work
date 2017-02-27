package com.ems.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "networkDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class NetworkDetails  implements Serializable {
	
//	@XmlElements({
//	    @XmlElement(name="networkInterfaceMappings",type=NetworkInterfaceMapping.class),
//	    @XmlElement(name="interfacess",type=NetworkSettings.class)
//	  })
	
	@XmlElementWrapper (name="networkInterfaceMappings")
	@XmlElement (name = "networkInterfaceMapping")
	ArrayList<NetworkInterfaceMapping> networkInterfaceMapping;

	@XmlElementWrapper (name="interfacess")
	@XmlElement (name = "interfaces")
	ArrayList<NetworkSettings> interfaces;

	public List<NetworkInterfaceMapping> getNetworkInterfaceMapping() {
		return networkInterfaceMapping;
	}

	public void setNetworkInterfaceMapping(
			ArrayList<NetworkInterfaceMapping> networkInterfaceMapping) {
		this.networkInterfaceMapping = networkInterfaceMapping;
	}

	public List<NetworkSettings> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(ArrayList<NetworkSettings> interfaces) {
		this.interfaces = interfaces;
	}


	
}
