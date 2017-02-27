package com.ems.model;

import java.io.Serializable;

public class WdsModelTypeButton implements Serializable {

	
	private static final long serialVersionUID = -8346640146061015941L;
	private Long id;
	private Long wdsModelTypeId;
	private Integer buttonNo;
	private String buttonName;
	
	public WdsModelTypeButton() {
		// TODO Auto-generated constructor stub
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
	 * @return the wdsModelTypeId
	 */
	public Long getWdsModelTypeId() {
		return wdsModelTypeId;
	}

	/**
	 * @param wdsModelTypeId the wdsModelTypeId to set
	 */
	public void setWdsModelTypeId(Long wdsModelTypeId) {
		this.wdsModelTypeId = wdsModelTypeId;
	}

	/**
	 * @return the buttonNo
	 */
	public Integer getButtonNo() {
		return buttonNo;
	}

	/**
	 * @param buttonNo the buttonNo to set
	 */
	public void setButtonNo(Integer buttonNo) {
		this.buttonNo = buttonNo;
	}

	/**
	 * @return the buttonName
	 */
	public String getButtonName() {
		return buttonName;
	}

	/**
	 * @param buttonName the buttonName to set
	 */
	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}

}
