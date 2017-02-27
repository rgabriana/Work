package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "em_stats", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmStats implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5118795673019397530L;
	private Long id;
	private Date captureAt;
    private Integer activeThreadCount;
    private Long gcCount;
    private Long gcTime;
    private Double heapUsed;
    private Double nonHeapUsed;
    private Double sysLoad;
    private Float cpuPercentage;
    private Long emInstanceId;
    private Boolean isEmAccessible;
    
    public EmStats() {
		// TODO Auto-generated constructor stub
	}
    
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_stats_seq")
    @SequenceGenerator(name="em_stats_seq", sequenceName="em_stats_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
    public Long getId() {
		return id;
	}
    
    public void setId(Long id) {
		this.id = id;
	}
    
    @Column(name = "capture_at")
    @XmlElement(name = "captureAt")
	public Date getCaptureAt() {
		return captureAt;
	}

	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	
	@Column(name = "active_thread_count")
    @XmlElement(name = "activeThreadCount")
	public Integer getActiveThreadCount() {
		return activeThreadCount;
	}

	public void setActiveThreadCount(Integer activeThreadCount) {
		this.activeThreadCount = activeThreadCount;
	}
	
	@Column(name = "gc_count")
    @XmlElement(name = "gcCount")
	public Long getGcCount() {
		return gcCount;
	}

	public void setGcCount(Long gcCount) {
		this.gcCount = gcCount;
	}
	
	@Column(name = "gc_time")
    @XmlElement(name = "gcTime")
	public Long getGcTime() {
		return gcTime;
	}

	public void setGcTime(Long gcTime) {
		this.gcTime = gcTime;
	}
	
	@Column(name = "heap_used")
    @XmlElement(name = "heapUsed")
	public Double getHeapUsed() {
		return heapUsed;
	}

	public void setHeapUsed(Double heapUsed) {
		this.heapUsed = heapUsed;
	}
	
	@Column(name = "non_heap_used")
    @XmlElement(name = "nonHeapUsed")
	public Double getNonHeapUsed() {
		return nonHeapUsed;
	}

	public void setNonHeapUsed(Double nonHeapUsed) {
		this.nonHeapUsed = nonHeapUsed;
	}
	
	@Column(name = "sys_load")
    @XmlElement(name = "sysLoad")
	public Double getSysLoad() {
		return sysLoad;
	}

	public void setSysLoad(Double sysLoad) {
		this.sysLoad = sysLoad;
	}
	
	@Column(name = "cpu_percentage")
    @XmlElement(name = "cpuPercentage")
	public Float getCpuPercentage() {
		return cpuPercentage;
	}

	public void setCpuPercentage(Float cpuPercentage) {
		this.cpuPercentage = cpuPercentage;
	}
	
	@Column(name = "em_instance_id")
    @XmlElement(name = "emInstanceId")
	public Long  getEmInstanceId() {
		return emInstanceId;
	}

	public void setEmInstanceId(Long emInstanceId) {
		this.emInstanceId = emInstanceId;
	}

	public void setIsEmAccessible(Boolean isEmAccessible) {
		this.isEmAccessible = isEmAccessible;
	}
	
	@Column(name = "is_em_accessible")
	@XmlElement(name = "isEmAccessible")
	public Boolean getIsEmAccessible() {
		return isEmAccessible;
	}

}
