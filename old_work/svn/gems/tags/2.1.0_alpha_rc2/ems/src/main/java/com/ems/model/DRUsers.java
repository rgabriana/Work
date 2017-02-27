/**
 * DR User maintains list of openADR user credentials required for communication with openADR server.
 */
package com.ems.model;

import java.io.Serializable;

/**
 * @author yogesh
 * @hibernate.class table="dr_users"
 * 
 */
public class DRUsers implements Serializable {

	private static final long serialVersionUID = -1702580838407770311L;
	private Long id;
	private String name;
	private String password;

	/**
	 * @return unique identifier
	 * @hibernate.id generator-class="native" unsaved-value="null"
	 * @hibernate.generator-param name="sequence" value="dr_users_seq"
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
	 * @hibernate.property column="name"
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
	 * @return the password
	 * @hibernate.property column="password"
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
