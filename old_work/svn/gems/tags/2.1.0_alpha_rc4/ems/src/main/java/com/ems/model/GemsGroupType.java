package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GemsGroupType implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6972249419604828672L;

    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "name")
    private String groupTypeName;

    @XmlElement(name = "groupnumber")
    private int groupNo;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the group type name
     */
    public String getGroupTypeName() {
        return groupTypeName;
    }

    public void setGroupTypeName(String groupTypeName) {
        this.groupTypeName = groupTypeName;
    }

    /**
     * @return the group type number
     */
    public int getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }
}
