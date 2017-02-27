/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

/**
 * @author yogesh
 */
public class CannedProfileConfiguration implements Serializable {

    private static final long serialVersionUID = 3417517521123672780L;
    private Long id;
    private String name;
    private Boolean status;
    private Integer parentProfileid;
    
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
        return this.name;
    }

    /**
     * @param name
     *            the Name to set
     */
    public void setName(String name) {
        this.name = name;
    }

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Integer getParentProfileid() {
		return parentProfileid;
	}

	public void setParentProfileid(Integer parentProfileid) {
		this.parentProfileid = parentProfileid;
	}
}
