package com.ems.vo;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ttlconfiguration")
public class TTLConfiguration {
	
	String modelNo;
	Integer hopCount;


	public String getModelNo() {
		return modelNo;
	}

	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}

	public Integer getHopCount() {
		return hopCount;
	}

	public void setHopCount(Integer hopCount) {
		this.hopCount = hopCount;
	}

	

}
