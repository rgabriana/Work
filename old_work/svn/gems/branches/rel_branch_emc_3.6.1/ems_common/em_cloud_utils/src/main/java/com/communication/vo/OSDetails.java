package com.communication.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OSDetails {
	
	//"{\"os\":{\"arch\":\"$ARCH_BIT\",\"version\":\"$current_os\"}}"
	@XmlElement(name="arch")
	private String arch = "32";
	@XmlElement(name="version")
	private String version = "10.04";
	public String getArch() {
		return arch;
	}
	public void setArch(String arch) {
		this.arch = arch;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
