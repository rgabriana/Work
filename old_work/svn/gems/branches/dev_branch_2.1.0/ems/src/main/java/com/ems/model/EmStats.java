/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author sreedhar
 */
public class EmStats implements Serializable {

    private static final long serialVersionUID = 7323703683116646247L;

    private Long id;
    private Date captureAt;
    private Integer activeThreadCount;
    private Long gcCount;
    private Long gcTime;
    private Double heapUsed;
    private Double nonHeapUsed;
    private Double sysLoad;
    private Float cpuPercentage;

    /**
   * 
   */
    public EmStats() {
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
     * @return the captureAt
     */
    public Date getCaptureAt() {
        return captureAt;
    }

    /**
     * @param captureAt
     *            the captureAt to set
     */
    public void setCaptureAt(Date captureAt) {
        this.captureAt = captureAt;
    }

    /**
     * @return the activeThreadCount
     */
    public Integer getActiveThreadCount() {
        return activeThreadCount;
    }

    /**
     * @param activeThreadCount
     *            the activeThreadCount to set
     */
    public void setActiveThreadCount(Integer activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
    }

    
    /**
     * @return the gcCount
     */
    public Long getGcCount() {
    
      return gcCount;
    }

    
    /**
     * @param gcCount the gcCount to set
     */
    public void setGcCount(Long gcCount) {
    
      this.gcCount = gcCount;
    }

    
    /**
     * @return the gcTime
     */
    public Long getGcTime() {
    
      return gcTime;
    }

    
    /**
     * @param gcTime the gcTime to set
     */
    public void setGcTime(Long gcTime) {
    
      this.gcTime = gcTime;
    }

    
    /**
     * @return the heapUsed
     */
    public Double getHeapUsed() {
    
      return heapUsed;
    }

    
    /**
     * @param heapUsed the heapUsed to set
     */
    public void setHeapUsed(Double heapUsed) {
    
      this.heapUsed = heapUsed;
    }

    
    /**
     * @return the nonHeapUsed
     */
    public Double getNonHeapUsed() {
    
      return nonHeapUsed;
    }

    
    /**
     * @param nonHeapUsed the nonHeapUsed to set
     */
    public void setNonHeapUsed(Double nonHeapUsed) {
    
      this.nonHeapUsed = nonHeapUsed;
    }

    
    /**
     * @return the sysLoad
     */
    public Double getSysLoad() {
    
      return sysLoad;
    }

    
    /**
     * @param sysLoad the sysLoad to set
     */
    public void setSysLoad(Double sysLoad) {
    
      this.sysLoad = sysLoad;
    }

    
    /**
     * @return the cpuPercentage
     */
    public Float getCpuPercentage() {
    
      return cpuPercentage;
    }

    
    /**
     * @param cpuPercentage the cpuPercentage to set
     */
    public void setCpuPercentage(Float cpuPercentage) {
    
      this.cpuPercentage = cpuPercentage;
    }

    
} // end of class GwStats
