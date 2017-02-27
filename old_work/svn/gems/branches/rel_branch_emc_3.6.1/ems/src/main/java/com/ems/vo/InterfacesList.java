package com.ems.vo;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.NetworkSettings;

@XmlRootElement(name = "interfacesList")
@XmlAccessorType(XmlAccessType.NONE)
public class InterfacesList {
	
	@XmlElement(name = "interfaces")
	List<NetworkSettings> interfaces;

	public List<NetworkSettings> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<NetworkSettings> interfaces) {
		this.interfaces = interfaces;
	}

	
		
	

}
