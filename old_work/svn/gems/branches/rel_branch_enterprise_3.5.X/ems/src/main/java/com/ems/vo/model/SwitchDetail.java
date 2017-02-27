package com.ems.vo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SwitchDetail {
	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "scenecount")
	private Integer scenecount;
	@XmlElement(name = "currentLightLevel")
	private Integer currentLightLevel;
	@XmlElement(name = "scene")
	List<SwitchSceneDetail> sceneDetail = new ArrayList();

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
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
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the scenecount
	 */
	public Integer getScenecount() {
		return scenecount;
	}

	/**
	 * @param scenecount
	 *            the scenecount to set
	 */
	public void setScenecount(Integer scenecount) {
		this.scenecount = scenecount;
	}

	public Integer getCurrentLightLevel() {
		return currentLightLevel;
	}

	public void setCurrentLightLevel(Integer currentLightLevel) {
		this.currentLightLevel = currentLightLevel;
	}

	public void setScene(Long sceneId, String sceneName,Integer sceneOrder) {
		// scene.put(sceneId, sceneName) ;
		SwitchSceneDetail ssd = new SwitchSceneDetail();
		ssd.setSceneId(sceneId);
		ssd.setSceneName(sceneName);
		ssd.setSceneOrder(sceneOrder);
		sceneDetail.add(ssd);
	}
}
