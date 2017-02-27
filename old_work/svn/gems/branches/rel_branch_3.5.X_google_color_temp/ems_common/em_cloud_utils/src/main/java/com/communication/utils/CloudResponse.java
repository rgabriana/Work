package com.communication.utils;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.communication.types.CloudParamType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CloudResponse {
	@XmlElement(name="status")
	private int status;
	@XmlElement(name="msg")
	private String msg;
	private ArrayList<NameValue> nameval;
	
	@XmlElement(name="namevalmap")
	private HashMap<String, String> namevalmap;
	
	public CloudResponse() {
		status = 0;
	}
	
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the nameval
	 */
	@JsonIgnore
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
	 * @return the namevalmap
	 */
	public HashMap<String, String> getNamevalmap() {
		return namevalmap;
	}

	/**
	 * @param namevalmap the namevalmap to set
	 */
	public void setNamevalmap(HashMap<String, String> namevalmap) {
		this.namevalmap = namevalmap;
	}
	
	public void listToMap() {
		namevalmap = new HashMap<String, String>();
		if(nameval != null && !nameval.isEmpty()) {
			for(NameValue nv: nameval) {
				namevalmap.put(nv.getName().getName(), nv.getValue());
			}
		}
	}
	
	public void mapToList() {
		nameval = new ArrayList<NameValue>();
		if(namevalmap != null) {
			for(String key: namevalmap.keySet()) {
				try {
					if(CloudParamType.valueOf(key).getName().equals(key)) {
						nameval.add(new NameValue(CloudParamType.valueOf(key), namevalmap.get(key)));
					}
				}
				catch (IllegalArgumentException e) {
					//System.out.println("ENUM NOT FOUND: " + key);
				}
			}
		}
		
	}
	
}
