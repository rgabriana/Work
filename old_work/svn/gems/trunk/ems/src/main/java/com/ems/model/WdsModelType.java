package com.ems.model;

import java.io.Serializable;

public class WdsModelType implements Serializable {
	
	
	private static final long serialVersionUID = -8346640146051015941L;
	
    private Long id;
    private Integer noOfButtons;
    private String name;

	public WdsModelType() {
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
	 * @return the noOfButtons
	 */
	public Integer getNoOfButtons() {
		return noOfButtons;
	}

	/**
	 * @param noOfButtons the noOfButtons to set
	 */
	public void setNoOfButtons(Integer noOfButtons) {
		this.noOfButtons = noOfButtons;
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

}
