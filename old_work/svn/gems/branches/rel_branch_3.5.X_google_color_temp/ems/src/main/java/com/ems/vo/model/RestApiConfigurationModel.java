package com.ems.vo.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RestApiConfigurationModel {
	
	 @XmlElement(name = "apikey")
	String apiKey ;
	 @XmlElement(name = "isValidated")
	boolean validated ;
	 @XmlElement(name = "file")
	 FileUpload licenseFile ;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public FileUpload getLicenseFile() {
		return licenseFile;
	}

	public void setLicenseFile(FileUpload licenseFile) {
		this.licenseFile = licenseFile;
	}

}
