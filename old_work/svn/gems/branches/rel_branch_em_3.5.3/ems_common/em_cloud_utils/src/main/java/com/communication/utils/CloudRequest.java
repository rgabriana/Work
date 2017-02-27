package com.communication.utils;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.communication.types.CloudParamType;
import com.communication.vo.OSDetails;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CloudRequest {
	
	@XmlElement(name="macId")
	private String macId;
	@XmlElement(name="appVersion")
	private String appVersion;
	@XmlElement(name="nameval")
	private ArrayList<NameValue> nameval;
	
	@XmlElement(name="os")
	private OSDetails os = new OSDetails();
	
	public CloudRequest() {}
	
	public CloudRequest(String macId, String appVersion) {
		this.macId = macId;
		this.appVersion = appVersion;
	}

	
	public OSDetails getOs() {
		return os;
	}

	public void setOs(OSDetails os) {
		this.os = os;
	}

	/**
	 * @return the macId
	 */
	public String getMacId() {
		return macId;
	}

	/**
	 * @param macId the macId to set
	 */
	public void setMacId(String macId) {
		this.macId = macId;
	}

	/**
	 * @return the nameval
	 */
	public ArrayList<NameValue> getNameval() {
		return nameval;
	}

	/**
	 * @param nameval the nameval to set
	 */
	public void setNameval(ArrayList<NameValue> nameval) {
		this.nameval = nameval;
	}
	
	@JsonIgnore
	public HashMap<CloudParamType, String> getNameValueMap() {
		HashMap<CloudParamType, String> map = new HashMap<CloudParamType, String>();
		if(nameval != null && !nameval.isEmpty()) {
			for(NameValue nv: nameval) {
				map.put(nv.getName(), nv.getValue());
			}
		}
		return map;
	}

	/**
	 * @return the appVersion
	 */
	public String getAppVersion() {
		return appVersion;
	}

	/**
	 * @param appVersion the appVersion to set
	 */
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

}
