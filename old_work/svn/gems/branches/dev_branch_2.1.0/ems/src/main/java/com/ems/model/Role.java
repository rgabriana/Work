package com.ems.model;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.RoleType;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Role implements Serializable {

    private static final long serialVersionUID = -4976777011844372813L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private RoleType roleType;
    @XmlElement(name = "modulepermissions")
    private Set<ModulePermission> modulePermissions;

    public Role() {
    }

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

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    /**
     * @return the modulePermissions
     */
    public Set<ModulePermission> getModulePermissions() {
        return modulePermissions;
    }

    /**
     * @param modulePermissions
     *            the modulePermissions to set
     */
    public void setModulePermissions(Set<ModulePermission> modulePermissions) {
        this.modulePermissions = modulePermissions;
    }
}
