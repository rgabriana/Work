/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

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
  
  /**
   * 
   */
  public ImageUpgradeDeviceStatus() {
    // TODO Auto-generated constructor stub
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
  
} //end of class ImageUpgradeDeviceStatus
