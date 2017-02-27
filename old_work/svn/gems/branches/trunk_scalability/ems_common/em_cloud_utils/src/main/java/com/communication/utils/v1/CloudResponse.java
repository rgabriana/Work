package com.communication.utils.v1;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.communication.types.CloudParamType;
import com.communication.utils.NameValue;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CloudResponse {
	@XmlElement(name="status")
	private int status;
	@XmlElement(name="msg")
	private String msg;
	@XmlElement(name="nameval")
	private ArrayList<NameValue> nameval;
	
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

	
}
