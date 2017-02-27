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
public class SceneTemplate implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    
    public SceneTemplate() {
    	
    }
    
    public SceneTemplate(String name) {
    	this.name = name;    	
    }
    
    public SceneTemplate(Long id, String name) {
        this.id = id;
        this.name = name;        
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
}
