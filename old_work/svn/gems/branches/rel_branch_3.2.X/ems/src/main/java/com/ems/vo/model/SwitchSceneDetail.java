package com.ems.vo.model;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SwitchSceneDetail {
	@XmlElement(name = "sceneid")
	private Long sceneId;
	@XmlElement(name = "scenename")
	private String sceneName;
	@XmlElement(name = "sceneorder")
	private Integer sceneOrder;
	
	public Integer getSceneOrder() {
		return sceneOrder;
	}
	public void setSceneOrder(Integer sceneOrder) {
		this.sceneOrder = sceneOrder;
	}
	public Long getSceneId() {
		return sceneId;
	}
	public void setSceneId(Long sceneId) {
		this.sceneId = sceneId;
	}
	public String getSceneName() {
		return sceneName;
	}
	public void setSceneName(String sceneName) {
		this.sceneName = sceneName;
	}
	
	
}
