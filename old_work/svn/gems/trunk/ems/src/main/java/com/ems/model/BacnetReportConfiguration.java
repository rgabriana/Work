/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author NileshS
 *
 */
@XmlRootElement(name = "bacnetreportconfiguration")
@XmlAccessorType(XmlAccessType.NONE)
public class BacnetReportConfiguration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "deviceid")
    private String deviceid;
	@XmlElement(name = "objecttype")
    private String objecttype;
	@XmlElement(name = "objectinstance")
    private String objectinstance;
	@XmlElement(name = "objectname")
	private String objectname;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDeviceid() {
		return deviceid;
	}
	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}
	public String getObjecttype() {
		return objecttype;
	}
	public void setObjecttype(String objecttype) {
		this.objecttype = objecttype;
	}
	public String getObjectinstance() {
		return objectinstance;
	}
	public void setObjectinstance(String objectinstance) {
		this.objectinstance = objectinstance;
	}
	public String getObjectname() {
		return objectname;
	}
	public void setObjectname(String objectname) {
		this.objectname = objectname;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
