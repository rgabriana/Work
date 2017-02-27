/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Sreedhar
 * @hibernate.class 
 * 		table="image_upgrade_job"
 */
public class ImageUpgradeDBJob implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 6824889907580048570L;

  private Long id;
  private String jobName;
  private String imageName;
  private Date scheduledTime;
  private Date startTime;
  private Date endTime;
  private Integer noOfRetries;
  private String status;
  private String description;
  private String deviceType;
  private Integer retryInterval;
  private Date stopTime;
  
  private Long[] deviceIds;    
  private String includeList;
  private String excludeList;
  private String version;
    	
  /**
   * 
   */
  public ImageUpgradeDBJob() {
    // TODO Auto-generated constructor stub
  }
  
  public ImageUpgradeDBJob(Long id, String jobName, String imageName, Date scheduledTime,
	Date startTime, Date endTime, int noOfRetries, String status, String description,
	String deviceType) {

    this.id = id;
    this.jobName = jobName;
    this.imageName = imageName;
    this.scheduledTime = scheduledTime;
    this.startTime = startTime;
    this.endTime = endTime;
    this.noOfRetries = noOfRetries;
    this.status = status;
    this.description = description;
    this.deviceType = deviceType;

  } //end of constructor
  
  /**
   * @return the id
   * @hibernate.id 
   * 		generator-class="native"
   * 		unsaved-value="null"
   * 
   * @hibernate.generator-param 
   * 		name="sequence"
   * 		value="image_upgrade_job_seq"
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
   * @return the jobName
   * @hibernate.property 
   * 		column="job_name"
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * @param jobName the jobName to set
   */
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  /**
   * @return the imageName
   * @hibernate.property 
   * 		column="image_name"
   */
  public String getImageName() {
    return imageName;
  }

  /**
   * @param imageName the imageName to set
   */
  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  /**
   * @return the scheduledTime
   * @hibernate.property 
   *  		column="scheduled_time"
   */
  public Date getScheduledTime() {
    return scheduledTime;
  }

  /**
   * @param scheduledTime the scheduledTime to set
   */
  public void setScheduledTime(Date scheduledTime) {
    this.scheduledTime = scheduledTime;
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
   * @return the noOfRetries
   * @hibernate.property 
   *  		column="no_of_retries"
   */
  public Integer getNoOfRetries() {
    return noOfRetries;
  }

  /**
   * @param noOfRetries the noOfRetries to set
   */
  public void setNoOfRetries(Integer noOfRetries) {
    this.noOfRetries = noOfRetries;
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
   * @return the deviceType
   * @hibernate.property 
   * 		column="device_type"
   */
  public String getDeviceType() {
    return deviceType;
  }

  /**
   * @param deviceType the deviceType to set
   */
  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

	/**
	 * @return the retryInterval
	 */
	public Integer getRetryInterval() {
		return retryInterval;
	}

	/**
	 * @param retryInterval the retryInterval to set
	 */
	public void setRetryInterval(Integer retryInterval) {
		this.retryInterval = retryInterval;
	}

  public Long[] getDeviceIds() {
    return deviceIds;
  }

  public void setDeviceIds(Long[] deviceIds) {
    this.deviceIds = deviceIds;
  }
    
  /**
	 * @return the includeList
	 */
	public String getIncludeList() {
		return includeList;
	}

	/**
	 * @param includeList the includeList to set
	 */
	public void setIncludeList(String includeList) {
		this.includeList = includeList;
	}

	/**
	 * @return the excludeList
	 */
	public String getExcludeList() {
		return excludeList;
	}

	/**
	 * @param excludeList the excludeList to set
	 */
	public void setExcludeList(String excludeList) {
		this.excludeList = excludeList;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the stopTime
	 */
	public Date getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	
} //end of class ImageUpgradeDBJob
