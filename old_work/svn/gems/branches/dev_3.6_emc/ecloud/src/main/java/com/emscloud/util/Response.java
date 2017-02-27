/**
 * Generic response type returned from the webservice layer.
 */
package com.emscloud.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Response {
	@XmlElement(name="status")
	private int status;
	@XmlElement(name="msg")
	private String msg;
	@XmlElement(name="commmessage")
	private String commMessage;
	
	public Response() {
		// Success
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

	public void setCommMessage(String commMessage) {
		this.commMessage = commMessage;
	}

	public String getCommMessage() {
		return commMessage;
	}


}
