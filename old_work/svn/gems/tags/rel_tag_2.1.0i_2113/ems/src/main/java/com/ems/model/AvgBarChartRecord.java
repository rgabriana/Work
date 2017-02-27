package com.ems.model;

import java.util.Date;


public class AvgBarChartRecord{
	private Integer id;
	private String name;
	private Float EN;	
	private Date showOn;
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the eN
	 */
	public Float getEN() {
		return EN;
	}
	/**
	 * @param eN the eN to set
	 */
	public void setEN(Float eN) {
		EN = eN;
	}
	/**
	 * @return the showOn
	 */
	public Date getShowOn() {
		return showOn;
	}
	/**
	 * @param showOn the showOn to set
	 */
	public void setShowOn(Date showOn) {
		this.showOn = showOn;
	}
}


