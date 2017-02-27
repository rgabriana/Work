package com.ems.model;

import java.io.Serializable;

public class CloudServerInfo  implements Serializable {
	
	private static final long serialVersionUID = -1702580838407731212L;
	private String serverIp;
	private String emMac;
	/**
	 * @return the serverIp
	 */
	public String getServerIp() {
		return serverIp;
	}
	/**
	 * @param serverIp the serverIp to set
	 */
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	/**
	 * @return the emMac
	 */
	public String getEmMac() {
		return emMac;
	}
	/**
	 * @param emMac the emMac to set
	 */
	public void setEmMac(String emMac) {
		this.emMac = emMac;
	}

}
