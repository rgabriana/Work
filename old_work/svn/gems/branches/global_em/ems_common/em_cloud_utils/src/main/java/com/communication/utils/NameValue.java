package com.communication.utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.communication.types.CloudParamType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NameValue {
	
	@XmlElement(name="name")
	private CloudParamType name;
	@XmlElement(name="value")
	private String value;
	
	public NameValue() {
	}
	
	public NameValue(CloudParamType name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public CloudParamType getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(CloudParamType name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
