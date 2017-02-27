/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Sreedhar
 * @hibernate.class 
 * 		table="firmware_upgrade_schedule"
 */
public class FirmwareUpgradeSchedule implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 6824889907580048570L;
  
  private Long id;
  private String fileName;
  private Date addedTime;
  private Date scheduledTime;
  private Date startTime;
  private Integer duration;
  private Boolean onReboot;
  private Integer retries;
  private Integer retryInterval;
  private String description;
  private String deviceType;
  private String modelNo;
  private Boolean active;
  private String includeList;
  private String excludeList;
  private String version;
  private String jobPrefix;
      	
  /**
   * 
   */
  public FirmwareUpgradeSchedule() {
    // TODO Auto-generated constructor stub
  }
  
  /**
   * @return the id
   */
  public Long getId() {
      return id;
  }

  /**
   * @param id
   *            the id to set
   */
  public void setId(Long id) {
      this.id = id;
  }

  /**
   * @return the fileName
   * @hibernate.property 
   * 		column="file_name"
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
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
   * @return the duration
   * @hibernate.property 
   * 		column="duration"
   */
  public Integer getDuration() {
    return duration;
  }

  /**
   * @param duration the duration to set
   */
  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  /**
   * @return the noOfRetries
   * @hibernate.property 
   *  		column="retries"
   */
  public Integer getRetries() {
    return retries;
  }

  /**
   * @param noOfRetries the noOfRetries to set
   */
  public void setRetries(Integer retries) {
    this.retries = retries;
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

	/**
	 * @return the onReboot
	 */
	public Boolean getOnReboot() {
		return onReboot;
	}

	/**
	 * @param onReboot the onReboot to set
	 */
	public void setOnReboot(Boolean onReboot) {
		this.onReboot = onReboot;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
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
	 * @return the addedTime
	 */
	public Date getAddedTime() {
		return addedTime;
	}

	/**
	 * @param addedTime the addedTime to set
	 */
	public void setAddedTime(Date addedTime) {
		this.addedTime = addedTime;
	}

	/**
	 * @return the modelNo
	 */
	public String getModelNo() {
		return modelNo;
	}

	/**
	 * @param modelNo the modelNo to set
	 */
	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
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
	 * @return the jobPrefix
	 */
	public String getJobPrefix() {
		return jobPrefix;
	}

	/**
	 * @param jobPrefix the jobPrefix to set
	 */
	public void setJobPrefix(String jobPrefix) {
		this.jobPrefix = jobPrefix;
	}
	
} //end of class ImageUpgradeDBJob
