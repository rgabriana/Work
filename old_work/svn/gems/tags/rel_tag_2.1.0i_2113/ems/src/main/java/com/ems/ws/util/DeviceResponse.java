/**
 * 
 */
package com.ems.ws.util;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh Helper class enables getting HVAC device response from modbus
 *         module
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceResponse {
	@XmlElement(name = "status")
	private int status;
	@XmlElement(name = "args")
	private String args;
	@XmlElement(name = "message")
	private String message;
	@XmlElement(name = "result")
	private String result;
	@XmlElement(name = "updatedts")
	private Date oUpdatedTS;

	public DeviceResponse() {
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
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return the oUpdatedTS
	 */
	public Date getoUpdatedTS() {
		return oUpdatedTS;
	}

	/**
	 * @param oUpdatedTS
	 *            the oUpdatedTS to set
	 */
	public void setoUpdatedTS(Date oUpdatedTS) {
		this.oUpdatedTS = oUpdatedTS;
	}

	public void copy(DeviceResponse oDevice) {
		this.status = oDevice.status;
		this.message = oDevice.message;
		this.result = oDevice.result;
		this.oUpdatedTS = oDevice.oUpdatedTS;
	}

	public String getArgs() {
		if (args == null || args.equals(""))
			return "-1";
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String toString() {
		return "{status: " + status + ", message: " + message + ", args: "
				+ args + ", result: " + result + ", updatets: " + oUpdatedTS
				+ "}";
	}
}
