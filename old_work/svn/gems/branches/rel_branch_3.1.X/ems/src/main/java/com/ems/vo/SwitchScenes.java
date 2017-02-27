/**
 * 
 */
package com.ems.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Scene;

/**
 * @author sreedhar.kamishetti
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SwitchScenes {

	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "scenes")
	private List<Scene> sceneList;
	
	public SwitchScenes() {
		
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
	 * @return the sceneList
	 */
	public List<Scene> getSceneList() {
		return sceneList;
	}

	/**
	 * @param sceneList the sceneList to set
	 */
	public void setSceneList(List<Scene> sceneList) {
		this.sceneList = sceneList;
	}
	
}
