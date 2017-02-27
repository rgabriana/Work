package com.ems.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContactClosure {
	
	@XmlElement(name = "enabled")
	private Boolean enabled;
	
	@XmlElement(name = "contactClosureVo")
	private List<ContactClosureVo> contactClosureVo;
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setContactClosureVo(List<ContactClosureVo> contactClosureVo) {
		this.contactClosureVo = contactClosureVo;
	}

	public List<ContactClosureVo> getContactClosureVo() {
		return contactClosureVo;
	}
	
}