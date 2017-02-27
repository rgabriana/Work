package com.enlightedinc.hvac.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.enlightedinc.hvac.types.Status;

public class User implements Serializable {


    /**
	 * 
	 */
	private static final long serialVersionUID = 6782516831121022328L;

	private Long id;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "char")
    private Status status;
    private Long roleId;
    
    /**
	 * @return the roleId
	 */
	public Long getRoleId() {
		return roleId;
	}

	/**
	 * @param roleId the roleId to set
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	

}
