package com.ems.model;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.TenantStatus;

/**
 * 
 * @author lalit
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Tenant implements Serializable {
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    private String address;
    private String email;
    private String phoneNo;
    private TenantStatus status;
    private String validDomain;
    private Set<TenantLocations> tenantLocations;

    public Tenant() {
        super();
    }

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
     * @return the email
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the email
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    /**
     * @return the phone no
     */
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public TenantStatus getStatus() {
        return status;
    }

    /**
     * @return the status
     */
    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public String getValidDomain() {
        return validDomain;
    }

    /**
     * @return the validDomain
     */
    public void setValidDomain(String validDomain) {
        this.validDomain = validDomain;
    }

    public Set<TenantLocations> getTenantLocations() {
        return tenantLocations;
    }

    public void setTenantLocations(Set<TenantLocations> tenantLocations) {
        this.tenantLocations = tenantLocations;
    }

}
