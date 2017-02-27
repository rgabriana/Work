package com.ems.model;

import java.io.Serializable;

public class ButtonMap implements Serializable {
	
	private static final long serialVersionUID = 2426089921974146841L;
	private Long id;
	private WdsModelType wdsModelType;

	public ButtonMap() {
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
	 * @return the wdsModelType
	 */
	public WdsModelType getWdsModelType() {
		return wdsModelType;
	}

	/**
	 * @param wdsModelType the wdsModelType to set
	 */
	public void setWdsModelType(WdsModelType wdsModelType) {
		this.wdsModelType = wdsModelType;
	}

	
}
