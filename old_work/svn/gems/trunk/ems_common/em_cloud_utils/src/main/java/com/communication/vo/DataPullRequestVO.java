package com.communication.vo;

import java.io.Serializable;
import java.util.Date;

import com.communication.types.DataPullRequestStateType;

public class DataPullRequestVO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6486844154050775112L;
	private Long id;
	private String dbName;
	private String tableName;
	private Date fromDate;
	private Date toDate;
	private DataPullRequestStateType state;
	private Integer retry = 0;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	public DataPullRequestStateType getState() {
		return state;
	}
	public void setState(DataPullRequestStateType state) {
		this.state = state;
	}
	public Integer getRetry() {
		return retry;
	}
	public void setRetry(Integer retry) {
		this.retry = retry;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	

}
