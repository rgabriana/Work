package com.ems.model;

import java.io.Serializable;
import java.sql.Blob;

public class WdsModelType implements Serializable {
	
	
	private static final long serialVersionUID = -8346640146051015941L;
	
    private Long id;
    private Blob modelImage;
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
	 * @return the modelImage
	 */
	public Blob getModelImage() {
		return modelImage;
	}

	/**
	 * @param modelImage the modelImage to set
	 */
	public void setModelImage(Blob modelImage) {
		this.modelImage = modelImage;
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
