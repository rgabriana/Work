package com.emscloud.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FacilityEmInfo implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	private long facilityId;
	private long emId;
	private long emFacilityId;
	private String replicaIp;	
	private String dbName;	
		
	/**
	 * @return the facilityId
	 */

	@XmlElement(name = "facilityId")
	public long getFacilityId() {
		return facilityId;
	}
	/**
	 * @param facilityId the facilityId to set
	 */
	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}
		
	/**
	 * @return the replicaIp
	 */
	@XmlElement(name = "replicaIp")
	public String getReplicaIp() {
		return replicaIp;
	}
	/**
	 * @param replicaIp the replicaIp to set
	 */
	public void setReplicaIp(String replicaIp) {
		this.replicaIp = replicaIp;
	}
	/**
	 * @return the emId
	 */
	@XmlElement(name = "emId")
	public long getEmId() {
		return emId;
	}
	/**
	 * @param emId the emId to set
	 */
	public void setEmId(long emId) {
		this.emId = emId;
	}
	/**
	 * @return the emFacilityId
	 */
	@XmlElement(name = "emFacilityId")
	public long getEmFacilityId() {
		return emFacilityId;
	}
	/**
	 * @param emFacilityId the emFacilityId to set
	 */
	public void setEmFacilityId(long emFacilityId) {
		this.emFacilityId = emFacilityId;
	}
	/**
	 * @return the dbName
	 */
	@XmlElement(name = "dbName")
	public String getDbName() {
		return dbName;
	}
	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
}
