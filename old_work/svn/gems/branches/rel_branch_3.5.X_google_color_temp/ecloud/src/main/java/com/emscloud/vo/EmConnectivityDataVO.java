package com.emscloud.vo;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmConnectivityDataVO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3471601758624936723L;
	private Long id;
	private Date lastEmConnectivity;
	private Date lastEmSync;
	private Date last5MinCaptureAt;
	
	
	
	@XmlElement(name = "lastEmConnectivity")
	public Date getLastEmConnectivity() {
		return lastEmConnectivity;
	}
	public void setLastEmConnectivity(Date lastEmConnectivity) {
		this.lastEmConnectivity = lastEmConnectivity;
	}
	
	@XmlElement(name = "lastEmSync")
	public Date getLastEmSync() {
		return lastEmSync;
	}
	public void setLastEmSync(Date lastEmSync) {
		this.lastEmSync = lastEmSync;
	}
	
	@XmlElement(name = "last5MinCaptureAt")
	public Date getLast5MinCaptureAt() {
		return last5MinCaptureAt;
	}
	public void setLast5MinCaptureAt(Date last5MinCaptureAt) {
		this.last5MinCaptureAt = last5MinCaptureAt;
	}
	
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

}
