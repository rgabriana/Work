package com.emscloud.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.communication.types.DataPullRequestStateType;

@Entity
@Table(name = "data_pull_request", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DataPullRequest implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -254695235960981894L;
	private Long id;
	private EmInstance em;
	private Long emId;
	private String tableName;
	private Date fromDate;
	private Date toDate;
	private DataPullRequestStateType state;
	private Integer retry = 0;
	private Users requestedBy;
	private Date requestedAt;
	private Date lastUpdatedAt;
	
	private String emName;
	private String userName;
	private String replicaServer;
	private String requestDate;
	private String from;
	private String to;
	private String lastUpdateDate;
	
	
	@Id	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="data_pull_request_seq")
    @SequenceGenerator(name="data_pull_request_seq", sequenceName="data_pull_request_seq", allocationSize=1, initialValue=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne
	@JoinColumn(name = "em_id")
	public EmInstance getEm() {
		return em;
	}
	public void setEm(EmInstance em) {
		this.em = em;
	}
	
	
	@Column(name = "table_name", nullable = false)
	@XmlElement(name = "tableName")
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Column(name = "from_date", nullable = false)
	@XmlElement(name = "fromDate")
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	@Column(name = "to_date", nullable = false)
	@XmlElement(name = "toDate")
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, columnDefinition = "char")
	@XmlElement(name = "state")
	public DataPullRequestStateType getState() {
		return state;
	}
	public void setState(DataPullRequestStateType state) {
		this.state = state;
	}
	
	@ManyToOne
	@JoinColumn(name = "requested_by")
	public Users getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(Users requestedBy) {
		this.requestedBy = requestedBy;
	}
	
	@Column(name = "requested_at", nullable = false)
	@XmlElement(name = "requestedAt")
	public Date getRequestedAt() {
		return requestedAt;
	}
	public void setRequestedAt(Date requestedAt) {
		this.requestedAt = requestedAt;
	}
	
	@Column(name = "last_updated_at", nullable = false)
	@XmlElement(name = "lastUpdatedAt")
	public Date getLastUpdatedAt() {
		return lastUpdatedAt;
	}
	public void setLastUpdatedAt(Date lastUpdatedAt) {
		this.lastUpdatedAt = lastUpdatedAt;
	}
	
	@Column(name = "retry", nullable = false)
	@XmlElement(name = "retry")
	public Integer getRetry() {
		return retry;
	}
	public void setRetry(Integer retry) {
		this.retry = retry;
	}
	
	@Transient
	@XmlElement(name = "emId")
	public Long getEmId() {
		return emId;
	}
	public void setEmId(Long emId) {
		this.emId = emId;
	}
	
	@Transient
	@XmlElement(name = "em")
	public String getEmName() {
		return emName;
	}
	public void setEmName(String emName) {
		this.emName = emName;
	}
	
	@Transient
	@XmlElement(name = "requestedBy")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	@Transient
	@XmlElement(name = "replicaServer")
	public String getReplicaServer() {
		return replicaServer;
	}
	public void setReplicaServer(String replicaServer) {
		this.replicaServer = replicaServer;
	}
	
	@Transient
	@XmlElement(name = "requestDate")
	public String getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	
	@Transient
	@XmlElement(name = "from")
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	@Transient
	@XmlElement(name = "to")
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	
	@Transient
	@XmlElement(name = "lastUpdateDate")
	public String getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	
	
	
	
	
	
	
	

}
