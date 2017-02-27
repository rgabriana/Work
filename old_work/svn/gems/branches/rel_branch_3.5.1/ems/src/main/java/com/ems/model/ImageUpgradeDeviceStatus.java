/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Transient;

import com.ems.types.DeviceType;

/**
 * @author Sreedhar
 * @hibernate.class 
 * 		table="image_upgrade_device_status"
 */
public class ImageUpgradeDeviceStatus implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -793157032117322475L;

  private Long id;
  private Long jobId;
  private Long deviceId;
  private Date startTime;
  private Date endTime;
  private Integer noOfAttempts;
  private String status;
  private String description;
  private String device_type;
  private String new_version;
  
  private String deviceName;
  
  private String jobName;
  
  /**
   * 
   */
  public ImageUpgradeDeviceStatus(DeviceType oDeviceType) {
	  device_type = oDeviceType.getName();
  }

  
  public ImageUpgradeDeviceStatus() {
	
  }
  /**
   * @return the id
   * @hibernate.id 
   * 		generator-class="native"
   * 		unsaved-value="null"
   * 
   * @hibernate.generator-param 
   * 		name="sequence"
   * 		value="image_upgrade_device_status_seq"
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
   * @return the jobId
   * @hibernate.property 
   * 		column="job_id"
   */
  public Long getJobId() {
    return jobId;
  }

  /**
   * @param jobId the jobId to set
   */
  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }

  /**
   * @return the startTime
   * @hibernate.property 
   * 		column="start_time"
   */
  public Date getStartTime() {
    return startTime;
  }

  /**
   * @param startTime the startTime to set
   */
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  /**
   * @return the endTime
   * @hibernate.property 
   * 		column="end_time"
   */
  public Date getEndTime() {
    return endTime;
  }

  /**
   * @param endTime the endTime to set
   */
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  /**
   * @return the noOfAttempts
   * @hibernate.property 
   * 		column="no_of_attempts"
   */
  public Integer getNoOfAttempts() {
    return noOfAttempts;
  }

  /**
   * @param noOfAttempts the noOfAttempts to set
   */
  public void setNoOfAttempts(Integer noOfAttempts) {
    this.noOfAttempts = noOfAttempts;
  }

  /**
   * @return the status
   * @hibernate.property 
   * 		column="status"
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the description
   * @hibernate.property 
   * 		column="description"
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * @return the description
   * @hibernate.property 
   * 		column="device_id"
   */
  public Long getDeviceId() {
    return deviceId;
  }

  /**
   * @param deviceId the deviceId to set
   */
  public void setDeviceId(Long deviceId) {
    this.deviceId = deviceId;
  }

public String getDevice_type() {
	return device_type;
}

public void setDevice_type(String device_type) {
	this.device_type = device_type;
}


/**
 * @return the new_version
 * @hibernate.property 
 * 		column="new_version"
 */
public String getNew_version() {
	return new_version;
}


/**
 * @param new_version the new_version to set
 */
public void setNew_version(String new_version) {
	this.new_version = new_version;
}


public void setDeviceName(String deviceName) {
	this.deviceName = deviceName;
}

@Transient
public String getDeviceName() {
	return deviceName;
}


public void setJobName(String jobName) {
	this.jobName = jobName;
}

@Transient
public String getJobName() {
	return jobName;
}
  
} //end of class ImageUpgradeDeviceStatus
