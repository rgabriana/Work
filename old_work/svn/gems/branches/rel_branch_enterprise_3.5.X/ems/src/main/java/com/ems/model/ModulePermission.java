package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ModulePermission implements Serializable {

    private static final long serialVersionUID = 4345096822030044699L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "module")
    private Module module;
    private Role role;
    @XmlElement(name = "permissiondetails")
    private PermissionDetails permissionDetails;

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
     * @return the module
     */
    public Module getModule() {
        return module;
    }

    /**
     * @param module
     *            the module to set
     */
    public void setModule(Module module) {
        this.module = module;
    }

    /**
     * @return the permissionDetails
     */
    public PermissionDetails getPermissionDetails() {
        return permissionDetails;
    }

    /**
     * @param permissionDetails
     *            the permissionDetails to set
     */
    public void setPermissionDetails(PermissionDetails permissionDetails) {
        this.permissionDetails = permissionDetails;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }
}
