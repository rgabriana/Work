/**
 * 
 */
package com.ems.server.upgrade;

import com.ems.model.ImageUpgradeDBJob;

/**
 * @author sreedhar
 *
 */
public class ImageUpgradeJob {

  private int deviceType;
  private String fileName;
  private int[] deviceIds;
  private ImageUpgradeDBJob dbJob;
  
  private long[] failedDevices;
    
  /**
   * 
   */
  public ImageUpgradeJob() {
    // TODO Auto-generated constructor stub
  }

  public int getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(int deviceType) {
    this.deviceType = deviceType;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public int[] getDeviceIds() {
    return deviceIds;
  }

  public void setDeviceIds(int[] deviceIds) {
    this.deviceIds = deviceIds;
  }
  
  public ImageUpgradeDBJob getImageUpgradeDBJob() {
    return dbJob;
  }
  
  public void setImageUpgradeDBJob(ImageUpgradeDBJob dbJob) {
    this.dbJob = dbJob;
  } //end of method saveToDB
  
} //end of class ImageUpgradeDBJob
