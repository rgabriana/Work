/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SceneLightLevelTemplate implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "scenetemplateid")
    private Long sceneTemplateId;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "lightlevel")
    private Integer lightlevel;
    @XmlElement(name = "sceneOrder")
    private Integer sceneOrder;
    
    public SceneLightLevelTemplate() {
    	
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
	 * @return the sceneTemplateId
	 */
	public Long getSceneTemplateId() {
		return sceneTemplateId;
	}

	/**
	 * @param sceneTemplateId the sceneTemplateId to set
	 */
	public void setSceneTemplateId(Long sceneTemplateId) {
		this.sceneTemplateId = sceneTemplateId;
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
	 * @return the lightlevel
	 */
	public Integer getLightlevel() {
		return lightlevel;
	}

	/**
	 * @param lightlevel the lightlevel to set
	 */
	public void setLightlevel(Integer lightlevel) {
		this.lightlevel = lightlevel;
	}

	/**
	 * @return the sceneOrder
	 */
	public Integer getSceneOrder() {
		return sceneOrder;
	}

	/**
	 * @param sceneOrder the sceneOrder to set
	 */
	public void setSceneOrder(Integer sceneOrder) {
		this.sceneOrder = sceneOrder;
	}

}
