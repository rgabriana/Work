package com.emscloud.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.types.SiteAnomalyType;

@Entity
@Table(name = "site_anomalies", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SiteAnomaly implements java.io.Serializable {

	private static final long serialVersionUID = 6954718773018880288L;
	private Long id;  
	private String geoLocation;
	private Date reportDate;
	private Date startDate;
	private Date endDate;
	private String issue;
	private String details;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="site_anomalies_seq")
    @SequenceGenerator(name="site_anomalies_seq", sequenceName="site_anomalies_seq",initialValue=1,allocationSize=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "geo_location")
	@XmlElement(name = "geoLocation")
	public String getGeoLocation() {
		return geoLocation;
	}
	public void setGeoLocation(String geoLocation) {
		this.geoLocation = geoLocation;
	}
	@Column(name = "report_date")
	@XmlElement(name = "reportDate")
	public Date getReportDate() {
		return reportDate;
	}
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}
	
	@Column(name = "start_date")
	@XmlElement(name = "startDate")
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@Column(name = "end_date")
	@XmlElement(name = "endDate")
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Column(name = "issue")
	@XmlElement(name = "issue")
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	@Column(name = "details")
	@XmlElement(name = "details")
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
} //end of class Site
