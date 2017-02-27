package com.ems.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ContactClosure {
	
	private Boolean enabled;
	
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