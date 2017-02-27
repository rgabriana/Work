package com.ems.model;

import java.io.Serializable;

public class MotionGroup implements Serializable {

	private static final long serialVersionUID = -8346640146011015942L;
	private Long id;
	private Integer groupNo;
	private GemsGroup gemsGroup;
	
	public MotionGroup() {
		
	}
	
	public MotionGroup(Long id, Integer groupNo, GemsGroup gemsGroup) {
		this.id = id;
		this.groupNo = groupNo;
		this.gemsGroup = gemsGroup;
	}

	/**
	 * @return the id
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
	 * @return the groupNo
	 */
	public Integer getGroupNo() {
		return groupNo;
	}

	/**
	 * @param groupNo the groupNo to set
	 */
	public void setGroupNo(Integer groupNo) {
		this.groupNo = groupNo;
	}

	/**
	 * @return the gemsGroup
	 */
	public GemsGroup getGemsGroup() {
		return gemsGroup;
	}

	/**
	 * @param gemsGroup the gemsGroup to set
	 */
	public void setGemsGroup(GemsGroup gemsGroup) {
		this.gemsGroup = gemsGroup;
	}
	
	
}
