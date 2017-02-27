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
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BacnetObjectsCfg implements Serializable {

    private static final long serialVersionUID = 3417517521123672780L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "bacnetobjecttype")
    private String bacnetobjecttype;
    @XmlElement(name = "bacnetobjectinstance")
    private Long bacnetobjectinstance;
    @XmlElement(name = "bacnetobjectdescription")
    private String bacnetobjectdescription;
    @XmlElement(name = "isvalidobject")
    private String isvalidobject;
    @XmlElement(name = "pointkeyword")
    private String pointkeyword;
    @XmlElement(name = "bacnetpointtype")
    private String bacnetpointtype;
    
	
    
    /**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}



	/**
	 * @return the bacnetobjecttype : AI, AV, BI, BV
	 */
	public String getBacnetobjecttype() {
		return bacnetobjecttype;
	}



	/**
	 * @param bacnetobjecttype the bacnetobjecttype to set
	 */
	public void setBacnetobjecttype(String bacnetobjecttype) {
		this.bacnetobjecttype = bacnetobjecttype;
	}



	/**
	 * @return the bacnetobjectinstance : Instance
	 */
	public Long getBacnetobjectinstance() {
		return bacnetobjectinstance;
	}



	/**
	 * @param bacnetobjectinstance the bacnetobjectinstance to set
	 */
	public void setBacnetobjectinstance(Long bacnetobjectinstance) {
		this.bacnetobjectinstance = bacnetobjectinstance;
	}



	/**
	 * @return the bacnetobjectdescription : Can be modified
	 */
	public String getBacnetobjectdescription() {
		return bacnetobjectdescription;
	}



	/**
	 * @param bacnetobjectdescription the bacnetobjectdescription to set
	 */
	public void setBacnetobjectdescription(String bacnetobjectdescription) {
		this.bacnetobjectdescription = bacnetobjectdescription;
	}



	/**
	 * @return the isvalidobject : y-valid,n-not valid,r-valid,read-only
	 */
	public String getIsvalidobject() {
		return isvalidobject;
	}



	/**
	 * @param isvalidobject the isvalidobject to set
	 */
	public void setIsvalidobject(String isvalidobject) {
		this.isvalidobject = isvalidobject;
	}



	/**
	 * @return the pointkeyword : Keyword to specify parameter
	 */
	public String getPointkeyword() {
		return pointkeyword;
	}



	/**
	 * @param pointkeyword the pointkeyword to set
	 */
	public void setPointkeyword(String pointkeyword) {
		this.pointkeyword = pointkeyword;
	}



	/**
	 * @return the bacnetpointtype : bacnetpointtype
	 */
	public String getBacnetpointtype() {
		return bacnetpointtype;
	}



	/**
	 * @param bacnetpointtype the bacnetpointtype to set
	 */
	public void setBacnetpointtype(String bacnetpointtype) {
		this.bacnetpointtype = bacnetpointtype;
	}



	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
